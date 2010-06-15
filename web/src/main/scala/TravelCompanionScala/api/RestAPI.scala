package TravelCompanionScala.api

import net.liftweb.http.rest.RestHelper
import net.liftweb.util.Helpers.toLong
import net.liftweb.http.rest._
import TravelCompanionScala.model.EntityConverter._
import net.liftweb.common._
import xml.{Node, Elem}
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
 * - Moderately complex pattern matching with extractors is causing problems in the current scala release.
 *    See open ticket for this bug: https://lampsvn.epfl.ch/trac/scala/ticket/1133
 *    This bug is causing problems in the serve method, not allowing to match all the necessary requests.
 *
 * @author Ralf Muri
 * @see RestHelper
 * @see EntityConverter
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
   * @param in the id of a comment
   */
  object AsComment {
    def unapply(in: String): Option[Comment] = {
      Model.find(classOf[Comment], toLong(in))
    }
  }

  /**
   * Returns a list of all tours in xml format sourrounded by the <Tour/> tag as parent.
   * Uses the implicit conversion toXml implemented by the EntitiyConverter class.
   */
  def listTours = {
    <Tours>
      {Model.findAll[Tour]("findAllTours").flatMap(e => e.toXml)}
    </Tours>
  }

  /**
   * Returns a list of all blog entries in xml format sourrounded by the <BlogEntries/> tag as parent.
   * Uses the implicit conversion toXml implemented by the EntitiyConverter class.
   */
  def listEntries = {
    <BlogEntries>
      {Model.findAll[BlogEntry]("findAllEntries").flatMap(e => e.toXml)}
    </BlogEntries>
  }

  /**
   * Returns a list of all comments by a blog entry in xml format sourrounded by the <Comments/> tag as parent.
   * Uses the implicit conversion toXml implemented by the EntitiyConverter class.
   * @param entry Blog entry to get comments from
   */
  def listComments(entry: BlogEntry) = {
    <Comments>
      {Model.findAll[Comment]("findCommentsByEntry", "entry" -> entry).flatMap(e => e.toXml)}
    </Comments>
  }

  /**
   * Saves a blog entry described by xml to the database.
   * Uses the implicit conversion entryFromXml implemented by the EntitiyConverter class
   * @param xml A blog entry in xml format
   */
  def saveBlogEntry(xml: Node) = {
    val e = xml.entryFromXml
    // proccess only if valid entry
    if (Validator.is_valid_entity_?(e)) {
      val merged = Model.mergeAndFlush(e)
      // fire the EditEntry event to the LiftActor and pass on the saved entry
      BlogCache.cache ! EditEntry(merged)
      // return the newly created entry as xml
      merged.toXml
    } else <error/>
  }

  /**
   * Creates a new comment described by xml and saves it to the database.
   * Uses the implicit conversion commentFromXml implemented by the EntityConverter class.
   * @param xml A comment in xml format
   * @param entry The blog entry where the comment belongs to
   */
  def createComment(xml: Node, entry: BlogEntry) = {
    val c = xml.commentFromXml
    c.blogEntry = entry
    // process only if valid entry
    if (Validator.is_valid_entity_?(c)) {
      // merge comment and entry to the database
      val mergedComment = Model.mergeAndFlush(c)
      val mergedEntry = Model.merge(entry)
      // entry needs to be refreshed so that the new comment will appear
      Model.refresh(mergedEntry)
      // fire the AddComment event to the LiftActor and pass on the new comment
      BlogCache.cache ! AddComment(mergedEntry)
      // return the newly created comment as xml
      mergedComment.toXml
    } else <error/>
  }

  /**
   * Removes a comment from a blog entry and the database.
   * @param c The comment to remove
   * @param e The blog entry which the comment to remove from
   */
  def removeComment(c: Comment, e: BlogEntry) = {
    val mergedc = Model.merge(c)
    // remove the comment from the database
    Model.removeAndFlush(mergedc)
    // fire the DeleteComment to the LiftActor and pass on the blog entry to update
    BlogCache.cache ! DeleteComment(e)
    // return the removed comment as xml
    mergedc.toXml
  }

  /**
   * Implicit definitions for handling xml or json for request/responses handled by the serverJx method.
   * More information on: http://www.assembla.com/wiki/show/liftweb/REST_Web_Services
   * Uses the implicit conversion toXml from the EntityConverter class
   */
  implicit def cvt: JxCvtPF[Object] = {
    case (JsonSelect, n: Elem, _) => Xml.toJson(n)
    case (XmlSelect, n: Elem, _) => n
    case (JsonSelect, o: Object, _) => o.toJson
    case (XmlSelect, o: Object, _) => o.toXml
  }

  /**
   * Serve method for REST request dispatching. The scala pattern matching and extractors are used to do
   * the right dispatching.
   */
  serve {
    // POST /api/blog creates new entry with xml data from request body
    case "api" :: "blog" :: Nil XmlPost xml -> _ => saveBlogEntry(xml)

    // PUT /api/blog/<valid id> updates the respective entry with xml data from request body
    case "api" :: "blog" :: AsBlogEntry(entry) :: Nil XmlPut xml -> _ => saveBlogEntry(xml)

    // POST /api/blog/<valid id>/comment creates a new comment on the respective entry
    case "api" :: "blog" :: AsBlogEntry(entry) :: "comment" :: Nil XmlPost xml -> _ => createComment(xml, entry)

    /**
     * Due to a bug in scala this request can't be served and is commented out.
     * Moderately complex pattern matching with extractors is causing problems in the current scala release.
     * See open ticket for this bug:
     * https://lampsvn.epfl.ch/trac/scala/ticket/1133
     */
    // DELETE /api/blog/<valid id>/comment/<valid id> removes the comment with the given id
    //    case "api" :: "blog" :: AsBlogEntry(entry) :: "comment" :: AsComment(comment) :: Nil XmlDelete _ if entry.comments.contains(comment) => removeComment(comment, entry)
  }

  /**
   * Serve method for REST request dispatching. Request and responses coming in and resulting from
   * this method are automatically converted from and in json/xml. The scala pattern matching and extractors
   * are used to do the right dispatching.
   */
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