/*
 * Copyright 2008 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package bootstrap.liftweb

import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import net.liftweb.http._
import net.liftweb.widgets.tablesorter.TableSorter
import net.liftweb.widgets.autocomplete.AutoComplete
import provider.{HTTPCookie, HTTPRequest}
import TravelCompanionScala.model._
import scala.collection.JavaConversions._
import TravelCompanionScala.snippet.{tourVar, pictureVar, blogEntryVar}
import TravelCompanionScala.widget.Gauge
import TravelCompanionScala.api.RestAPI
import net.liftweb.common._
import java.util.Locale
import net.liftweb.util.{NamedPF, Helpers}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    ///http://groups.google.com/group/liftweb/browse_thread/thread/c95fcc4ce801b06c/d293bd49a9e68007
    ///UTF8 vs. tomcat
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // where to search for snippets, views, etc
    LiftRules.addToPackages("TravelCompanionScala")
    LiftRules.resourceNames = "TravelCompanion" :: "Member" :: "Tour" :: "Blog" :: "Picture" :: Nil

    ResourceServer.allow {
      case "css" :: _ => true
      case "images" :: _ => true
    }

    LiftRules.dispatch.append(RestAPI)
    LiftRules.dispatch.append(ImageLogic.matcher)


    // Build SiteMap (used for navigation, access control...)
    val LoggedIn = If(
      () => UserManagement.loggedIn_?,
      () => RedirectWithState(UserManagement.loginPageURL, RedirectState(() => S.error(S.??("must.be.logged.in")))))

    val EntryModification = If(
      () => {
        (UserManagement.currentUser == blogEntryVar.is.owner) ||
                (blogEntryVar.is.owner == null) ||
                (UserManagement.currentUser.roles.exists(_ == "mod"))
      },
      () => RedirectWithState("/accessrestricted", RedirectState(() => S.error(S.?("member.operation.denied")))))

    val PictureModification = If(
      () => {
        (UserManagement.currentUser == pictureVar.is.owner) ||
                (pictureVar.is.owner == null)
      },
      () => RedirectWithState("/accessrestricted", RedirectState(() => S.error(S.?("member.operation.denied")))))

    val TourModification = If(
      () => {
        (UserManagement.currentUser == tourVar.is.owner) ||
                (tourVar.is.owner == null)
      },
      () => RedirectWithState("/accessrestricted", RedirectState(() => S.error(S.?("member.operation.denied")))))

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

    LiftRules.statelessRewrite.prepend(NamedPF("TourRewrite") {
      case RewriteRequest(
      ParsePath("tour" :: "view" :: AsTour(tour) :: Nil, _, _, _), _, _) => {
        tourVar(tour)
        RewriteResponse("tour" :: "view" :: Nil)
      }
    })


    ///Copied from: https://www.assembla.com/wiki/show/liftweb/Internationalization
    def localeCalculator(request: Box[HTTPRequest]): Locale = {
      request.flatMap(r => {
        def localeCookie(in: String): HTTPCookie =
          HTTPCookie("language", Full(in),
            Empty, Full("/"), Full(2629743), Empty, Empty)
        def localeFromString(in: String): Locale = {
          val x = in.split("_").toList;
          new Locale(x.head, x.last)
        }
        def calcLocale: Box[Locale] =
          S.findCookie("language").map(
            _.value.map(localeFromString)
            ).openOr(Full(LiftRules.defaultLocaleCalculator(request)))
        S.param("locale") match {
          case Full(null) => calcLocale
          case f@Full(selectedLocale) =>
            S.addCookie(localeCookie(selectedLocale))
            Helpers.tryo(localeFromString(selectedLocale))
          case _ => calcLocale
        }
      }).openOr(Locale.getDefault())
    }

    LiftRules.localeCalculator = localeCalculator _



    //Widgets
    TableSorter.init
    AutoComplete.init
    Gauge.init
  }
}

