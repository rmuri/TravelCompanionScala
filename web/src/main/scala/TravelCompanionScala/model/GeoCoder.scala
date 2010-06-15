package TravelCompanionScala.model

import java.net.{URL, URLEncoder}
import collection.mutable.Queue
import java.io.{IOException}
import net.liftweb.http.S
import xml.{Elem, XML}

/**
 * The GeoCoder Object provides a service for finding Locations by Name.
 *
 * @author Daniel Hobi
 *
 */

object GeoCoder {
  val wsAdress: String = "http://ws.geonames.org/search?"
  var locations: Seq[Location] = List()

  /**
   * Returns a sequence of location objects
   */
  def getCurrentLocations(): Seq[Location] = {
    locations
  }

  /**
   * Returns a sequence of location objects
   * @param in a string of a location name
   */
  def findLocationsByName(locationName: String): Seq[Location] = {
    var root: Elem = getElement(locationName)
    var results = new Queue[Location]()
    root \ "geoname" foreach {
      (geoname) =>
        {
          val loc = new Location()
          loc.admincode = (geoname \ "adminCode1" text)
          loc.adminname = (geoname \ "adminName1" text)
          loc.countrycode = (geoname \ "countryCode" text)
          loc.countryname = (geoname \ "countryName" text)
          loc.geonameid = (geoname \ "geonameId" text)
          loc.lat = (geoname \ "lat" text)
          loc.lng = (geoname \ "lng" text)
          loc.name = (geoname \ "name" text)
          loc.population = (geoname \ "population" text)
          results.enqueue(loc)
        }
    }
    locations = results
    locations
  }

  /**
   * Connects to the geonames.org service and returns the response in XML
   */
  private def getElement(locationName: String): Elem = {
    val query: String = "name_equals=" + URLEncoder.encode(locationName, "UTF-8") + "&fclass=P&style=FULL"
    val url = new URL(wsAdress + "" + query)
    try {
      val conn = url.openConnection
      XML.load(conn.getInputStream)
    } catch {
      case e: IOException => S.error("Verbindung zu geonames.org fehlgeschlagen.")
      XML.loadString("<geoname />")
    }

  }


}