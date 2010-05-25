package TravelCompanionScala.snippet

import java.util.Locale
import net.liftweb.util.Helpers._
import xml.{Text, NodeSeq}
import net.liftweb.http.provider.HTTPCookie
import javax.servlet.http.HttpServletRequest
import net.liftweb.common.{Failure, Empty, Box, Full}
import net.liftweb.http.{LiftRules, S, SHtml}

/**
 * Created by IntelliJ IDEA.
 * User: dhobi
 * Date: 20.05.2010
 * Time: 08:53:44
 * To change this template use File | Settings | File Templates.
 */

class Language {
  val cookiename = "lang"
  val languages: Map[String, String] = Map("de_DE" -> "/classpath/images/de.png", "en_US" -> "/classpath/images/en.png")

  def render(html: NodeSeq): NodeSeq = {
    languages.flatMap(m =>
      bind("lang", html,
        "link" -> SHtml.link("?locale="+m._1, ()=>(), <img src={m._2}/> ))).toList

  }

}