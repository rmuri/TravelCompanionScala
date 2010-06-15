package TravelCompanionScala.snippet

import net.liftweb.http.S
import xml.{Text, NodeSeq}
import _root_.net.liftweb.util.Props

/**
 * The Props snippet renders <Props/> tags
 *
 * Further Information on usages of Properties can be found on:
 * - Technologiestudium (github link) Chapter 5.6 [German]
 *
 * @author Ralf Muri
 *
 */

class Props {
  /**
   * Renders prop tags like <lift:Props key="lastUpdate"/>
   * net.liftweb.util.Props looks for property files in src/main/resources
   */

  def render(html: NodeSeq): NodeSeq = {
    S.attr.~("key").map(_.text) match {
      case Some(key) => Text(Props.get(key, "not found"))
      case _ => NodeSeq.Empty
    }
  }
}