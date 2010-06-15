package TravelCompanionScala.snippet

import xml.NodeSeq
import TravelCompanionScala.widget.Gauge

/**
 * The TravelDistance snippet is responsible for rendering the Gauge widget.
 *
 * Further Information on creating a Widget can be found on:
 * - http://www.assembla.com/wiki/show/liftweb/Widgets
 * - Technologiestudium (github link) Chapter 4.5 [German]
 *
 * Known issues:
 * - distance has a fixed and not a dynamically calculated value
 * @author Daniel Hobi
 *
 */

class TravelDistance {
  def render(html: NodeSeq): NodeSeq = {
    val distance = 70
    Gauge(distance)
  }
}