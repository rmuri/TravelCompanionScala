package TravelCompanionScala.snippet

import xml.NodeSeq
import net.liftweb.http.S

/**
 * Created by IntelliJ IDEA.
 * User: dhobi
 * Date: 20.04.2010
 * Time: 08:52:59
 * To change this template use File | Settings | File Templates.
 */


class TableSorter {
  def render(xhtml: NodeSeq): NodeSeq = {
    val which = S.attr("for").map(_.toString) openOr ""
    println("Tablesorter for: " + which)
    net.liftweb.widgets.tablesorter.TableSorter("#" + which)
  }


}