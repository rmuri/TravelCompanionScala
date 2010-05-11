package TravelCompanionScala.snippet

import _root_.scala.xml.{NodeSeq, Text}

import _root_.net.liftweb._
import common.{Full, Empty}
import http._
import js.JE.ElemById
import js.jquery.JqJE.{JqRemove, JqId}
import js.jquery.{JqJE, JqJsCmds}
import js.{JE, JsCmds, JsCmd}
import S._
import util._
import Helpers._
import JqJsCmds._

import TravelCompanionScala.model._
import java.text.SimpleDateFormat
import scala.collection.JavaConversions._

/**
 * Created by IntelliJ IDEA.
 * User: Ralf Muri
 * Date: 19.04.2010
 * Time: 09:02:05
 * To change this template use File | Settings | File Templates.
 */

// Set up a requestVar to track the STAGE object for edits and adds
object blogEntryVar extends RequestVar[BlogEntry](new BlogEntry())
object commentVar extends RequestVar[Comment](new Comment())

class BlogSnippet {
  def blogEntry = blogEntryVar.is

  def removeBlogEntry(entry: BlogEntry) {
    val e = Model.merge(entry)
    Model.remove(e)
    S.redirectTo("/blog/list")
  }

  def is_valid_Entry_?(toCheck: BlogEntry): Boolean = {
    val validationResult = validator.get.validate(toCheck)
    validationResult.foreach((e) => S.error(e.getPropertyPath + " " + e.getMessage))
    validationResult.isEmpty
  }

  def editBlogEntry(html: NodeSeq): NodeSeq = {
    def doEdit() = {
      if (is_valid_Entry_?(blogEntry)) {
        Model.mergeAndFlush(blogEntry)
        S.redirectTo("/blog/list")
      }
    }

    val currentEntry = blogEntry

    currentEntry.owner = UserManagement.currentUser
    currentEntry.lastUpdated = TimeHelpers.now

    val tours = Model.createNamedQuery[Tour]("findTourByOwner").setParams("owner" -> UserManagement.currentUser).findAll.toList
    val choices = List("" -> "- Keine -") ::: tours.map(tour => (tour.id.toString -> tour.name)).toList

    bind("entry", html,
      "title" -> SHtml.text(currentEntry.title, currentEntry.title = _),
      "content" -> SHtml.textarea(currentEntry.content, currentEntry.content = _),
      "tour" -> SHtml.select(choices, if (currentEntry.tour == null) Empty else Full(currentEntry.tour.id.toString), (tourId: String) => {if (tourId != "") currentEntry.tour = Model.getReference(classOf[Tour], tourId.toLong) else currentEntry.tour = null}),
      "owner" -> SHtml.text(currentEntry.owner.name, currentEntry.owner.name = _),
      "submit" -> SHtml.submit(?("save"), () => {blogEntryVar(currentEntry); doEdit}))
  }

  def showEntry(html: NodeSeq): NodeSeq = {
    val currentEntry = blogEntry
    listEntries(html, List(blogEntry))
  }

  val entryForm = "addEntryForm"
  val newEntryLink = "newEntryLink"
  val errorDiv = "addEntryErrors"
  val blogEntryDivIdPrefix = "blogEntry"

  def doEditBlogEntry(entry: BlogEntry): JsCmd = {
    JqSetHtml(blogEntryDivIdPrefix + entry.id, Text("Dieser Eintrag ist in Bearbeitung"))
  }

  def doRemoveBlogEntry(entry: BlogEntry): JsCmd = {
    //    JqId(JE.Str(blogEntryDivIdPrefix + entry.id)) ~> JqRemove
    //    ElemById(blogEntryDivIdPrefix + entry.id) ~> JsRemove
    val e = Model.merge(entry)
    Model.remove(e)
    JqSetHtml(blogEntryDivIdPrefix + entry.id, Text("Dieser Eintrag wurde geloescht"))
  }

  def listEntries(html: NodeSeq, entries: List[BlogEntry]): NodeSeq = {
    entries.flatMap(entry => bind("entry", html,
      FuncAttrBindParam("id", _ => Text(blogEntryDivIdPrefix + entry.id), "id"),
      "title" -> entry.title,
      "tour" -> {
        if (entry.tour == null) {
          NodeSeq.Empty
        } else {
          Text(?("blog.belongsTo") + " ") ++ SHtml.link("/tour/view", () => tourVar(entry.tour), Text(entry.tour.name))
        }
      },
      "content" -> entry.content,
      "edit" -> SHtml.a(() => doEditBlogEntry(entry), Text(?("edit"))),
      "comments" -> SHtml.link("/blog/view", () => blogEntryVar(entry), Text(?("blog.comments"))),
      "remove" -> SHtml.a(() => doRemoveBlogEntry(entry), Text(?("remove"))),
      "preview" -> entry.content.substring(0, Math.min(entry.content.length, 50)),
      "readOn" -> SHtml.link("/blog/view", () => blogEntryVar(entry), Text(?("blog.readOn"))),
      "lastUpdated" -> new SimpleDateFormat("dd.MM.yyyy HH:mm").format(entry.lastUpdated),
      "creator" -> entry.owner.name))
  }


  def ajaxForm(html: NodeSeq): NodeSeq = {

    def doEdit(entry: BlogEntry): JsCmd = {
      if (is_valid_Entry_?(entry)) {
        val merged = Model.mergeAndFlush(entry)
        Hide(errorDiv) &
                Hide(entryForm) &
                Show(newEntryLink) &
                AppendHtml("entriesList", listEntries(chooseTemplate("choose", "entry", html), List(merged)))
      } else {
        Show(errorDiv)
      }
    }

    def addEntryForm(html: NodeSeq): NodeSeq = {
      val e = new BlogEntry
      e.owner = UserManagement.currentUser
      e.lastUpdated = TimeHelpers.now

      val tours = Model.createNamedQuery[Tour]("findTourByOwner").setParams("owner" -> UserManagement.currentUser).findAll.toList
      val choices = List("" -> "- Keine -") ::: tours.map(tour => (tour.id.toString -> tour.name)).toList
      bind("entry", SHtml.ajaxForm(html),
        "title" -> SHtml.text(e.title, e.title = _),
        "content" -> SHtml.textarea(e.content, e.content = _),
        "tour" -> SHtml.select(choices, if (e.tour == null) Empty else Full(e.tour.id.toString), (tourId: String) => {if (tourId != "") e.tour = Model.getReference(classOf[Tour], tourId.toLong) else e.tour = null}),
        "owner" -> SHtml.text(e.owner.name, e.owner.name = _),
        "submit" -> SHtml.ajaxSubmit(?("save"), () => doEdit(e)),
        "cancel" -> SHtml.a(() => Hide(entryForm) & Hide(errorDiv) & Show(newEntryLink), Text("Cancel"), "class" -> "button"))
    }

    def newEntryButton() = {
      SHtml.a(
        () => Hide(newEntryLink) & Show(entryForm) & JqSetHtml(entryForm, addEntryForm(chooseTemplate("choose", "form", html))),
        Text("Neuer Eintrag"),
        "class" -> "button", "id" -> newEntryLink)
    }

    bind("ajax", html,
      "entriesList" -> <div id="entriesList">
        {listOwnEntries(chooseTemplate("choose", "entry", html))}
      </div>,
      "errors" -> <div id={errorDiv} style="display: none;">
        <lift:Msgs>
            <lift:error_msg/>
        </lift:Msgs>
      </div>,
      "template" -> NodeSeq.Empty,
      "newEntry" -> newEntryButton,
      "newEntryForm" -> <div id="addEntryForm"></div>)
  }

  def showBlogEntriesFromTour(html: NodeSeq): NodeSeq = {
    val currentTour = tourVar.is
    val entries = Model.createNamedQuery[BlogEntry]("findEntriesByTour").setParams("tour" -> currentTour).findAll.toList
    listEntries(html, entries)
  }

  def listOtherEntries(html: NodeSeq): NodeSeq = {
    val entries = Model.createNamedQuery[BlogEntry]("findEntriesByOthers").setParams("owner" -> UserManagement.currentUser).findAll.toList
    listEntries(html, entries)
  }

  def listOwnEntries(html: NodeSeq): NodeSeq = {
    val entries = Model.createNamedQuery[BlogEntry]("findEntriesByOwner").setParams("owner" -> UserManagement.currentUser).findAll.toList
    listEntries(html, entries)
  }

  def is_valid_Comment_?(toCheck: Comment): Boolean = {
    val validationResult = validator.get.validate(toCheck)
    validationResult.foreach((e) => S.error(e.getPropertyPath + " " + e.getMessage))
    validationResult.isEmpty
  }

  def addComment(html: NodeSeq): NodeSeq = {
    def doAdd(c: Comment) = {
      if (is_valid_Comment_?(c))
        Model.mergeAndFlush(c)
    }

    val currentEntry = blogEntry
    val newComment = new Comment
    newComment.blogEntry = blogEntry
    newComment.member = UserManagement.currentUser
    newComment.dateCreated = TimeHelpers.now

    bind("comment", html,
      "content" -> SHtml.textarea(newComment.content, newComment.content = _),
      "submit" -> SHtml.submit(?("save"), () => {
        blogEntryVar(currentEntry);
        doAdd(newComment)
      }))
  }

  def doRemoveComment(comment: Comment) {
    val c = Model.merge(comment)
    Model.remove(c)
    S.redirectTo("/blog/view", () => blogEntryVar(c.blogEntry))
  }

  def showComments(html: NodeSeq): NodeSeq = {
    val comments = Model.createNamedQuery[Comment]("findCommentsByEntry").setParams("entry" -> blogEntry).findAll.toList
    comments.flatMap(comment =>
      bind("comment", html,
        "member" -> comment.member.name,
        "dateCreated" -> new SimpleDateFormat("dd.MM.yyyy HH:mm").format(comment.dateCreated),
        "content" -> comment.content,
        "options" -> {
          if ((comment.member == UserManagement.currentUser) || (blogEntry.owner == UserManagement.currentUser))
            bind("link", chooseTemplate("option", "list", html), "remove" -> SHtml.link("remove", () => {
              blogEntryVar(comment.blogEntry);
              doRemoveComment(comment)
            }, Text(?("remove"))))
          else
            NodeSeq.Empty
        }))
  }
}