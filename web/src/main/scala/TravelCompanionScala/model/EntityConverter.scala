package TravelCompanionScala.model


import scala.collection.JavaConversions._
import net.liftweb.json.{DefaultFormats, Xml}
import java.util.Date
import xml.{Utility, Elem, Node, NodeSeq}
import net.liftweb.util.Helpers._
import net.liftweb.json.JsonAST.JValue

/**
 * Created by IntelliJ IDEA.
 * User: rmuri
 * Date: 18.05.2010
 * Time: 09:35:51
 * To change this template use File | Settings | File Templates.
 */

// implicit def serializeEntity (o: Object) = new EntityConverter (o)


class EntityConverter(o: Object) {
  def entryFromXml: BlogEntry = {
    o match {
      case elem: Elem => {
        val entry = new BlogEntry
        entry.id = toLong((elem \ "id" text))
        entry.title = (elem \ "title" text)
        entry.content = (elem \ "content" text)
        entry.published = toBoolean(elem \ "published" text)
        entry.tour = Model.find[Tour](classOf[Tour], toLong((elem \ "tour" text))).getOrElse(null)
        entry.owner = Model.find[Member](classOf[Member], toLong((elem \ "owner" text))).getOrElse(null)
        entry.lastUpdated = new Date()
        entry.comments.addAll(Model.createNamedQuery[Comment]("findCommentsByEntry").setParams("entry" -> entry).findAll.toList)
        entry
      }
    }
  }

  def commentFromXml: Comment = {
    o match {
      case elem: Elem => {
        val comment = new Comment
        comment.id = toLong((elem \ "id" text))
        comment.content = (elem \ "content" text)
        comment.member = Model.find[Member](classOf[Member], toLong((elem \ "member" text))).getOrElse(null)
        comment.blogEntry = Model.find[BlogEntry](classOf[BlogEntry], toLong((elem \ "blogEntry" text))).getOrElse(null)

        comment.dateCreated = new Date()

        comment
      }
    }
  }

  def toXml: Node = {
    o match {
      case e: BlogEntry => {
        Utility.trim(
          <BlogEntry>
            <id>
              {e.id}
            </id>
            <title>
              {e.title}
            </title>
            <content>
              {e.content}
            </content>
            <lastUpdated>
              {e.lastUpdated}
            </lastUpdated>
            <published>
              {e.published}
            </published>
            <tour>
              {if (e.tour != null) e.tour.id else "null"}
            </tour>
            <owner>
              {if (e.owner != null) e.owner.id else "null"}
            </owner>
            <comments>
              {e.comments.flatMap(c => new EntityConverter(c).toXml)}
            </comments>
          </BlogEntry>)
      }
      case c: Comment => {
        Utility.trim(
          <comment>
            <id>
              {c.id}
            </id>
            <content>
              {c.content}
            </content>
            <member>
              {c.member.id}
            </member>
            <dateCreated>
              {c.dateCreated}
            </dateCreated>
            <blogEntry>
              {c.blogEntry.id}
            </blogEntry>
          </comment>)
      }
      case e: Tour => {
        Utility.trim(
          <Tour>
            <id>
              {e.id}
            </id>
            <name>
              {e.name}
            </name>
            <description>
              {e.description}
            </description>
            <blogs>
              {e.blogEntries.flatMap(b => new EntityConverter(b).toXml)}
            </blogs>
            <stages>
              {e.stages.flatMap(s => new EntityConverter(s).toXml)}
            </stages>
          </Tour>)
      }
      case e: Stage => {
        Utility.trim(
          <Stage>
            <id>
              {e.id}
            </id>
            <name>
              {e.name}
            </name>
            <description>
              {e.description}
            </description>
            <startdate>
              {e.startdate}
            </startdate>
            <destination>
              {new EntityConverter(e.destination).toXml}
            </destination>
          </Stage>)
      }
      case e: Location => {
        Utility.trim(
          <Location>
            <id>
              {e.id}
            </id>
            <name>
              {e.name}
            </name>
            <adminname>
              {e.adminname}
            </adminname>
            <admincode>
              {e.admincode}
            </admincode>
            <countryname>
              {e.countryname}
            </countryname>
            <countrycode>
              {e.countrycode}
            </countrycode>
            <geonameid>
              {e.geonameid}
            </geonameid>
            <lat>
              {e.lat}
            </lat>
            <lng>
              {e.lng}
            </lng>
            <population>
              {e.population}
            </population>
          </Location>)
      }
    }
  }

  //jquery Grid-Plugin needs special XML-Markup
  def toGrid: Node = {

    o match {
      case e: Tour => {
        Utility.trim(
          <row id={e.id.toString }>
            <cell>
              { e.id }
            </cell>
            <tour:name/>
            <cell>
              {e.description}
            </cell>
          </row>)
      }
    }
  }

  def toJson = {
    o match {
      case e: BlogEntry => Xml.toJson(new EntityConverter(e).toXml)
      case c: Comment => Xml.toJson(new EntityConverter(c).toXml)
    }
  }
}

object EntityConverter {
  implicit def serializeEntity(o: Object) = new EntityConverter(o)
}