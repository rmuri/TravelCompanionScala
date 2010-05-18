package TravelCompanionScala.api

import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST.JString
import net.liftweb.http.{XmlResponse, Req}
import TravelCompanionScala.model.EntityConverter._
import net.liftweb.http._
import net.liftweb.http.rest._
import net.liftweb.common.{Box, Empty, Full}
import xml.{Node, Elem, NodeSeq, UnprefixedAttribute}
import TravelCompanionScala.model.{validator, BlogEntry, Model}
import scala.collection.JavaConversions._

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
      Model.find(classOf[BlogEntry], in.toLong)
    }
  }

  def listEntries = {
    <BlogEntries>
      {Model.findAll[BlogEntry]("findAllEntries").flatMap(e => e.toXml)}
    </BlogEntries>
  }

  def is_valid_Entry_?(toCheck: BlogEntry): Boolean = {
    val validationResult = validator.get.validate(toCheck)
    validationResult.foreach((e) => S.error(e.getPropertyPath + " " + e.getMessage))
    validationResult.isEmpty
  }

  def saveBlogEntry(xml: Node) = {
    val e = xml.entryFromXml
    if (validator.is_valid_entity_?(e)) {
      Model.mergeAndFlush(e)
      <succesful>yes</succesful>
    } else {
      <succesful>no</succesful>
    }
  }

  serve {
    case "api" :: "blog" :: AsBlogEntry(entry) :: _ XmlPut xml -> _ => saveBlogEntry(xml)
    case "api" :: "blog" :: AsBlogEntry(entry) :: _ XmlGet _ => entry.toXml
    //    case "api" :: "blog" :: Nil XmlPost xml -> _ => saveBlogEntry(xml)
    case "api" :: "blog" :: Nil XmlGet _ => listEntries
  }
}