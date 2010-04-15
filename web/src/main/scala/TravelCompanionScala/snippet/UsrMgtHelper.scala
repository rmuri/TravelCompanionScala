package TravelCompanionScala.snippet

import xml.NodeSeq
import TravelCompanionScala.model.UserManagement
import net.liftweb.util.Helpers._

/**
 * Created by IntelliJ IDEA.
 * User: rmuri
 * Date: 13.04.2010
 * Time: 12:08:13
 * To change this template use File | Settings | File Templates.
 */

class UsrMgtHelper {
  def showIfAuthenticated(html: NodeSeq): NodeSeq = {
    if (UserManagement.loggedIn_?) {
      html
    } else {
      NodeSeq.Empty
    }
  }

  def currentUser(html: NodeSeq): NodeSeq = {
    bind("user", html, "name" -> UserManagement.user.name)
  }
}