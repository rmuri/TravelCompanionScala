package TravelCompanionScala.model

import net.liftweb.http._

import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._

import scala.xml._
import scala.xml.transform._

import net.liftweb.common._
import net.liftweb.util.Helpers._
import TravelCompanionScala.snippet.isLoggedIn

/**
 * Created by IntelliJ IDEA.
 * User: dhobi
 * Date: 25.03.2010
 * Time: 15:08:12
 * To change this template use File | Settings | File Templates.
 */

object UserManagement {
  val database: List[User] = RalfMuri.get :: DanielHobi.get :: Nil
  ///
  val basePath: List[String] = "user" :: Nil

  lazy val testLogginIn = If(loggedIn_? _, S.??("must.be.logged.in"))


  def loginSuffix = "login"

  lazy val loginPath = thePath(loginSuffix)

  def logoutSuffix = "logout"

  lazy val logoutPath = thePath(logoutSuffix)


  /**
   * Return the URL of the "login" page
   */
  def loginPageURL = loginPath.mkString("/", "/", "")


  /// Menues
  def loginMenuLoc: Box[Menu] =
    Full(Menu(Loc("Login", loginPath, S.??("login"), loginMenuLocParams)))

  def logoutMenuLoc: Box[Menu] =
    Full(Menu(Loc("Logout", logoutPath, S.??("logout"), logoutMenuLocParams)))

  def thePath(end: String): List[String] = basePath ::: List(end)

  /**
   * The LocParams for the menu item for login.
   * Overwrite in order to add custom LocParams. Attention: Not calling super will change the default behavior!
   */
  protected def loginMenuLocParams: List[LocParam[Unit]] =
    If(notLoggedIn_? _, S.??("already.logged.in")) ::
            Template(() => wrapIt(login)) ::
            Nil

  /**
   * The LocParams for the menu item for logout.
   * Overwrite in order to add custom LocParams. Attention: Not calling super will change the default behavior!
   */
  protected def logoutMenuLocParams: List[LocParam[Unit]] =
    Template(() => wrapIt(logout)) ::
            testLogginIn ::
            Nil



  ///Menu sitemap
  def menus: List[Menu] = sitemap

  lazy val sitemap: List[Menu] = List(loginMenuLoc, logoutMenuLoc).flatten(a => a)

  ///Login function
  def notLoggedIn_? = !loggedIn_?

  def loggedIn_? = {
    isLoggedIn.get
  }

  ///wrap it
  def screenWrap: Box[Node] = Full(<lift:surround with="default" at="content">
      <lift:bind/>
  </lift:surround>)

  protected def wrapIt(in: NodeSeq): NodeSeq =
    screenWrap.map(new RuleTransformer(new RewriteRule {
      override def transform(n: Node) = n match {
        case e: Elem if "bind" == e.label && "lift" == e.prefix => in
        case _ => n
      }
    })) openOr in


  ///Login form
  def loginXhtml = {
    (<form method="post" action={S.uri}>
      <table>
        <tr>
          <td
          colspan="2">
            {S.??("log.in")}
          </td>
        </tr>
        <tr>
          <td>
            {S.??("email.address")}
          </td> <td>
            <user:email/>
        </td>
        </tr>
        <tr>
          <td>
            {S.??("password")}
          </td> <td>
            <user:password/>
        </td>
        </tr>
        <tr>
          <td></td> <td>
            <user:submit/>
        </td>
        </tr>
      </table>
    </form>)
  }

  def checkLogin(user: User) = {
    if (user.email.equals(S.param("username").open_!) && user.password.equals(S.param("password").open_!)) {
      isLoggedIn.set(true)
    } else {
      println(S.param("username") + "/" + S.param("password"))
    }
  }


  ///Functions

  def logout = {
    isLoggedIn.set(false)
    S.redirectTo("/")
  }


  def login = {
    if (S.post_?) {
      database.foreach(u => checkLogin(u))
    }

    bind("user", loginXhtml,
      "email" -> (<input type="text" name="username"/>),
      "password" -> (<input type="password" name="password"/>),
      "submit" -> (<input type="submit" value={S.??("log.in")}/>))
  }

}