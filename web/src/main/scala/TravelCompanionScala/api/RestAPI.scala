package TravelCompanionScala.api

import net.liftweb.http.rest.RestHelper
import net.liftweb.util.Helpers.toLong
import TravelCompanionScala.model.EntityConverter._
import net.liftweb.http._
import net.liftweb.http.rest._
import net.liftweb.common._
import xml.{Node, Elem, NodeSeq, UnprefixedAttribute}
import scala.collection.JavaConversions._
import TravelCompanionScala.controller._
import TravelCompanionScala.model.{Comment, validator, BlogEntry, Model}
import net.liftweb.json.Xml

/**
 * Created by IntelliJ IDEA.
 * User: rmuri
 * Date: 18.05.2010
 * Time: 10:05:32
 * To change this template use File | Settings | File Templates.
 */


object RestAPI extends RestHelper {
  object AsBlogEntry {
    def unapply(in: String): Option[BlogEntry] = {
      Model.find(classOf[BlogEntry], toLong(in))
    }
  }
  object AsComment {
    def unapply(in: String): Option[Comment] = {
      //      Model.createNamedQuery[Comment]("findCommentByEntry", "id" -> toLong(in), "entry" -> entry).findOne
      Model.find(classOf[Comment], toLong(in))
    }
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
    println("saveEntry")
    if (validator.is_valid_entity_?(e)) {
      val merged = Model.mergeAndFlush(e)
      BlogCache.cache ! EditEntry(merged)
      <succesful>yes</succesful>
    } else {
      <succesful>no</succesful>
    }
  }

  def createBlogEntry(xml: Node) = {
    val e = xml.entryFromXml
    println("new Entry")
    println(e)
    <succesful>yes</succesful>
  }

  implicit def cvt: JxCvtPF[Object] = {
    case (JsonSelect, n: Elem, _) => Xml.toJson(n)
    case (XmlSelect, n: Elem, _) => n
    case (JsonSelect, o: Object, _) => o.toJson
    case (XmlSelect, o: Object, _) => o.toXml
  }

  serveJx {
    // GET /api/blog lists all entries
    case Get("api" :: "blog" :: Nil, _) => Full(listEntries)

    // GET /api/blog/<valid id>/comment
    case Get("api" :: "blog" :: AsBlogEntry(entry) :: "comment" :: Nil, _) => Full(listComments(entry))

    //    POST /api/blog creates new entry with xml data from request body
    //    case "api" :: "blog" :: Nil XmlPost xml -> _ => createBlogEntry(xml)

    //    GET /api/blog/<valid id> returns entry with given ID
    case Get("api" :: "blog" :: AsBlogEntry(entry) :: Nil, _) => Full(entry)

    //    PUT /api/blog/<valid id> updates the respective entry with xml data from request body
    //    case "api" :: "blog" :: AsBlogEntry(entry) :: Nil XmlPut xml -> _ => saveBlogEntry(xml)

    // GET /api/blog/<valid id>/comment/<valid id> returns comment with given ID
    case Get("api" :: "blog" :: AsBlogEntry(entry) :: "comment" :: AsComment(comment) :: Nil, _) => Full(comment)
  }
}