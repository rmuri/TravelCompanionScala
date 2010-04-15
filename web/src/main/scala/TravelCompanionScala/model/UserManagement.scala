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
  ///
  val basePath: List[String] = "user" :: Nil

  lazy val testLogginIn = If(loggedIn_? _, S.??("must.be.logged.in"))

  object curUsr extends SessionVar[Box[Member]](Empty)

  object userVar extends SessionVar[Member](new Member)

  def user: Member = userVar.is

  def currentUser: Member = {
    Model.merge(curUsr.is.open_!)
  }

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

  def profileMenuLoc: Box[Menu] =
    Full(Menu(Loc("Profile", profilePath, S.??("edit.profile"), profileMenuLocParams)))

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
    Template(() => wrapIt(signupFunc.map(_()) openOr signup)) ::
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
  protected def profileMenuLocParams: List[LocParam[Unit]] =
  /*Template(() => wrapIt(profile)) ::*/
    testLogginIn :: defaultLocGroup ::
            Nil



  ///Menu sitemap
  def menus: List[Menu] = sitemap

  lazy val sitemap: List[Menu] = List(loginMenuLoc, logoutMenuLoc, createUserMenuLoc, profileMenuLoc).flatten(a => a)

  ///Login function
  def notLoggedIn_? = !loggedIn_?

  def loggedIn_? = !curUsr.get.isEmpty

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
            {S.??("user.name")}
          </td> <td>
            <user:username/>
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

  def logInUser(user: Member) = {
    curUsr.set(Full(user))
    userVar(user)
    S.redirectTo("/")
  }


  ///Functions

  def logout = {
    curUsr.set(Empty)
    userVar(new Member)
    S.redirectTo("/")
  }


  def login = {
    def checkLogin() {
      val tryUser = Model.createQuery[Member]("from Member m where m.name = :name and m.password = :password").setParams("name" -> user.name, "password" -> user.password).findOne
      if (tryUser.isDefined) {
        logInUser(tryUser.get)
      } else {
        S.error({S.??("invalid.credentials")})
      }
    }

    val current = user

    bind("user", loginXhtml,
      "username" -> SHtml.text(user.name, user.name = _),
      "password" -> SHtml.password(user.password, user.password = _),
      "submit" -> SHtml.submit(S.??("log.in"), () => {userVar(current); checkLogin}))
  }

  def signupXhtml() = {
    (<form method="post" action={S.uri}>
      <table>
        <tr>
          <td colspan="2">
            <h2>
              {S.??("sign.up")}
            </h2>
          </td>
        </tr>

        <tr>
          <td>
            {S.??("user.name")}
          </td> <td>
            <user:username/>
        </td>
        </tr>

        <tr>
          <td>
            {S.??("first.name")}
          </td> <td>
            <user:firstname/>
        </td>
        </tr>

        <tr>
          <td>
            {S.??("last.name")}
          </td> <td>
            <user:lastname/>
        </td>
        </tr>

        <tr>
          <td>
            {S.??("street")}
          </td> <td>
            <user:street/>
        </td>
        </tr>

        <tr>
          <td>
            {S.??("zipcode")}
          </td> <td>
            <user:zipcode/>
        </td>
        </tr>

        <tr>
          <td>
            {S.??("city")}
          </td> <td>
            <user:city/>
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
          <td>
            &nbsp;
          </td> <td>
            <user:submit/>
        </td>
        </tr>
      </table>
    </form>)
  }

  protected object signupFunc extends RequestVar[Box[() => NodeSeq]](Empty)

  def validateMember(m: Member): Boolean = {
    var validation = true
    if (m.name == "") {
      S.error(S.??("name"))
      validation = false
    }
    if (m.email == "") {
      S.error(S.??("email"))
      validation = false
    }
    if (m.password == "") {
      S.error(S.??("password"))
      validation = false
    }
    if (!Model.createQuery[Tour]("from Member m where m.name = :name or m.email = :email").setParams("name" -> m.name, "email" -> m.email).findAll.isEmpty) {
      S.error(S.??("duplicated"))
      validation = false
    }
    validation
  }

  def signup = {

    def testSignup() {
      if (validateMember(userVar)) {
        userVar(Model.mergeAndFlush(user))
        S.notice(S.??("welcome"))
        curUsr.set(Full(user))
        S.redirectTo("/")
      } else {
        S.error(S.??("error"));
      }
    }

    val current = user

    bind("user",
      signupXhtml,
      "username" -> SHtml.text(current.name, current.name = _),
      "firstname" -> SHtml.text(current.forename, current.forename = _),
      "lastname" -> SHtml.text(current.surname, current.forename = _),
      "street" -> SHtml.text(current.street, current.street = _),
      "zipcode" -> SHtml.text(current.zipcode, current.zipcode = _),
      "city" -> SHtml.text(current.city, current.city = _),
      "email" -> SHtml.text(current.email, current.email = _),
      "password" -> SHtml.password(current.password, current.password = _),
      "submit" -> SHtml.submit(S.??("sign.up"), () => {
        userVar(current);
        testSignup
      }))

  }

}