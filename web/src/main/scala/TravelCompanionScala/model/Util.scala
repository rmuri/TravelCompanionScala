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

  

}