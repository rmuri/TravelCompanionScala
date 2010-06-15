package TravelCompanionScala.snippet

import xml.NodeSeq
import TravelCompanionScala.model.UserManagement
import net.liftweb.util.Helpers._
import net.liftweb.http.S
import scala.collection.JavaConversions._


/**
 * The UsrMgtHelper class helps to deal with content which should be only viewable for users owning certain roles
 *
 * @author Ralf Muri
 *
 */

class UsrMgtHelper {

  /**
   * Checks the condition and return the given NodeSeq back if it is true
   * If the condition is false it swallows the NodeSeq and returns a empty one
   */
  private def showIf(html: NodeSeq, cond: Boolean): NodeSeq = {
    if (cond)
      html
    else
      NodeSeq.Empty
  }

  /**
   * Only shows the given NodeSeq if the user is authenticated
   */
  def showIfAuthenticated(html: NodeSeq): NodeSeq = {
    showIf(html, UserManagement.loggedIn_?)

  }

  /**
   * Only shows the given NodeSeq if the user is the tour owner
   */
  def showIfTourOwner(html: NodeSeq): NodeSeq = {
    showIf(html, tourVar.is.owner == UserManagement.currentUser)
  }

  /**
   * Only shows the given NodeSeq if the user is the blog entry owner
   */
  def showIfBlogEntryOwner(html: NodeSeq): NodeSeq = {
    showIf(html, blogEntryVar.is.owner == UserManagement.currentUser)
  }

  /**
   * Only shows the given NodeSeq if the user is in role
   */
  def showIfInRole(html: NodeSeq): NodeSeq = {
    val role = S.attr("role").map(_.toString) openOr "Guest"
    showIf(html, UserManagement.currentUser.roles.exists(_.name == role))
  }

  /**
   * Shows up the username of the current user
   */
  def currentUser(html: NodeSeq): NodeSeq = {
    bind("user", html, "name" -> UserManagement.currentUser.name)
  }
}