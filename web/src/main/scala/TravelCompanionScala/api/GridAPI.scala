package TravelCompanionScala.api

import TravelCompanionScala.model.EntityConverter._
import net.liftweb.common._
import TravelCompanionScala.model.{Model, Tour}
import net.liftweb.util.Helpers._
import net.liftweb.http.{SessionVar, RequestVar, SHtml, S}
import net.liftweb.http.rest.{XmlSelect, JsonSelect, RestHelper}
import net.liftweb.json.Xml
import xml.{Elem, Text, Node}
import collection.mutable.Buffer
import TravelCompanionScala.snippet.tourVarSession

/**
 * GridAPI, serves the special jqGrid Table. This component was chosen because of the paging
 *
 * @author Philipp Meier
 */

object tourVar extends RequestVar[Tour](new Tour())

object GridAPI extends RestHelper {

  //This var is needed  for the construction of the xml
  var tourData = Buffer[Node]()

  //a more performant solution with direct sql query
  val resultOption: Option[java.lang.Long] = Model.createNativeQuery("select count(*) from tours").findOne
  val result = resultOption.getOrElse(new java.lang.Long(100))
  val tourSize = toInt(result)

  var page = 1;
  var rows = 10;


  def getData = {
    for{
      Spage <- S.param("page") ?~ "page parameter missing" ~> 400
      Srows <- S.param("rows") ?~ "row parameter missing" ~> 400
      //the sort order
      Ssord <- S.param("sord") ?~ "row parameter missing" ~> 400
      //which column: ID, Name, Description
      Ssidx <- S.param("sidx") ?~ "row parameter missing" ~> 400

    } yield {
      page = Spage.toInt - 1
      rows = Srows.toInt

      //Not posssible to create typesafe query - because only in Java with Java Entity-Types
      //see http://www.ibm.com/developerworks/java/library/j-typesafejpa
      var queryString = "SELECT t from Tour t order by"

      Ssidx match {
        case "ID" => queryString = queryString.concat(" t.id")
        case "Name" => queryString = queryString.concat(" t.name")
        case "Description" => queryString = queryString.concat(" t.description")
        case _ => queryString = queryString.concat(" t.id")
      }

      Ssord match {
        case "asc" => queryString = queryString.concat(" ASC")
        case "desc" => queryString = queryString.concat(" DESC")
        case _ => queryString = queryString.concat(" ASC")
      }


      val customQuery = Model.createQuery[Tour](queryString)
      //Not all DBs support FETCH, FIRST, JPA might behave strange when changing the DB
      //see http://troels.arvin.dk/db/rdbms/#select-limit
      customQuery.setFirstResult(page * rows)
      customQuery.setMaxResults(rows)


      var tourData2 = customQuery.getResultList()
      tourData = tourData2.flatMap(tour => bind("tour", tour.toGrid, "name" -> PCData(SHtml.link("view", () => tourVarSession(tour), Text(tour.name)))))
    }
  }

  def listTours = {
    getData
    <rows>
      <page>
        {page + 1}
      </page>
      <total>
        {tourSize / rows}
      </total>
      <records>
        {rows}
      </records>{tourData}
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