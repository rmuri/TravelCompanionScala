package TravelCompanionScala.model


import scala.collection.JavaConversions._
import net.liftweb.json.Xml
import net.liftweb.http.js.JsObj
import xml.{Node, NodeSeq}

/**
 * Created by IntelliJ IDEA.
 * User: rmuri
 * Date: 18.05.2010
 * Time: 09:35:51
 * To change this template use File | Settings | File Templates.
 */

// implicit def serializeEntity (o: Object) = new EntityConverter (o)



class EntityConverter(o: Object) {
  def toXml: NodeSeq = {
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
            {e.tour.id}
          </tour>
          <owner>
            {e.owner.id}
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