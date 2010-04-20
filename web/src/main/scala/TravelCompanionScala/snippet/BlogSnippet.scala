package TravelCompanionScala.snippet

import _root_.scala.xml.{NodeSeq, Text}

import _root_.net.liftweb._
import http._
import S._
import util._
import Helpers._

import TravelCompanionScala.model._

/**
 * Created by IntelliJ IDEA.
 * User: Ralf Muri
 * Date: 19.04.2010
 * Time: 09:02:05
 * To change this template use File | Settings | File Templates.
 */

class BlogSnippet {
  // Set up a requestVar to track the STAGE object for edits and adds
  object blogEntryVar extends RequestVar(new BlogEntry())
  def blogEntry = blogEntryVar.is

  def listEntries(html: NodeSeq, entries: List[BlogEntry]): NodeSeq = {
    entries.flatMap(entry => bind("entry", html,
      "title" -> entry.title,
      "preview" -> entry.content.substring(0, 50),
      "readOn" -> SHtml.link("view", () => blogEntryVar(blogEntry), Text(?("weiterlesen"))),
      "lastUpdate" -> entry.lastUpdated.toString,
      "creator" -> entry.owner.name))
  }

  def listOtherEntries(html: NodeSeq): NodeSeq = {
    val entries = Model.createQuery[BlogEntry]("from BlogEntry e where e.owner.id != :id").setParams("id" -> UserManagement.currentUser.id).findAll.toList
    listEntries(html, entries)
  }

  def listOwnEntries(html: NodeSeq): NodeSeq = {
    val entries = scala.collection.JavaConversions.asBuffer(UserManagement.currentUser.blogEntries).toList
    listEntries(html, entries)
  }
}