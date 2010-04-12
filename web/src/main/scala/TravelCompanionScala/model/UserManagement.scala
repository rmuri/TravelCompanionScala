package TravelCompanionScala.model

import net.liftweb.http._

import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._

import scala.xml._
import scala.xml.transform._

import net.liftweb.common._
import net.liftweb.util.Helpers._

/**
 * Created by IntelliJ IDEA.
 * User: dhobi
 * Date: 25.03.2010
 * Time: 15:08:12
 * To change this template use File | Settings | File Templates.
 */

object UserManagement {
  val database: List[User] = new User("Ralf", "Muri", "rmuri@gmail.com", "123456") :: new User("Daniel", "Hobi", "d.hobi@gmx.ch", "1234") :: new User("Test", "Test", "t@test.com", "test") :: Nil
  ///
  val basePath: List[String] = "user" :: Nil

  lazy val testLogginIn = If(loggedIn_? _, S.??("must.be.logged.in"))
  private object curUser extends SessionVar[Box[User]](Empty)


  def loginSuffix = "login"

  lazy val loginPath = thePath(loginSuffix)

  def logoutSuffix = "logout"

  lazy val logoutPath = thePath(logoutSuffix)

  def signUpSuffix = "sign_up"

  lazy val registerPath = thePath(signUpSuffix)

  def profileSuffix = "profile"

  lazy val profilePath = thePath(profileSuffix)

  val defaultLocGroup = LocGroup("user")


  /**
   * Return the URL of the "login" page
   */
  def loginPageURL = loginPath.mkString("/", "/", "")


  /// Menues
  def loginMenuLoc: Box[Menu] =
    Full(Menu(Loc("Login", loginPath, S.??("login"), loginMenuLocParams)))

  def logoutMenuLoc: Box[Menu] =
    Full(Menu(Loc("Logout", logoutPath, S.??("logout"), logoutMenuLocParams)))

  def createUserMenuLoc: Box[Menu] =
    Full(Menu(Loc("CreateUser", registerPath, S.??("sign.up"), createUserMenuLocParams)))

  /*def profileMenuLoc: Box[Menu] =
    Full(Menu(Loc("Profile", profilePath, S.??("profile"), profileMenuLocParams)))*/

  def thePath(end: String): List[String] = basePath ::: List(end)

  /**
   * The LocParams for the menu item for login.
   * Overwrite in order to add custom LocParams. Attention: Not calling super will change the default behavior!
   */
  protected def loginMenuLocParams: List[LocParam[Unit]] =
  
    If(notLoggedIn_? _, S.??("already.logged.in")) ::
            Template(() => wrapIt(login)) :: defaultLocGroup ::
            Nil

  /**
   * The LocParams for the menu item for register.
   * Overwrite in order to add custom LocParams. Attention: Not calling super will change the default behavior!
   */
  protected def createUserMenuLocParams: List[LocParam[Unit]] =
    /*Template(() => wrapIt(signupFunc.map(_()) openOr signup)) ::*/
            If(notLoggedIn_? _, S.??("logout.first")) :: defaultLocGroup ::
            Nil

  /**
   * The LocParams for the menu item for logout.
   * Overwrite in order to add custom LocParams. Attention: Not calling super will change the default behavior!
   */
  protected def logoutMenuLocParams: List[LocParam[Unit]] =
    Template(() => wrapIt(logout)) ::
            testLogginIn :: defaultLocGroup ::
            Nil

  /**
   * The LocParams for the menu item for profile.
   * Overwrite in order to add custom LocParams. Attention: Not calling super will change the default behavior!
   */
  /*  protected def profileMenuLocParams: List[LocParam[Unit]] =
Template(() => wrapIt(profile)) ::
  testLogginIn :: defaultLocGroup ::
  Nil*/



  ///Menu sitemap
  def menus: List[Menu] = sitemap

  lazy val sitemap: List[Menu] = List(loginMenuLoc, logoutMenuLoc, createUserMenuLoc /*, profileMenuLoc*/ ).flatten(a => a)

  ///Login function
  def notLoggedIn_? = !loggedIn_?

  def loggedIn_? = !curUser.get.isEmpty

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
      curUser.set(Full(user))
      S.redirectTo("/")
    } else {

    }
  }


  ///Functions

  def logout = {
    curUser.set(Empty)
    S.redirectTo("/")
  }


  def login = {
    if (S.post_?) {
      database.foreach(u => checkLogin(u))
      if (notLoggedIn_?) {
        S.error({S.??("invalid.credentials")})
      }
    }

    bind("user", loginXhtml,
      "email" -> (<input type="text" name="username"/>),
      "password" -> (<input type="password" name="password"/>),
      "submit" -> (<input type="submit" value={S.??("log.in")}/>))
  }

}