package TravelCompanionScala.snippet


import xml.NodeSeq
import net.liftweb.http._
import net.liftweb.http.SHtml._
import net.liftweb.util.Helpers._

/**
 * Created by IntelliJ IDEA.
 * User: dhobi
 * Date: 23.03.2010
 * Time: 09:08:05
 * To change this template use File | Settings | File Templates.
 */

object userName extends RequestVar[String]("")
object password extends RequestVar[String]("")
// You can use a StatefulSnippet to avoid using these RequestVar-s

object isLoggedIn extends SessionVar[Boolean](false)

class Login {
  def login(xhtml: NodeSeq): NodeSeq = {
    bind("f", xhtml,
      "username" -> text("", userName.set(_)),
      "password" -> text("", password.set(_)),
      "submit" -> submit("Login", () => {
        // Call your service login to talk with an external source
        isLoggedIn.set(true)
        // If your external session needs its own session ID after authentication (they usually do) you can retain that session ID
        // into the SessionVar
        println("Username: " + userName.get + ", Password: " + password.get)
      })
      )
    
  }
}
// We used -%> function in order for the attributes from the markup to be preserved.
