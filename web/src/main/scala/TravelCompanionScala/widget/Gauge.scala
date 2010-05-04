package TravelCompanionScala.widget

import net.liftweb.http.{LiftRules, ResourceServer}

/**
 * Created by IntelliJ IDEA.
 * User: rmuri
 * Date: 04.05.2010
 * Time: 15:15:52
 * To change this template use File | Settings | File Templates.
 */

object Gauge {
  def apply(value: Int) = renderOnLoad(value)

  def init() {
    ResourceServer.allow({
      case "gauge" :: tail => true
    })
  }

  def renderOnLoad(value: Int) = {
    val onLoad = "jQuery(document).ready(function() { var g = new Gauge(); g.initialize(" + value + ", 'gauge'); });"
    <head>
      <script type="text/javascript" src={"/" + LiftRules.resourceServerPath + "/gauge/gauge.js"}></script>
      <script type="text/javascript" charset="utf-8">
        {onLoad}
      </script>
    </head>
              <canvas id="gauge" width="269" height="269"/>
  }
}