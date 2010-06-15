package TravelCompanionScala.snippet

import _root_.scala.xml.{NodeSeq, Text}

import _root_.net.liftweb._
import common.{Box}
import http._

import js.JsCmds._
import js.JE.{JsRaw, JsArray}
import js.JsCmds.JsCrVar
import js.{JsObj, JE, JsCmd}
import S._
import util._
import Helpers._
import JE._
import TravelCompanionScala.model._
import java.text.SimpleDateFormat
import widgets.autocomplete.AutoComplete
import TravelCompanionScala.api.tourVarFromAPI

/**
 *  Set up a requestVar to track the STAGE object for edits and adds
 */
object stageVar extends RequestVar[Stage](new Stage())


/**
 * The StageSnippet is responsible for the stage view
 *
 * @author Daniel Hobi
 */

class StageSnippet {
  lazy val slashDate = new SimpleDateFormat("dd.MM.yyyy")

  def stage = stageVar.is

  /**
   *  This method edits a stage
   */
  def editStage(html: NodeSeq): NodeSeq = {
    val currentTour = tourVar.is
    val currentStage = stage
    stage.tour = tourVar.is

    /**
     *  Finally edits the stage object
     */
    def doEdit() = {
      if (validator.is_valid_entity_?(stage)) {
        Model.mergeAndFlush(stage)
        val currentTour = tourVar.is
        S.redirectTo("/tour/view", () => tourVar(currentTour))
      }
    }

    /**
     *  Gets all locations for a location name and checks if the location is available in the database
     */
    def setLocation(name: String, s: Stage) = {
      val geos: List[String] = name.split(",").toList.map(str => str.trim)
      var loc = GeoCoder.getCurrentLocations.find(
        loc => (geos.contains(loc.name) && geos.contains(loc.countryname))
        ).getOrElse(s.destination)

      loc = Model.createQuery[Location]("SELECT l from Location l where l.geonameid = :geonameid").setParams("geonameid" -> loc.geonameid).findOne.getOrElse(loc)

      s.destination = loc
    }

    if (currentStage.destination == null) {
      currentStage.destination = new Location
    }
    if (currentStage.startdate == null) {
      currentStage.startdate = TimeHelpers.now
    }

    /**
     *  Renders stage
     *  Specials:
     *  - AutoComplete Widget
     */
    bind("stage", html,
      "title" -> SHtml.text(currentStage.name, currentStage.name = _),
      "destination" -> AutoComplete(currentStage.destination.name, (current, limit) => {GeoCoder.findLocationsByName(current).map(loc => loc.name + ", " + loc.countryname)}, s => setLocation(s, currentStage)),
      "description" -> SHtml.textarea(currentStage.description, currentStage.description = _),
      "dateOf" -%> SHtml.text(slashDate.format(currentStage.startdate), (p: String) => currentStage.startdate = slashDate.parse(p)),
      "submit" -> SHtml.submit(S.?("save"), () => {stageVar(currentStage); tourVar(currentTour); doEdit}))
  }

  /**
   *  Renders stage
   */
  def viewStage(html: NodeSeq): NodeSeq = {
    stage.tour = tourVar.is
    bind("stage", html,
      "title" -> Text(stage.name),
      "date" -> Text(slashDate.format(stage.startdate)),
      "destination" -> Text(stage.destination.name + ", " + stage.destination.countryname))
  }

  /**
   *  Converts a stage to a JSON Object
   */
  def cvt(stage: Stage): JsObj = {
    JsObj(("title", stage.destination.name),
      ("lat", stage.destination.lat),
      ("lng", stage.destination.lng))
  }

  /**
   *  Called by renderGoogleMap
   *  Outputs the stages as JSON Objects and calls the generate() Javascript function
   */
  def ajaxFunc(stages: List[Stage]): JsCmd = {
    val locobj = stages.map(stage => cvt(stage))

    JsCrVar("locations", JsObj(("stages", JsArray(locobj: _*)))) & JsRaw("generate(locations)").cmd
  }

  /**
   *  Renders the Google Map
   */
  def renderGoogleMap(xhtml: NodeSeq): NodeSeq = {
    val currentTour = tourVar.is
    val maptype = S.attr("type").map(_.toString) openOr "SINGLE"
    var stages: List[Stage] = List()

    if (maptype.equals("ALL")) {
      stages = Model.createNamedQuery[Stage]("findStagesByTour").setParams("tour" -> currentTour).findAll.toList
    } else {
      stages = List(stage)
    }

    (<head>
      {Script(OnLoad(ajaxFunc(stages)))}
    </head>)
  }

  /**
   *  Deletes a stage
   */
  def doRemove() {
    val s = Model.merge(stage)
    Model.remove(s)
    val currentTour = tourVar.is
    S.redirectTo("/tour/view", () => tourVar(currentTour))
  }

  /**
   *  Shows all stages from tour
   */
  def showStagesFromTour(html: NodeSeq): NodeSeq = {
    var currentTour = tourVar.is

    if (currentTour.id == 0) {
      currentTour = tourVarFromAPI.is
    }

    val stages = Model.createNamedQuery[Stage]("findStagesByTour").setParams("tour" -> currentTour).findAll.toList

    stages.flatMap(stage => {
      stageVar(stage);
      bind("stage", html,
        "startdate" -> new SimpleDateFormat("dd.MM.yyyy").format(stage.startdate),
        "title" -> SHtml.link("/tour/stage/view", () => stageVar(stage), Text(stage.name)),
        "destination" -> stage.destination.name,
        "description" -> stage.description,
        "edit" -%> SHtml.link("/tour/stage/edit", () => {stageVar(stage); tourVar(currentTour); }, Text(?("edit"))),
        "remove" -%> SHtml.link("remove", () => {
          stageVar(stage);
          tourVar(currentTour);
          doRemove
        }, Text(?("remove"))))
    })

  }
}