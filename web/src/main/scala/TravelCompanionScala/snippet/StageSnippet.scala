package TravelCompanionScala.snippet

import _root_.scala.xml.{NodeSeq, Text}

import _root_.net.liftweb._
import common.{Box, Empty}
import http._
import jquery.JqSHtml
import js._
import js.JE.{JsRaw, JsArray}
import js.JsCmds.JsCrVar
import S._
import util._
import Helpers._

import TravelCompanionScala.model._
import java.text.SimpleDateFormat
import widgets.autocomplete.AutoComplete

/**
 * Created by IntelliJ IDEA.
 * User: Ralf Muri
 * Date: 19.04.2010
 * Time: 08:55:22
 * To change this template use File | Settings | File Templates.
 */

// Set up a requestVar to track the STAGE object for edits and adds
object stageVar extends RequestVar[Stage](new Stage())

class StageSnippet {
  def stage = stageVar.is

  def editStage(html: NodeSeq): NodeSeq = {
    val currentStage = stage

    def doEdit() = {
      Model.mergeAndFlush(stage)
      S.redirectTo("/tour/list")
    }

    def setLocation(name: String, s: Stage) = {
      val geos: List[String] = name.split(",").toList.map(str => str.trim)
      var loc = GeoCoder.getCurrentLocations.find(loc => (geos.contains(loc.name) && geos.contains(loc.countryname))).get
      s.destination = loc
    }

    stage.tour = tourVar.is
    bind("stage", html,
      "title" -> SHtml.text(currentStage.name, currentStage.name = _),
      "destination" -> AutoComplete("", (current, limit) => {GeoCoder.findLocationsByName(current).map(loc => loc.name + ", " + loc.countryname)}, s => setLocation(s, currentStage)),
      "description" -> SHtml.textarea(currentStage.description, currentStage.description = _),
      "dateOf" -%> SHtml.text(Util.slashDate.format(currentStage.startdate), (p: String) => currentStage.startdate = Util.slashDate.parse(p)),
      "submit" -> SHtml.submit("Speichern", () => {stageVar(currentStage); doEdit}))
  }


  def doRemove() {
    val s = Model.merge(stage)
    Model.remove(s)
    val currentTour = tourVar.is
    S.redirectTo("/tour/view", () => tourVar(currentTour))
  }

  def showStagesFromTour(html: NodeSeq): NodeSeq = {
    val currentTour = tourVar.is
    val stages = Model.createNamedQuery[Stage]("findStagesByTour").setParams("tour" -> currentTour).findAll.toList


    stages.flatMap(stage => {
      stageVar(stage);
      bind("stage", html,
        "startdate" -> new SimpleDateFormat("dd.MM.yyyy").format(stage.startdate),
        "title" -> SHtml.link("/tour/stage/view", () => stageVar(stage), Text(stage.name)),
        "destination" -> stage.destination.name,
        "description" -> stage.description,
        "edit" -%> SHtml.link("/tour/stage/edit", () => stageVar(stage), Text(?("edit"))),
        "remove" -%> SHtml.link("remove", () => {stageVar(stage); tourVar(currentTour); doRemove}, Text(?("remove"))))
    })

  }
}