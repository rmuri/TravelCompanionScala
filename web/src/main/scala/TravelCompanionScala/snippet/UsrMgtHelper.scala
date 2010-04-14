package TravelCompanionScala.snippet

import xml.NodeSeq
import TravelCompanionScala.model.UserManagement

/**
 * Created by IntelliJ IDEA.
 * User: rmuri
 * Date: 13.04.2010
 * Time: 12:08:13
 * To change this template use File | Settings | File Templates.
 */

class UsrMgtHelper {
  def showIfAuthenticated(html: NodeSeq) : NodeSeq = {
     if (UserManagement.loggedIn_?) {
      html
    } else {
      NodeSeq.Empty
    }
  }
}