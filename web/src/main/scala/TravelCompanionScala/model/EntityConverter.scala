package TravelCompanionScala.model

import scala.collection.JavaConversions._
import net.liftweb.json.Xml
import java.util.Date
import xml.{Utility, Elem, Node}
import net.liftweb.util.Helpers._

/**
 * The EntityConverter class implements the conversion between blog entries and comments to and from xml.
 * The class is intendend to being used by implicit conversions. For this purpose the EntityConveter companion
 * objects exists which makes the implicit definition available to classes having an import to it. So to
 * use these implicit conversions, the following import is necessary:
 *
 * import TravelCompanionScala.model.EntityConverter._
 *
 * Further information on implicit conversion can be found on:
 * - http://www.codecommit.com/blog/scala/scala-for-java-refugees-part-6
 *
 * Known Issues:
 * - The converter methods are made available on class Object. A better solution would be a common superclass for
 *    all the Entity classes. Since such a super class does not exist, Object is used to attach the converter
 *    methods to.
 * - The class is missing general exception handling (invalid xml, parsing, etc.)
 *
 * @author Ralf Muri
 */
object EntityConverter {
  // implicit definition to add methods of EntityConverter to instances of the class Object
  implicit def serializeEntity(o: Object) = new EntityConverter(o)
}

/**
 * Converter logic
 * @param o Is the Object on which the method is called
 */
class EntityConverter(o: Object) {
  /**
   * Creates a blog entry from a xml representation
   */
  def entryFromXml: BlogEntry = {
    // If the method is called on a instance of class Elem (xml data) a entry can be created
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
        // return the created blog entry
        entry
      }
    }
  }

  /**
   * Creates a comment from a xml representation
   */
  def commentFromXml: Comment = {
    // If the method is called on a instance of class Elem (xml data) a comment can be created
    o match {
      case elem: Elem => {
        val comment = new Comment
        comment.id = toLong((elem \ "id" text))
        comment.content = (elem \ "content" text)
        comment.member = Model.find[Member](classOf[Member], toLong((elem \ "member" text))).getOrElse(null)
        comment.blogEntry = Model.find[BlogEntry](classOf[BlogEntry], toLong((elem \ "blogEntry" text))).getOrElse(null)
        comment.dateCreated = new Date()
        // return the created comment
        comment
      }
    }
  }

  /**
   * Converts a entity to xml
   */
  def toXml: Node = {
    // If the method is called on a instance of domain entity, the instance can be converted
    o match {
    // converting logic for BlogEntry
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
      // converting logic for Comment
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
      // converting logic for Tour
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
      // converting logic for Stage
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
      // converting logic for Location
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
          <row id={e.id.toString}>
            <cell>
              {e.id}
            </cell>
            <tour:name/>
            <cell>
              {e.description}
            </cell>
          </row>)
      }
    }
  }

  /**
   * Converts a entity into json using the toJson method on the Xml class
   */
  def toJson = {
    // converting logic is defined for BlogEntry and Comment
    o match {
      case e: BlogEntry => Xml.toJson(new EntityConverter(e).toXml)
      case c: Comment => Xml.toJson(new EntityConverter(c).toXml)
    }
  }
}