package TravelCompanionScala.snippet

import xml.{NodeSeq, Text}
import TravelCompanionScala.model.UserManagement
import net.liftweb.http.SHtml

/**
 * Created by IntelliJ IDEA.
 * User: Ralf Muri
 * Date: 11.04.2010
 * Time: 11:57:48
 * To change this template use File | Settings | File Templates.
 */

class UserMenu {
  def show(xhtml: NodeSeq): NodeSeq = {
    {
      if (UserManagement.loggedIn_?) {
        <span>Wilkommen, username</span>
      }
      <span>[
        <lift:Menu.group group="user">
            <menu:bind/>
        </lift:Menu.group>
        ]</span>
    }
  }
}