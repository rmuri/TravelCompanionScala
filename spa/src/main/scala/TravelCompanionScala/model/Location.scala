package TravelCompanionScala {
package model {

import javax.persistence._
import _root_.java.util._

/**
 * Created by IntelliJ IDEA.
 * User: dhobi
 * Date: 08.04.2010
 * Time: 16:04:31
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "Location")
class Location {
  @Id
  @Version
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column(name = "admin_code")
  var admincode: String = ""

  @Column(name = "admin_name")
  var adminname: String = ""

  @Column(name = "country_code")
  var countrycode: String = ""

  @Column(name = "country_name")
  var countryname: String = ""

  @Column(name = "geo_name_id")
  var geonameid: String = ""

  @Column(name = "lat")
  var lat: String = ""

  @Column(name = "lng")
  var lng: String = ""

  @Column(name = "name")
  var name: String = ""

  @Column(name = "population")
  var population: String = ""

}

}

}