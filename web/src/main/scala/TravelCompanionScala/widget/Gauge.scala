package TravelCompanionScala.widget

import net.liftweb.http.{LiftRules, ResourceServer}

/**
 * The Gauge object is as a GUI Widget.
 *
 * Further Information on GUI Widgets can be found on:
 * - Technologiestudium (github link) Chapter 4.5 [German]
 *
 * Notices:
 * - objImg and objImg2 have to be outside the gauge.js class to ensure correct pointing to the image path
 *
 * @author Daniel Hobi
 *
 */

object Gauge {

  /**
   *  Called by a snippet
   *  i.e. Gauge(70)
   */
  def apply(value: Int) = renderOnLoad(value)

  /**
   *  Called by Boot.scala class
   *  Makes resources available for application
   */
  def init() {
    ResourceServer.allow({
      case "gauge" :: tail => true
    })
  }

  /**
   *  Returns a NodeSeq containing several Javascript files, Javascript commands and a canvas element for drawing
   */
  def renderOnLoad(value: Int) = {
    val resources = """
        var objImg = new Image();
        var objImg2 = new Image();
        objImg2.src = '/""" + LiftRules.resourceServerPath + """/gauge/arrow.png';
        objImg.src = '/""" + LiftRules.resourceServerPath + """/gauge/gauge.png';
        """
    val onLoad = "jQuery(document).ready(function() { var g = new Gauge(); g.initialize(" + value + ", 'gauge'); });"
    <head>
      <script type="text/javascript" src="/scripts/excanvas.js"></script>
      <script type="text/javascript" src={"/" + LiftRules.resourceServerPath + "/gauge/gauge.js"}></script>
      <script type="text/javascript" charset="utf-8">
        {resources}{onLoad}
      </script>
    </head>
    <canvas id="gauge" width="269" height="269"/>
  }
}