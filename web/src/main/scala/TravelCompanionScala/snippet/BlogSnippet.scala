package TravelCompanionScala.snippet

import _root_.scala.xml.{NodeSeq, Text}

import _root_.net.liftweb._
import http._
import S._
import util._
import Helpers._

import TravelCompanionScala.model._

/**
 * Created by IntelliJ IDEA.
 * User: Ralf Muri
 * Date: 19.04.2010
 * Time: 09:02:05
 * To change this template use File | Settings | File Templates.
 */

class BlogSnippet {
  // Set up a requestVar to track the STAGE object for edits and adds
  object blogEntryVar extends RequestVar(new BlogEntry())
  def blogEntry = blogEntryVar.is

  
}