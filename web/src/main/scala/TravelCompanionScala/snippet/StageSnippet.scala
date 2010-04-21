package TravelCompanionScala.snippet

import _root_.scala.xml.{NodeSeq, Text}

import _root_.net.liftweb._
import common.{Box, Empty}
import http._
import S._
import util._
import Helpers._

import TravelCompanionScala.model._

/**
 * Created by IntelliJ IDEA.
 * User: Ralf Muri
 * Date: 19.04.2010
 * Time: 08:55:22
 * To change this template use File | Settings | File Templates.
 */

object tourParam extends RequestVar[Box[Tour]](Empty)


class StageSnippet {
  // Set up a requestVar to track the STAGE object for edits and adds
  object stageVar extends RequestVar[Stage](new Stage())
  def stage = stageVar.is

  def editStage(html: NodeSeq): NodeSeq = {
    def doEdit() = {
      Model.mergeAndFlush(stage)
      S.redirectTo("/tour/list") //??
    }

    val currentStage = stage


    bind("stage", html,
      "title" -> SHtml.text(currentStage.name, currentStage.name = _),
      "description" -> SHtml.textarea(currentStage.description, currentStage.description = _),
      "dateOf" -%> SHtml.text(Util.slashDate.format(currentStage.startdate), (p:String) => currentStage.startdate = Util.slashDate.parse(p)),
      "submit" -> SHtml.submit("Speichern", () => {stageVar(currentStage); doEdit}))
  }
}