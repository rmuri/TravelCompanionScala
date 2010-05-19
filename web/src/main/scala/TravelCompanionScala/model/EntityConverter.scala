package TravelCompanionScala.model


import scala.collection.JavaConversions._
import net.liftweb.json.{DefaultFormats, Xml}
import java.util.Date
import xml.{Utility, Elem, Node, NodeSeq}
import net.liftweb.util.Helpers._

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