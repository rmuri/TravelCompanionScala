package TravelCompanionScala.api

/**
 * Created by IntelliJ IDEA.
 * User: pmei
 * Date: 04.06.2010
 * Time: 11:25:54
 * Package: TravelCompanionScala.api
 * Class: PCData
 */

import xml.{Unparsed, Elem}

//http://scala-programming-language.1934581.n4.nabble.com/scala-Ampersands-are-escaped-inside-CDATA-literals-tp2072719p2073464.html
object PCData {
  def apply(anElem: Elem): Elem = <cell>{Unparsed("<![CDATA["+anElem+"]]>")}</cell>
}
