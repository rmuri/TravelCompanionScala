package TravelCompanionScala {
package snippet {

import _root_.scala.xml.{NodeSeq, Text}

import _root_.net.liftweb._
import common.{Box}
import http._
import S._
import util._
import Helpers._

import TravelCompanionScala.model._
import api.tourVarFromAPI


/**
 *  Simple enumeration to differ between own and other tours
 */
object TourEnum extends Enumeration {
  val OWN_TOURS = Value("OwnTours")
  val OTHERS_TOURS = Value("OthersTours")
}

/**
 *  Set up a requestVar to track the tour object for edits and adds
 */
object tourVar extends RequestVar[Tour](new Tour())

/**
 * The TourSnippet is responsible for the whole tour view
 *
 * @author Daniel Hobi
 */

class TourSnippet {
  def tour = tourVar.is

  /**
   *  This method removes a tour and redirects the user to /tour/list afterwards
   */
  def doRemove() = {
    val t = Model.merge(tour)
    Model.remove(t)
    S.redirectTo("/tour/list")
  }

  /**
   *  This method shows a tour with the bind helper
   */
  def showTour(html: NodeSeq): NodeSeq = {
    var currentTour = tour

    //The requestVar is filled with a default tour, because Request comes from GridAPI via the sessionVar
    if (currentTour.id == 0) {
      currentTour = tourVarFromAPI.is
    }

    /**
     *  This method renders a tour with the given html parameter
     */
    bind("tour", html,
      "name" -> currentTour.name,
      "description" -> currentTour.description,
      "edit" -%> SHtml.link("edit", () => tourVar(currentTour), Text(?("tour.editTour"))),
      "newStage" -%> SHtml.link("stage/edit", () => tourVar(currentTour), Text(?("tour.newStage"))))
  }

  /**
   *  Utility methods for processing a submitted form
   */
  def is_valid_Tour_?(toCheck: Tour): Boolean =
    List((if (toCheck.name.length == 0) {S.error(S.?("tour.noName")); false} else true),
      (if (toCheck.owner == null) {S.error(S.?("tour.noOwner")); false} else true)).forall(_ == true)

  /**
   *  Edit a tour
   */
  def editTour(html: NodeSeq): NodeSeq = {

    /**
     *  Finally does the edit on the tour
     */
    def doEdit() = {
      if (is_valid_Tour_?(tour)) {
        Model.mergeAndFlush(tour)
        S.redirectTo("/tour/list")
      }
    }

    val currentTour = tour

    if (currentTour.owner == null) {
      currentTour.owner = UserManagement.currentUser
    }

    /**
     *  renders a tour to a submittable form
     *  doEdit() will be called on submit
     */
    bind("tour", html,
      "name" -> SHtml.text(currentTour.name, currentTour.name = _),
      "description" -> SHtml.textarea(currentTour.description, currentTour.description = _),
      "owner" -> SHtml.text(currentTour.owner.name, currentTour.owner.name = _),
      "submit" -> SHtml.submit(?("save"), () => {tourVar(currentTour); doEdit}))
  }

  /**
   *  Lists and renders all tours either of the owner or the others
   */
  def listTours(html: NodeSeq): NodeSeq = {
    val which = S.attr("which").map(_.toString) openOr "AllTours"
    tours(TourEnum.withName(which)).flatMap(tour => bind("tour", html,
      "name" -> SHtml.link("view", () => tourVar(tour), Text(tour.name)),
      "description" -> tour.description,
      "creator" -> tour.owner.name,
      "addStage" -> SHtml.link("stage/edit", () => tourVar(tour), Text(?("tour.addStage"))),
      "edit" -> SHtml.link("edit", () => tourVar(tour), Text(?("edit"))),
      "view" -> SHtml.link("view", () => tourVar(tour), Text(?("view"))),
      "remove" -> SHtml.link("remove", () => {tourVar(tour); doRemove}, Text(?("remove")))))
  }

  /**
   *  returns a list of tours depending TourEnum
   */
  private def tours(which: TourEnum.Value): List[Tour] = {
    which match {
      case TourEnum.OWN_TOURS => Model.createNamedQuery[Tour]("findTourByOwner").setParams("owner" -> UserManagement.currentUser).findAll.toList
      case TourEnum.OTHERS_TOURS => Model.createNamedQuery[Tour]("findTourByOthers").setParams("owner" -> UserManagement.currentUser).findAll.toList
    }
  }
}
}
}