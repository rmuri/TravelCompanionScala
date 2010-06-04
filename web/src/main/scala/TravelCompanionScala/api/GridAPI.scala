package TravelCompanionScala.api

/**
 * Created by IntelliJ IDEA.
 * User: pmei
 * Date: 04.06.2010
 * Time: 11:26:26
 * Package: TravelCompanionScala.api
 * Class: GridAPI
 */

import TravelCompanionScala.model.EntityConverter._
import net.liftweb.common._
import TravelCompanionScala.model.{Model, Tour}
import net.liftweb.util.Helpers._
import net.liftweb.http.{SessionVar, RequestVar, SHtml, S}
import net.liftweb.http.rest.{XmlSelect, JsonSelect, RestHelper}
import net.liftweb.json.Xml
import xml.{Elem, Text}
object tourVar extends RequestVar[Tour](new Tour())

object GridAPI extends RestHelper {

  var tourData = Model.findAll[Tour]("findAllTours")flatMap(e => e.toGrid)
  val tourSize = Model.findAll[Tour]("findAllTours").size
  var page = 1;
  var rows = 10;

  //var foo = SHtml.link("view", () => tourVar(e), Text("Foobar"))

  def getData = {
     for {
      Spage <- S.param("page") ?~ "page parameter missing" ~> 400
      Srows <- S.param("rows") ?~ "row parameter missing" ~> 400
    } yield {
      page = Spage.toInt -1
      rows = Srows.toInt
      val query = Model.createNamedQuery[Tour]("findAllTours")
              .setFirstResult(page*rows).setMaxResults(rows)
      var tourData2 = query.findAll
      tourData = tourData2.flatMap(tour => bind("tour", tour.toGrid, "name" -> PCData(SHtml.link("view", () => tourVarFromAPI(tour), Text(tour.name)))))
    }
  }

  def listTours = {
    getData
    <rows>
      <page>{ page + 1 }</page>
      <total>{ tourSize/rows}</total>
      <records>{ rows }</records>
      { tourData }
    </rows>
  }

  serveJx {
    // GET /api/tour lists all entries
    case Get("gridapi" :: "tour" :: Nil, _) => Full(listTours)
  }

  implicit def cvt: JxCvtPF[Object] = {
    case (JsonSelect, n: Elem, _) => Xml.toJson(n)
    case (XmlSelect, n: Elem, _) => n
    case (JsonSelect, o: Object, _) => o.toJson
    case (XmlSelect, o: Object, _) => o.toXml
  }

}

object tourVarFromAPI extends SessionVar[Tour](new Tour())