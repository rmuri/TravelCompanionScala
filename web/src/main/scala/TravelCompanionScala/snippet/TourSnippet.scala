package TravelCompanionScala {
package snippet {

import _root_.scala.xml.{NodeSeq, Text}

import _root_.net.liftweb._
import http._
import S._
import common._
import util._
import Helpers._

import _root_.javax.persistence.{EntityExistsException, PersistenceException}
import TravelCompanionScala.model._
import Model._


/**
 * Created by IntelliJ IDEA.
 * User: Ralf Muri
 * Date: 09.04.2010
 * Time: 17:14:14
 * To change this template use File | Settings | File Templates.
 */


object TourEnum extends Enumeration {
  val OWN_TOURS = Value("OwnTours")
  val OTHERS_TOURS = Value("OthersTours")
}

class TourSnippet {
  def viewTour(html: NodeSeq): NodeSeq = {
    var id = S.param("id").map(_.toLong) openOr 0l
    val tour = Model.find(classOf[Tour], id).get
    bind("tour", html, "name" -> tour.name, "description" -> tour.description)
  }

  def listTours(html: NodeSeq): NodeSeq = {

    val which = S.attr("which").map(_.toString) openOr "AllTours"
    tours(TourEnum.withName(which)).flatMap(tour => bind("tour", html,
      "name" -> tour.name,
      "description" -> tour.description,
      "creator" -> tour.owner.name,
      FuncAttrBindParam("view_href", _ => Text("view/" + tour.id), "href")))
  }

  def tours(which: TourEnum.Value): List[Tour] = {
    val mid = UserManagement.currentUserId
    which match {
      case TourEnum.OWN_TOURS => Model.createQuery[Tour]("from Tour t where t.owner.id = :id").setParams("id" -> mid).findAll.toList
      case TourEnum.OTHERS_TOURS => return Model.createQuery[Tour]("from Tour t where t.owner.id != :id").setParams("id" -> mid).findAll.toList
    }
  }

}
}
}