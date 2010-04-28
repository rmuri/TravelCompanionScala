package TravelCompanionScala.model

import java.net.{URLConnection, URL, URLEncoder}
import java.io.{BufferedInputStream, DataInputStream}
import xml.{Elem, XML}
import collection.mutable.Queue

/**
 * Created by IntelliJ IDEA.
 * User: dhobi
 * Date: 27.04.2010
 * Time: 14:23:57
 * To change this template use File | Settings | File Templates.
 */

object GeoCoder {
  val wsAdress: String = "http://ws.geonames.org/search?"
  var locations : Seq[Location] = _

  def getCurrentLocations(): Seq[Location] = {
    locations
  }

  def findLocationsByName(locationName: String): Seq[Location] = {
    var root: Elem = getElement(locationName)
    var results = new Queue[Location]()    
    root \ "geoname" foreach {(geoname) =>
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

  def getElement(locationName: String): Elem = {
    val query: String = "name_equals=" + URLEncoder.encode(locationName,"UTF-8") + "&fclass=P&style=FULL"
    val url = new URL(wsAdress + "" + query)
    val conn = url.openConnection
    XML.load(conn.getInputStream)
  }


}