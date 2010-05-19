package TravelCompanionScala.api

import net.liftweb.http.rest.RestHelper
import net.liftweb.util.Helpers.toLong
import TravelCompanionScala.model.EntityConverter._
import net.liftweb.common.{Box}
import xml.{Node, Elem, NodeSeq, UnprefixedAttribute}
import TravelCompanionScala.model.{validator, BlogEntry, Model}
import net.liftweb.http.{PutRequest, XmlResponse, Req}

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

  def listEntries = {
    <BlogEntries>
      {Model.findAll[BlogEntry]("findAllEntries").flatMap(e => e.toXml)}
    </BlogEntries>
  }

  def saveBlogEntry(xml: Node) = {
    val e = xml.entryFromXml
    println("saveEntry")
    if (validator.is_valid_entity_?(e)) {
      //      Model.mergeAndFlush(e)
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

  serve {
    // GET /api/blog lists all entries
    case XmlGet("api" :: "blog" :: Nil, _) => listEntries
    // POST /api/blog creates new entry with xml data from request body
    case XmlPost("api" :: "blog" :: Nil, xml -> _) => createBlogEntry(xml)
    // GET /api/blog/<valid id> returns entry with given ID
    case XmlGet("api" :: "blog" :: AsBlogEntry(entry) :: Nil, _) => entry.toXml
    // PUT /api/blog/<valid id> updates the respective entry with xml data from request body
    //    case XmlPut("api" :: "blog" :: AsBlogEntry(entry) :: Nil, xml -> _) => saveBlogEntry(xml)
    //    case r@Req("api" :: "blog" :: AsBlogEntry(entry) :: Nil, "xml", PutRequest) => saveBlogEntry(r.xml.open_!)
  }
}