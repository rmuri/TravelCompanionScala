package TravelCompanionScala.model


import scala.collection.JavaConversions._
import xml.{Elem, Node, NodeSeq}
import net.liftweb.json.{DefaultFormats, Xml}
import java.util.Date

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
        entry.id = (elem \ "id" text).trim.toLong
        entry.title = (elem \ "title" text).trim
        entry.content = (elem \ "content" text).trim
        entry.lastUpdated = new Date() /*new SimpleDateFormat("yyyy-mm-dd").parse((elem \ "lastUpdated" text))*/
        entry.published = (elem \ "published" text).trim.toBoolean
        entry.tour = Model.getReference(classOf[Tour], (elem \ "tour" text).trim.toLong)
        entry.owner = Model.getReference(classOf[Member], (elem \ "owner" text).trim.toLong)
        elem \ "comment" foreach {
          (comment) => {
            entry.comments.add(Model.getReference(classOf[Comment], (comment \ "id" text).trim.toLong))
          }
        }
        entry
      }
    }
  }

  def toXml: Elem = {
    o match {
      case e: BlogEntry => {
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
        </BlogEntry>
      }
      case c: Comment => {
        <comment>
          <id>
            {c.id}
          </id>
          <content>
            {c.content}
          </content>
          <member>
            {c.member}
          </member>
          <dateCreated>
            {c.dateCreated}
          </dateCreated>
          <blogEntry>
            {c.blogEntry.id}
          </blogEntry>
        </comment>
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