package TravelCompanionScala.api

import net.liftweb.http.rest.RestHelper
import net.liftweb.util.Helpers.toLong
import TravelCompanionScala.model.EntityConverter._
import net.liftweb.http.rest._
import net.liftweb.common._
import xml.{Node, Elem, NodeSeq, UnprefixedAttribute}
import TravelCompanionScala.controller._
import net.liftweb.json.Xml
import TravelCompanionScala.model._

/**
 * The RestAPI Object provides a API for the blog functionality accessible for REST Clients.
 * It extends from the RestHelper trait which provides support for implementing a REST API.
 *
 * Further Information on RestHelper and creating a REST API can be found on:
 * - http://www.assembla.com/wiki/show/liftweb/REST_Web_Services
 * - Technologiestudium (github link) Chapter 4.7 [German]
 *
 * Known Issues:
 * -
 *
 *
 * @author Ralf Muri
 *
 */
object RestAPI extends RestHelper {

  /**
   *  Extractor: extracts a blog entry by id
   * @param in the id of a blog entry
   */
  object AsBlogEntry {
    def unapply(in: String): Option[BlogEntry] = {
      Model.find(classOf[BlogEntry], toLong(in))
    }
  }

  /**
   * Extractor: extracts a comment by id
   * * @param in the id of a comment
   */
  object AsComment {
    def unapply(in: String): Option[Comment] = {
      Model.find(classOf[Comment], toLong(in))
    }
  }

    def listTours = {
    <Tours>
      {Model.findAll[Tour]("findAllTours").flatMap(e => e.toXml)}
    </Tours>
  }

  def listEntries = {
    <BlogEntries>
      {Model.findAll[BlogEntry]("findAllEntries").flatMap(e => e.toXml)}
    </BlogEntries>
  }

  def listComments(entry: BlogEntry) = {
    <Comments>
      {Model.findAll[Comment]("findCommentsByEntry", "entry" -> entry).flatMap(e => e.toXml)}
    </Comments>
  }

  def saveBlogEntry(xml: Node) = {
    val e = xml.entryFromXml
    if (validator.is_valid_entity_?(e)) {
      val merged = Model.mergeAndFlush(e)
      BlogCache.cache ! EditEntry(merged)
      merged.toXml
    } else <error/>
  }

  def createBlogEntry(xml: Node) = {
    val e = xml.entryFromXml
    if (validator.is_valid_entity_?(e)) {
      val merged = Model.mergeAndFlush(e)
      BlogCache.cache ! EditEntry(merged)
      merged.toXml
    } else <error/>
  }

  def createComment(xml: Node, entry: BlogEntry) = {
    val c = xml.commentFromXml
    c.blogEntry = entry
    if (validator.is_valid_entity_?(c)) {
      val mergedComment = Model.mergeAndFlush(c)
      val mergedEntry = Model.merge(entry)
      Model.refresh(mergedEntry)
      BlogCache.cache ! AddComment(mergedEntry)
      mergedComment.toXml
    } else <error/>
  }

  def removeComment(c: Comment, e: BlogEntry) = {
    val mergedc = Model.merge(c)
    Model.removeAndFlush(mergedc)
    BlogCache.cache ! DeleteComment(e)
    mergedc.toXml
  }

  implicit def cvt: JxCvtPF[Object] = {
    case (JsonSelect, n: Elem, _) => Xml.toJson(n)
    case (XmlSelect, n: Elem, _) => n
    case (JsonSelect, o: Object, _) => o.toJson
    case (XmlSelect, o: Object, _) => o.toXml
  }

  serve {
    // POST /api/blog creates new entry with xml data from request body
    case "api" :: "blog" :: Nil XmlPost xml -> _ => createBlogEntry(xml)

    // PUT /api/blog/<valid id> updates the respective entry with xml data from request body
    case "api" :: "blog" :: AsBlogEntry(entry) :: Nil XmlPut xml -> _ => saveBlogEntry(xml)

    // POST /api/blog/<valid id>/comment creates a new comment on the respective entry
    case "api" :: "blog" :: AsBlogEntry(entry) :: "comment" :: Nil XmlPost xml -> _ => createComment(xml, entry)

    // DELETE /api/blog/<valid id>/comment/<valid id> removes the comment with the given id
    //    case "api" :: "blog" :: AsBlogEntry(entry) :: "comment" :: AsComment(comment) :: Nil XmlDelete _ if entry.comments.contains(comment) => removeComment(comment, entry)
  }

  serveJx {
    // GET /api/tour lists all tours and stages
    case Get("api" :: "tour" :: Nil, _) => Full(listTours)

    // GET /api/blog lists all entries
    case Get("api" :: "blog" :: Nil, _) => Full(listEntries)

    // GET /api/blog/<valid id>/comment
    case Get("api" :: "blog" :: AsBlogEntry(entry) :: "comment" :: Nil, _) => Full(listComments(entry))

    // GET /api/blog/<valid id> returns entry with given ID
    case Get("api" :: "blog" :: AsBlogEntry(entry) :: Nil, _) => Full(entry)

    // GET /api/blog/<valid id>/comment/<valid id> returns comment with given ID
    case Get("api" :: "blog" :: AsBlogEntry(entry) :: "comment" :: AsComment(comment) :: Nil, _) if entry.comments.contains(comment) => Full(comment)
  }
}