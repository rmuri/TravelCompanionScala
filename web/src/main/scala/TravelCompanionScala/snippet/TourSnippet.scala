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
  val ALL_TOURS = Value("AllTours")
  val OWN_TOURS = Value("OwnTours")
  val OTHERS_TOURS = Value("OthersTours")
}

class TourSnippet {
  var listOwnTours: List[Tour] = Model.createNamedQuery[Tour]("findAllTours").getResultList().toList
  var ListOthersTours: List[Tour] = Model.createNamedQuery[Tour]("findAllTours").getResultList().toList
  var ListAllTours = listOwnTours ::: ListOthersTours

  def viewTour(html: NodeSeq): NodeSeq = {
    var id = S.param("id").map(_.toInt) openOr 0
    val tour = ListAllTours.find((t) => t.id == id) getOrElse new Tour
    bind("tour", html, "name" -> tour.name, "description" -> tour.description)
  }

  def listTours(html: NodeSeq): NodeSeq = {
    println("count of db tours" + listOwnTours.size)

    val which = S.attr("which").map(_.toString) openOr "AllTours"
    var tours: List[Tour] = Nil
    TourEnum.withName(which) match {
      case TourEnum.OWN_TOURS => tours = listOwnTours
      case TourEnum.OTHERS_TOURS => tours = ListOthersTours
      case _ => tours = ListAllTours
    }
    tours.flatMap(tour => bind("tour", html,
      "name" -> tour.name,
      "description" -> tour.description,
      "creator" -> tour.owner.name,
      FuncAttrBindParam("view_href", _ => Text("view/" + tour.id), "href")))
  }

}

}
}