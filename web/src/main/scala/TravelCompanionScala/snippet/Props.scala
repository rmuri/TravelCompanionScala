package TravelCompanionScala.snippet

import net.liftweb.util.Props
import net.liftweb.http.S
import xml.{Text, NodeSeq}

/**
 * Created by IntelliJ IDEA.
 * User: rmuri
 * Date: 27.05.2010
 * Time: 10:32:08
 * To change this template use File | Settings | File Templates.
 */

class Props {
  def render(html: NodeSeq): NodeSeq = {
    S.attr.~("key").map(_.text) match {
      case Some(key) => Text(Props.get(key, "not found"))
      case _ => NodeSeq.Empty
    }
  }
}