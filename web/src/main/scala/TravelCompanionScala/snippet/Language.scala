package TravelCompanionScala.snippet

import net.liftweb.util.Helpers._
import xml.{NodeSeq}
import net.liftweb.common.{Box}
import net.liftweb.http.{SHtml}

/**
 * The Language snippet is responsible in rendering the language chooser for internationalization purposes
 *
 * Further Information on Internationalization can be found on:
 * - http://www.assembla.com/wiki/show/liftweb/Internationalization
 * - Technologiestudium (github link) Chapter 5.5 [German]
 *
 * @author Daniel Hobi
 *
 */

class Language {
  val languages: Map[String, String] = Map("de_DE" -> "/classpath/images/de.png", "en_US" -> "/classpath/images/en.png")

  def render(html: NodeSeq): NodeSeq = {
    languages.flatMap(m =>
      bind("lang", html,
        "link" -> SHtml.link("?locale=" + m._1, () => (), <img src={m._2}/>))).toList

  }

}