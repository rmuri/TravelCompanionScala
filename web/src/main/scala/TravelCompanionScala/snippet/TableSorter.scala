package TravelCompanionScala.snippet

import xml.NodeSeq
import net.liftweb.http.S

/**
 * The TableSorter snippet is responsible for rendering the Tablesorter widget.
 *
 * Further Information on GUI Widgets and creating a Widget can be found on:
 * - http://www.assembla.com/wiki/show/liftweb/Widgets
 * - Technologiestudium (github link) Chapter 4.5 [German]
 *
 * @author Daniel Hobi
 *
 */

class TableSorter {

  /**
   * gets the id of the html table which should become sortable
   * and passes this value to the TableSorter widget
   */
  def render(xhtml: NodeSeq): NodeSeq = {
    val which = S.attr("for").map(_.toString) openOr ""
    net.liftweb.widgets.tablesorter.TableSorter("#" + which)
  }


}