package TravelCompanionScala.snippet

import xml.NodeSeq
import TravelCompanionScala.widget.Gauge

/**
 * Created by IntelliJ IDEA.
 * User: rmuri
 * Date: 04.05.2010
 * Time: 15:11:51
 * To change this template use File | Settings | File Templates.
 */

class TravelDistance {
  def render(html: NodeSeq): NodeSeq = {
    // calculate...
    val distance = 70
    (<canvas id="gauge"/> ++ Gauge(distance, "gauge"))
  }
}