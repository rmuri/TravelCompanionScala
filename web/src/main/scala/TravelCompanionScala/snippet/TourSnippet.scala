package TravelCompanionScala.snippet

import net.liftweb.util.Helpers
import net.liftweb._

import http.S
import util._
import Helpers._
import xml.{Text, NodeSeq}

/**
 * Created by IntelliJ IDEA.
 * User: Ralf Muri
 * Date: 09.04.2010
 * Time: 17:14:14
 * To change this template use File | Settings | File Templates.
 */

class Tour(var id: Int, var name: String, var creator: String, var description: String)

object TourEnum extends Enumeration {
  val ALL_TOURS = Value("AllTours")
  val OWN_TOURS = Value("OwnTours")
  val OTHERS_TOURS = Value("OthersTours")
}

class TourSnippet {
  var listOwnTours: List[Tour] = new Tour(1, "Amsterdam", "Ralf", "bli bla blub") :: new Tour(2, "Philippinen", "Ralf", "blindtext blabla") :: Nil
  var ListOthersTours: List[Tour] = new Tour(3, "Canada", "Daniel", "howdey partner...") :: Nil
  var ListAllTours: List[Tour] = listOwnTours ::: ListOthersTours


  def listTours(html: NodeSeq): NodeSeq = {
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
      "creator" -> tour.creator,
      FuncAttrBindParam("view_href", _ => Text("view/" + tour.id), "href")))
  }

}