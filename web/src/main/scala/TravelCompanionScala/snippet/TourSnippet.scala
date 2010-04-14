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
  var id = S.param("id").map(_.toLong) openOr 0l

  // Set up a requestVar to track the TOUR object for edits and adds
  object tourVar extends RequestVar(new Tour())
  def tour = tourVar.is

  def deleteTour(html: NodeSeq): NodeSeq = {

    def doRemove() = {
      Model.remove(tour)
      S.redirectTo("/tour/list")
    }

    val currentTour = tour

    bind("tour", html,
      "id" -> SHtml.hidden(() => tourVar(currentTour)),
      "name" -> tour.name,
      "description" -> tour.description,
      "submit" -> SHtml.submit("Delete", doRemove))
  }

  def viewTour(html: NodeSeq): NodeSeq = {
    val currentTour = tour
    bind("tour", html, "name" -> tour.name, "description" -> tour.description)
  }

  def editTourForm(html: NodeSeq, tour: Tour, saveAction: () => Any *): NodeSeq = {
    def submitHandler() = {
      val editedTour = Model.mergeAndFlush(tour)
      saveAction.foreach(_())
      S.redirectTo("/tour/view/" + editedTour.id)
    }
    bind("tour", html,
      "name" -> SHtml.text(tour.name, tour.name = _),
      "description" -> SHtml.textarea(tour.description, tour.description = _),
      "submit" -> SHtml.submit("Submit", submitHandler _))
  }

  def editTour(html: NodeSeq): NodeSeq = {
    def doEdit() = {
      println(tour.description)
      tourVar(Model.mergeAndFlush(tour))
      println(tour.description)
      S.redirectTo("/tour/list")
    }

    val currentTour = tour

    bind("tour", html,
      "id" -> SHtml.hidden(() => tourVar(currentTour)),
      "name" -> SHtml.text(tour.name, tour.name = _),
      "description" -> SHtml.textarea(tour.description, tour.description = _),
      "submit" -> SHtml.submit("Speichern", doEdit))
  }

  def createTour(html: NodeSeq): NodeSeq = {
    editTour(html)
  }

  def listTours(html: NodeSeq): NodeSeq = {
    val which = S.attr("which").map(_.toString) openOr "AllTours"
    tours(TourEnum.withName(which)).flatMap(tour => bind("tour", html,
      "name" -> tour.name,
      "description" -> tour.description,
      "creator" -> tour.owner.name,
      FuncAttrBindParam("create_href", _ => Text("create/" + tour.id), "href"),
      "edit" -> SHtml.link("edit", () => tourVar(tour), Text(?("Edit"))),
      "view" -> SHtml.link("view", () => tourVar(tour), Text(?("View"))),
      "remove" -> SHtml.link("remove", () => tourVar(tour), Text(?("Remove")))))
  }

  private def tours(which: TourEnum.Value): List[Tour] = {
    val mid = UserManagement.currentUserId
    which match {
      case TourEnum.OWN_TOURS => Model.createQuery[Tour]("from Tour t where t.owner.id = :id").setParams("id" -> mid).findAll.toList
      // case TourEnum.OWN_TOURS => scala.collection.JavaConversions.asBuffer(UserManagement.currentUser.tours).toList
      case TourEnum.OTHERS_TOURS => return Model.createQuery[Tour]("from Tour t where t.owner.id != :id").setParams("id" -> mid).findAll.toList
    }
  }

}
}
}