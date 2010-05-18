package TravelCompanionScala.api

import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST.JString
import net.liftweb.http.{XmlResponse, Req}
import TravelCompanionScala.model.{BlogEntry, Model}
import TravelCompanionScala.model.EntityConverter._
import net.liftweb.util.BasicTypesHelpers._
import net.liftweb.http._
import net.liftweb.http.rest._
import net.liftweb.common.{Box, Empty, Full}
import xml.{Elem, NodeSeq, UnprefixedAttribute}

/**
 * Created by IntelliJ IDEA.
 * User: rmuri
 * Date: 18.05.2010
 * Time: 10:05:32
 * To change this template use File | Settings | File Templates.
 */


object RestAPI extends RestHelper {
  serve {
    //    case "api" :: "blog" :: Nil XmlGet _ => <b>hello</b>
    //    case "api" :: "blog" :: Nil JsonGet _ => JString("hello")
    case "api" :: "blog" :: AsLong(entryid) :: _ XmlGet _ => {
      println(Model.getReference(classOf[BlogEntry], entryid).toXml);
      <error>error with id
        {entryid}
      </error>
    }
    //    case "api" :: "blog" :: AsLong(entryid) :: _ JsonGet _ => Model.getReference(classOf[BlogEntry], entryid).toJson
    //    case "api" :: "blog" :: entryid :: "comment" :: _ XmlGet _ => <b>hello</b>
    case "api" :: "blog" :: _ XmlPut xml -> _ => {
      val e = xml.entryFromXml;
      println(xml)
      println(e.id)
      println(e.title)
      println(e.content)
      <succesful>yes</succesful>
    }
  }
}