package TravelCompanionScala.snippet

import net.liftweb.util.Helpers._
import TravelCompanionScala.model.UserManagement
import xml.{Text, NodeSeq}
import net.liftweb.http.{LiftRules, S, SHtml}
import net.liftweb.common.{Box, Empty}

/**
 * Created by IntelliJ IDEA.
 * User: dhobi
 * Date: 30.03.2010
 * Time: 12:01:19
 * To change this template use File | Settings | File Templates.
 */

class Startpage {
  def link(xhtml: NodeSeq): NodeSeq = {
    (<a href="/">Hallo</a>)
  }

  // cheap variant
  def link2(xhtml: NodeSeq): NodeSeq = {
    if (UserManagement.loggedIn_?) {
      SHtml.link("/user/login", () => println("hallo"), Text("Edit"))
    } else {
      Text(S.??("must.be.logged.in"))
    }
  }

}