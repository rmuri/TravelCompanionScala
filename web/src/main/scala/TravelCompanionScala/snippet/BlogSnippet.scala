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
import TravelCompanionScala.controller._

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
  def is_valid_Entry_?(toCheck: BlogEntry): Boolean = {
    val validationResult = validator.get.validate(toCheck)
    validationResult.foreach((e) => S.error(e.getPropertyPath + " " + e.getMessage))
    validationResult.isEmpty
  }

  def is_valid_Comment_?(toCheck: Comment): Boolean = {
    val validationResult = validator.get.validate(toCheck)
    validationResult.foreach((e) => S.error(e.getPropertyPath + " " + e.getMessage))
    validationResult.isEmpty
  }

  /* Blog as single webpage Application */

  val entriesDivId = "entriesList"
  val entryFormDivId = "addEntryForm"
  val newEntryLink = "newEntryLink"
  val entryErrorDivId = "addEntryErrors"
  val blogEntryDivId = "blogEntry"
  val commentDivId = "comment"
  val commentErrorDivId = "commentError"
  val commentFormDivId = "commentForm"

  def getErrorDiv(divIdPrefix: String) = <div id={divIdPrefix} style="display: none;">
    <lift:Msgs>
        <lift:error_msg/>
    </lift:Msgs>
  </div>

  def render(html: NodeSeq): NodeSeq = {

    val entryTemplate = chooseTemplate("choose", "entry", html)
    val entryFormTemplate = chooseTemplate("choose", "form", html)
    val commentsTemplate = chooseTemplate("choose", "comments", html)
    val commentFormTemplate = chooseTemplate("choose", "commentForm", html)

    def doEditBlogEntry(entry: BlogEntry): JsCmd = {
      val save = () => {
        if (is_valid_Entry_?(entry)) {
          val merged = Model.mergeAndFlush(entry)
          BlogCache.cache ! EditEntry(merged)
          JqSetHtml(blogEntryDivId + entry.id, listEntries(entryTemplate, List(merged)))
        } else {
          Show(entryErrorDivId)
        }
      }
      val cancel = () => Hide(entryErrorDivId) & JqSetHtml(blogEntryDivId + entry.id, listEntries(entryTemplate, List(Model.getReference(classOf[BlogEntry], entry.id))))
      JqSetHtml(blogEntryDivId + entry.id, getEntryForm(entry, entryFormTemplate, save, cancel))
    }

    def doComments(entry: BlogEntry): JsCmd = {

      def removeComment(c: Comment): JsCmd = {
        val mergedc = Model.merge(c)
        Model.removeAndFlush(mergedc)
        BlogCache.cache ! DeleteComment(entry)
        JqSetHtml(commentDivId + entry.id, renderComments)
      }

      def bindComment(c: Comment) = bind("comment", chooseTemplate("blog", "comment", html),
        "member" -> c.member.name,
        "dateCreated" -> new SimpleDateFormat("dd.MM.yyyy HH:mm").format(c.dateCreated),
        "content" -> c.content,
        "options" -> {
          if ((c.member == UserManagement.currentUser) || (entry.owner == UserManagement.currentUser))
            bind("link", chooseTemplate("option", "list", html), "remove" -> SHtml.a(() => removeComment(c), Text(?("remove"))))
          else
            NodeSeq.Empty
        })

      def renderComments() = {
        val merged = Model.merge(entry)
        Model.refresh(merged)
        bind("blog", commentsTemplate, "comment" -> merged.comments.flatMap(c => bindComment(c)))
      }

      def renderNewCommentForm(): NodeSeq = {
        def doSaveComment(c: Comment): JsCmd = {
          if (is_valid_Comment_?(c)) {
            val merged = Model.merge(entry)
            merged.comments.add(c)
            Model.mergeAndFlush(merged)
            BlogCache.cache ! AddComment(merged)
            Hide(commentErrorDivId) & JqSetHtml(commentDivId + entry.id, renderComments) & JqSetHtml(commentFormDivId + entry.id, renderNewCommentForm)
          } else {
            Show(commentErrorDivId)
          }
        }

        val newComment = new Comment
        newComment.blogEntry = entry
        newComment.member = UserManagement.currentUser
        newComment.dateCreated = TimeHelpers.now
        bind("blog", SHtml.ajaxForm(commentFormTemplate),
          "error" -> getErrorDiv(commentErrorDivId),
          "newComment" -> SHtml.textarea(newComment.content, newComment.content = _),
          "submit" -> SHtml.ajaxSubmit(?("save"), () => doSaveComment(newComment)),
          "cancel" -> SHtml.a(() => Hide(commentErrorDivId) &
                  JqSetHtml(blogEntryDivId + entry.id, listEntries(entryTemplate, List(entry))), Text(?("cancel")), "class" -> "button"))
      }

      JqSetHtml(commentDivId + entry.id, renderComments) & JqSetHtml(commentFormDivId + entry.id, renderNewCommentForm)
    }

    def doRemoveBlogEntry(entry: BlogEntry): JsCmd = {
      val e = Model.merge(entry)
      Model.removeAndFlush(e)
      BlogCache.cache ! DeleteEntry(e)
      JqSetHtml(blogEntryDivId + entry.id, NodeSeq.Empty)
      //      object myRemoveHtml {
      //        def apply(uid: String): JsCmd =
      //          JqJE.JqId(JE.Str(uid)) ~> JqJE.JqRemove
      //      }
      //      case class myJqRemove(id: String) extends JsCmd {
      //        override def toJsCmd = "jQuery('#'+" + id + ").remove()"
      //      }
      //      myJqRemove(blogEntryDivId)
    }

    def listEntries(html: NodeSeq, entries: List[BlogEntry]): NodeSeq = {
      def belongsTo(entry: BlogEntry): NodeSeq = {
        if (entry.tour == null) {
          NodeSeq.Empty
        } else {
          Text(?("blog.belongsTo") + " ") ++ SHtml.link("/tour/view", () => tourVar(entry.tour), Text(entry.tour.name))
        }
      }
      entries.flatMap(entry => bind("entry", html,
        FuncAttrBindParam("id", _ => Text(blogEntryDivId + entry.id), "id"),
        "title" -> entry.title,
        "tour" -> belongsTo(entry),
        "content" -> entry.content,
        "edit" -> SHtml.a(() => doEditBlogEntry(entry), Text(?("edit"))),
        "comments" -> SHtml.a(() => doComments(entry), Text(?("blog.comments"))),
        "remove" -> SHtml.a(() => doRemoveBlogEntry(entry), Text(?("remove"))),
        "preview" -> entry.content.substring(0, Math.min(entry.content.length, 50)),
        "readOn" -> SHtml.link("/blog/view", () => blogEntryVar(entry), Text(?("blog.readOn"))),
        "lastUpdated" -> new SimpleDateFormat("dd.MM.yyyy HH:mm").format(entry.lastUpdated),
        "creator" -> entry.owner.name,
        FuncAttrBindParam("commentsId", _ => Text(commentDivId + entry.id), "id"),
        FuncAttrBindParam("commentFormId", _ => Text(commentFormDivId + entry.id), "id")))
    }

    def getEntryForm(e: BlogEntry, html: NodeSeq, submitFunc: () => JsCmd, cancelFunc: () => JsCmd): NodeSeq = {
      val tours = Model.createNamedQuery[Tour]("findTourByOwner").setParams("owner" -> UserManagement.currentUser).findAll.toList
      val choices = List("" -> "- Keine -") ::: tours.map(tour => (tour.id.toString -> tour.name)).toList
      bind("entry", SHtml.ajaxForm(html),
        "error" -> getErrorDiv(entryErrorDivId),
        "title" -> SHtml.text(e.title, e.title = _),
        "content" -> SHtml.textarea(e.content, e.content = _),
        "tour" -> SHtml.select(choices, if (e.tour == null) Empty else Full(e.tour.id.toString), (tourId: String) => {
          if (tourId != "") e.tour = Model.getReference(classOf[Tour], tourId.toLong) else e.tour = null
        }),
        "owner" -> SHtml.text(e.owner.name, e.owner.name = _),
        "submit" -> SHtml.ajaxSubmit(?("save"), submitFunc),
        "cancel" -> SHtml.a(cancelFunc, Text(?("cancel")), "class" -> "button"))
    }

    def doNewEntry() = {
      def save(entry: BlogEntry): JsCmd = {
        if (is_valid_Entry_?(entry)) {
          val merged = Model.mergeAndFlush(entry)
          BlogCache.cache ! AddEntry(merged)
          Hide(entryErrorDivId) &
                  Hide(entryFormDivId) &
                  Show(newEntryLink) &
                  AppendHtml(entriesDivId, listEntries(entryTemplate, List(merged)))
        } else {
          Show(entryErrorDivId)
        }
      }

      def addEntryForm(html: NodeSeq): NodeSeq = {
        val e = new BlogEntry
        e.owner = UserManagement.currentUser
        e.lastUpdated = TimeHelpers.now
        getEntryForm(e, html, () => save(e), () => Hide(entryFormDivId) & Hide(entryErrorDivId) & Show(newEntryLink))
      }

      SHtml.a(
        () => Hide(newEntryLink) & Show(entryFormDivId) & JqSetHtml(entryFormDivId, addEntryForm(entryFormTemplate)),
        Text(?("blog.addEntry")),
        "class" -> "button")
    }

    def listOwnEntries(html: NodeSeq): NodeSeq = {
      val entries = Model.createNamedQuery[BlogEntry]("findEntriesByOwner").setParams("owner" -> UserManagement.currentUser).findAll.toList
      listEntries(html, entries)
    }

    bind("ajax", html,
      "entriesList" -> <div id={entriesDivId}>
        {listOwnEntries(entryTemplate)}
      </div>,
      "template" -> NodeSeq.Empty,
      "newEntry" -> <div id={newEntryLink} class="content">
        {doNewEntry}
      </div>,
      "newEntryForm" -> <div id={entryFormDivId}></div>)
  }


  /* Blog as traditional multi page application */

  def blogEntry = blogEntryVar.is

  def removeBlogEntry(entry: BlogEntry) {
    val e = Model.merge(entry)
    Model.remove(e)
    BlogCache.cache ! DeleteEntry(e)
    S.redirectTo("/blog/list")
  }

  def editBlogEntry(html: NodeSeq): NodeSeq = {
    def doEdit() = {
      if (is_valid_Entry_?(blogEntry)) {
        val newEntry = Model.mergeAndFlush(blogEntry)
        BlogCache.cache ! AddEntry(newEntry)
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

  def listEntries(html: NodeSeq, entries: List[BlogEntry]): NodeSeq = {
    entries.flatMap(entry => bind("entry", html,
      "title" -> entry.title,
      "tour" -> {
        if (entry.tour == null) {
          NodeSeq.Empty
        } else {
          Text(?("blog.belongsTo") + " ") ++ SHtml.link("/tour/view", () => tourVar(entry.tour), Text(entry.tour.name))
        }
      },
      "content" -> entry.content,
      "edit" -> SHtml.link("/blog/edit", () => blogEntryVar(entry), Text(?("edit"))),
      "comments" -> SHtml.link("/blog/view", () => blogEntryVar(entry), Text(?("blog.comments"))),
      "remove" -> SHtml.link("/blog/remove", () => removeBlogEntry(entry), Text(?("remove"))),
      "preview" -> entry.content.substring(0, Math.min(entry.content.length, 50)),
      "readOn" -> SHtml.link("/blog/view", () => blogEntryVar(entry), Text(?("blog.readOn"))),
      "lastUpdated" -> new SimpleDateFormat("dd.MM.yyyy HH:mm").format(entry.lastUpdated),
      "creator" -> entry.owner.name))
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