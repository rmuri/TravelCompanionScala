package TravelCompanionScala.model

import java.util.Date
import net.liftweb.common.{Empty, Full, Box}
import net.liftweb.http.S
import java.text.SimpleDateFormat

/**
 * Created by IntelliJ IDEA.
 * User: dhobi
 * Date: 21.04.2010
 * Time: 14:38:41
 * To change this template use File | Settings | File Templates.
 */

object Util {

  val noSlashDate = new SimpleDateFormat("ddMMyyyy")

  val slashDate = new SimpleDateFormat("dd.MM.yyyy")

  def splitEvery[A](as : List[A], n : Int) : List[List[A]] = as.splitAt(n) match {
    case (a, Nil) => a :: Nil
    case (a, b)   => a :: splitEvery(b, n)
  }

  def getIntParam(name : String, default : Int) : Int = {
    try {
      S.param(name).map(_.toInt) openOr default
    }
    catch {
      case e => default // Should log something in this case
    }
  }

  type DateConverter = String => Date

  def parseDate(value : String, converter : DateConverter) : Box[Date] =
    try {
      Full(converter(value))
    } catch {
      case e => Empty
    }

  def getDateParam(name : String, converter : DateConverter) : Box[Date] = {
    S.param(name).map(parseDate(_, converter)) openOr Empty
  }

}