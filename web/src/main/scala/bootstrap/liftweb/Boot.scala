package bootstrap.liftweb

import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import net.liftweb.http._
import net.liftweb.widgets.tablesorter.TableSorter
import net.liftweb.widgets.autocomplete.AutoComplete
import provider.{HTTPCookie, HTTPRequest}
import TravelCompanionScala.model._
import scala.collection.JavaConversions._
import TravelCompanionScala.widget.Gauge
import net.liftweb.common._
import java.util.Locale
import net.liftweb.util.{NamedPF, Helpers}
import TravelCompanionScala.snippet.{stageVar, tourVar, pictureVar, blogEntryVar}
import TravelCompanionScala.api.{GridAPI, RestAPI}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    ///http://groups.google.com/group/liftweb/browse_thread/thread/c95fcc4ce801b06c/d293bd49a9e68007
    ///UTF8 vs. tomcat
    //LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    //Have a central control for forcing mimetype
    //http://groups.google.com/group/liftweb/browse_thread/thread/85721b2134db5203/8dd1529cd9eaf315?lnk=raot
    LiftRules.determineContentType = {
      case (Full(Req("tour" :: "list" :: Nil, _, //jqgrid
      GetRequest)), _) =>
        "text/html; charset=utf-8"
      case (Full(Req("tour" :: "view" :: Nil, _, //google maps
      GetRequest)), _) =>
        "text/html; charset=utf-8"
      case (Full(Req("tour" :: "stage" :: "view" :: Nil, _, //google maps
      GetRequest)), _) =>
        "text/html; charset=utf-8"
      case (_, Full(accept))
        if LiftRules.useXhtmlMimeType &&
                accept.toLowerCase.contains("application/xhtml+xml") =>
        "application/xhtml+xml; charset=utf-8"
      case _ => "text/html; charset=utf-8"
    }

    // where to search for snippets, views, etc
    LiftRules.addToPackages("TravelCompanionScala")
    LiftRules.resourceNames = "TravelCompanion" :: "Member" :: "Tour" :: "Blog" :: "Picture" :: Nil

    ResourceServer.allow {
      case "css" :: _ => true
      case "images" :: _ => true
    }

    LiftRules.dispatch.append(RestAPI)
    LiftRules.dispatch.append(GridAPI)
    LiftRules.dispatch.append(ImageLogic.matcher)

    // Build SiteMap (used for navigation, access control...)
    val LoggedIn = If(
      () => UserManagement.loggedIn_?,
      () => RedirectWithState(UserManagement.loginPageURL, RedirectState(() => S.error(S.??("must.be.logged.in")))))

    def conditionalAccess(cond: List[Boolean]) = If(
      () => cond.exists(_ == true),
      () => RedirectWithState("/accessrestricted", RedirectState(() => S.error(S.?("member.operation.denied")))))

    val EntryModification = conditionalAccess(List(
      UserManagement.currentUser == blogEntryVar.is.owner,
      blogEntryVar.is.owner == null,
      UserManagement.currentUser.roles.exists(_ == "mod")))

    val PictureModification = conditionalAccess(List(
      UserManagement.currentUser == pictureVar.is.owner,
      pictureVar.is.owner == null))

    val TourModification = conditionalAccess(List(
      UserManagement.currentUser == tourVar.is.owner,
      tourVar.is.owner == null))

    // new DSL Syntax for creating Menu Entries, since Lift2.0-M5
    val tourMenuEntries: List[Menu] = List(
      Menu("tour", S ? "tour") / "tour" / "list" >> LocGroup("main") >> LocGroup("tour"),
      Menu("tour_view", "Reise anzeigen") / "tour" / "view" >> LocGroup("tour"),
      Menu("tour_edit", "Reise bearbeiten") / "tour" / "edit" >> LoggedIn >> TourModification >> LocGroup("tour"),
      Menu("tour_stage_add", "Abschnitt ansehen") / "tour" / "stage" / "view" >> LocGroup("tour"),
      Menu("tour_stage_edit", "Abschnitt bearbeiten") / "tour" / "stage" / "edit" >> LoggedIn >> TourModification >> LocGroup("tour")
      )

    val blogMenuEntries: List[Menu] = List(
      Menu(Loc("blog", "blog" :: "list" :: Nil, S.?("blog"), LocGroup("main"), LocGroup("blog"))),
      Menu(Loc("blog_view", "blog" :: "view" :: Nil, S.?("viewElem", S.?("blog.entry")), LocGroup("blog"))),
      Menu(Loc("blog_edit", "blog" :: "edit" :: Nil, S.?("editElem", S.?("blog.entry")), LoggedIn, EntryModification, LocGroup("blog"))),
      Menu(Loc("blog_remove", "blog" :: "remove" :: Nil, S.?("removeElem", S.?("blog.entry")), LoggedIn, EntryModification, LocGroup("blog"))))

    val pictureMenuEntries: List[Menu] = List(
      Menu(Loc("picture", "picture" :: "list" :: Nil, S.?("pictures"), LocGroup("main"), LocGroup("picture"))),
      Menu(Loc("picture_view", "picture" :: "view" :: Nil, "Bild anzeigen", LocGroup("picture"))),
      Menu(Loc("picture_create", "picture" :: "create" :: Nil, "Bild hinzuf&uuml;gen", LoggedIn, PictureModification, LocGroup("picture"))))

    val entries = Menu(Loc("index", "index" :: Nil, S.?("home"), LocGroup("main"))) ::
            Menu(Loc("access_restricted", "accessrestricted" :: Nil, "Access Restricted")) ::
            tourMenuEntries ::: blogMenuEntries ::: pictureMenuEntries ::: UserManagement.sitemap

    LiftRules.setSiteMap(SiteMap(entries: _*))


    object AsTour {
      def unapply(name: String): Option[Tour] = {
        Model.createNamedQuery("findTourByName", "name" -> name).findOne
      }
    }

    object AsStage {
      def unapply(name: String): Option[Stage] = {
        Model.createNamedQuery("findStageByName", "name" -> name).findOne
      }
    }

    LiftRules.statefulRewrite.prepend(NamedPF("TourRewrite") {
      case RewriteRequest(
      ParsePath("tour" :: "view" :: AsTour(tour) :: Nil, _, _, _), _, _) => {
        tourVar(tour)
        RewriteResponse("tour" :: "view" :: Nil)
      }
      case RewriteRequest(
      ParsePath("tour" :: "view" :: AsTour(tour) :: AsStage(stage) :: Nil, _, _, _), _, _) => {
        tourVar(tour)
        stageVar(stage)
        RewriteResponse("tour" :: "stage" :: "view" :: Nil)
      }
      //      case RewriteRequest(
      //      ParsePath("tour" :: "view" :: Nil, _, _, _), _, _) => {
      //        RewriteResponse("tour" :: "view" :: tourVar.get.name :: Nil)
      //      }
      //      case RewriteRequest(
      //      ParsePath("tour" :: "stage" :: "view" :: Nil, _, _, _), _, _) => {
      //        RewriteResponse("tour" :: "view" :: tourVar.get.name :: stageVar.get.name :: Nil)
      //      }
    })

    ///Copied from: https://www.assembla.com/wiki/show/liftweb/Internationalization
    def localeCalculator(request: Box[HTTPRequest]): Locale = {

      object sessionLanguage extends SessionVar[Locale](LiftRules.defaultLocaleCalculator(request))

      request.flatMap(r => {
        def localeCookie(in: String): HTTPCookie =
          HTTPCookie("language", Full(in),
            Empty, Full("/"), Full(2629743), Empty, Empty)
        def localeFromString(in: String): Locale = {
          val x = in.split("_").toList;
          sessionLanguage(new Locale(x.head, x.last))
          sessionLanguage.is
        }
        def calcLocale: Box[Locale] =
          S.findCookie("language").map(
            _.value.map(localeFromString)
            ).openOr(Full(sessionLanguage.is))
        S.param("locale") match {
          case Full(null) => calcLocale
          case f@Full(selectedLocale) =>
            S.addCookie(localeCookie(selectedLocale))
            //hacky?
            S.session.map(_.findComet("DynamicBlogViews").foreach(_ ! ReRender(true)))
            Helpers.tryo(localeFromString(selectedLocale))
          case _ => calcLocale
        }
      }).openOr(sessionLanguage.is)
    }

    LiftRules.localeCalculator = localeCalculator _

    //Widgets
    TableSorter.init
    AutoComplete.init
    Gauge.init
  }
}

