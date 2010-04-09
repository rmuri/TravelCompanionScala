package TravelCompanionScala.snippet

import xml.NodeSeq
import net.liftweb.util.Helpers
import net.liftweb._

import util._
import Helpers._

/**
 * Created by IntelliJ IDEA.
 * User: Ralf Muri
 * Date: 09.04.2010
 * Time: 17:14:14
 * To change this template use File | Settings | File Templates.
 */

class Tour(var name: String, var description: String)

class TourSnippet {
  var ownTours: List[Tour] = new Tour("Amsterdam", "bli bla blub") :: new Tour("Philippinen", "blindtext blabla") :: Nil


  def listOwnTours(html: NodeSeq): NodeSeq = {
    ownTours.flatMap(tour => bind("tour", html, "name" -> tour.name, "description" -> tour.description))
  }

}