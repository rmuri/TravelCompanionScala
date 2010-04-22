package TravelCompanionScala.snippet

import _root_.scala.xml.{NodeSeq, Text}

import _root_.net.liftweb._
import common.Empty
import http._
import S._
import util._
import Helpers._
import net.liftweb.common._

import TravelCompanionScala.model._
import java.text.SimpleDateFormat

/**
 * Created by IntelliJ IDEA.
 * User: Ralf Muri
 * Date: 19.04.2010
 * Time: 09:02:05
 * To change this template use File | Settings | File Templates.
 */

// Set up a requestVar to track the STAGE object for edits and adds
object blogEntryVar extends RequestVar[BlogEntry](new BlogEntry())

class BlogSnippet {
  def blogEntry = blogEntryVar.is

  def removeBlogEntry(entry: BlogEntry) {
    val e = Model.merge(entry)
    Model.remove(e)
    S.redirectTo("/blog/list")
  }

  def editBlogEntry(html: NodeSeq): NodeSeq = {
    def doEdit() = {
      Model.mergeAndFlush(blogEntry)
      S.redirectTo("/blog/list")
    }

    val currentEntry = blogEntry

    if (currentEntry.owner == null) {
      currentEntry.owner = UserManagement.currentUser
    }

    val tours = Model.createNamedQuery[Tour]("findTourByOwner").setParams("id" -> UserManagement.currentUser.id).findAll.toList
    val choices = List("" -> "- Keine -") ::: tours.map(tour => (tour.id.toString -> tour.name)).toList

    bind("entry", html,
      "title" -> SHtml.text(currentEntry.title, currentEntry.title = _),
      "content" -> SHtml.textarea(currentEntry.content, currentEntry.content = _),
      "tour" -> SHtml.select(choices, Empty, (tourId: String) => {if (tourId != "") currentEntry.tour = Model.getReference(classOf[Tour], tourId.toLong) else currentEntry.tour = null}),
      "owner" -> SHtml.text(currentEntry.owner.name, currentEntry.owner.name = _),
      "submit" -> SHtml.submit(?("save"), () => {blogEntryVar(currentEntry); doEdit}))
  }

  def showEntry(html: NodeSeq): NodeSeq = {

    val currentEntry = blogEntry

    bind("entry", html,
      "title" -> currentEntry.title,
      "tour" -> {
        if (currentEntry.tour == null) {
          NodeSeq.Empty
        } else {
          Text(?("blog.belongsTo") + " ") ++ SHtml.link("/tour/view", () => tourVar(currentEntry.tour), Text(currentEntry.tour.name))
        }
      },
      "content" -> currentEntry.content,
      "lastUpdated" -> new SimpleDateFormat("dd.MM.yyyy HH:mm").format(currentEntry.lastUpdated))
  }

  def addComment(html: NodeSeq): NodeSeq = {
    def doAdd(c: Comment) = {
      c.blogEntry = blogEntry
      c.member = UserManagement.currentUser
      Model.mergeAndFlush(c)
    }

    val currentEntry = blogEntry
    val newComment = new Comment

    bind("comment", html,
      "content" -> SHtml.textarea(newComment.content, newComment.content = _),
      "submit" -> SHtml.submit(?("save"), () => {blogEntryVar(currentEntry); doAdd(newComment)}))
  }

  def showComments(html: NodeSeq): NodeSeq = {
    val comments = Model.createNamedQuery[Comment]("findCommentsByEntry").setParams("entry" -> blogEntry).findAll.toList
    comments.flatMap(comment => bind("comment", html,
      "member" -> comment.member.name,
      "dateCreated" -> new SimpleDateFormat("dd.MM.yyyy HH:mm").format(comment.dateCreated),
      "content" -> comment.content))
  }

  def showBlogEntriesFromTour(html: NodeSeq): NodeSeq = {
    val currentTour = tourVar.is
    val entries = Model.createNamedQuery[BlogEntry]("findEntriesByTour").setParams("tour" -> currentTour).findAll.toList

    entries.flatMap(entry => bind("entry", html,
      "lastUpdated" -> new SimpleDateFormat("dd.MM.yyyy HH:mm").format(entry.lastUpdated),
      "title" -> entry.title,
      "preview" -> entry.content.substring(0, Math.min(entry.content.length, 50)),
      "readOn" -> SHtml.link("/blog/view", () => blogEntryVar(entry), Text(?("blog.readOn"))),
      "edit" -> SHtml.link("/blog/edit", () => blogEntryVar(entry), Text(?("edit"))),
      "remove" -> SHtml.link("/blog/remove", () => removeBlogEntry(entry), Text(?("remove")))))
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
      "edit" -> SHtml.link("edit", () => blogEntryVar(entry), Text(?("edit"))),
      "comments" -> SHtml.link("view", () => blogEntryVar(entry), Text(?("blog.comments"))),
      "remove" -> SHtml.link("remove", () => removeBlogEntry(entry), Text(?("remove"))),
      "preview" -> entry.content.substring(0, Math.min(entry.content.length, 50)),
      "readOn" -> SHtml.link("view", () => blogEntryVar(entry), Text(?("blog.readOn"))),
      "lastUpdated" -> new SimpleDateFormat("dd.MM.yyyy HH:mm").format(entry.lastUpdated),
      "creator" -> entry.owner.name))
  }

  def listOtherEntries(html: NodeSeq): NodeSeq = {
    val entries = Model.createNamedQuery[BlogEntry]("findEntriesByOthers").setParams("id" -> UserManagement.currentUser.id).findAll.toList
    listEntries(html, entries)
  }

  def listOwnEntries(html: NodeSeq): NodeSeq = {
    val entries = Model.createNamedQuery[BlogEntry]("findEntriesByOwner").setParams("id" -> UserManagement.currentUser.id).findAll.toList
    listEntries(html, entries)
  }
}