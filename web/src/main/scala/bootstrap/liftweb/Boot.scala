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

import _root_.net.liftweb.common.{Box}
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import net.liftweb.http._
import net.liftweb.widgets.tablesorter.TableSorter
import net.liftweb.widgets.autocomplete.AutoComplete
import TravelCompanionScala.model._
import scala.collection.JavaConversions._
import TravelCompanionScala.snippet.{tourVar, pictureVar, blogEntryVar}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    // where to search for snippets, views, etc
    LiftRules.addToPackages("TravelCompanionScala")
    LiftRules.resourceNames = "TravelCompanion" :: "Member" :: "Tour" :: "Blog" :: Nil

    ResourceServer.allow {
      case "css" :: _ => true
    }

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

    val tourMenuEntries: List[Menu] = List(
      Menu(Loc("tour", "tour" :: "list" :: Nil, S.?("tour"), LocGroup("main"), LocGroup("tour"))),
      Menu(Loc("tour_view", "tour" :: "view" :: Nil, "Reise anzeigen", LocGroup("tour"))),
      Menu(Loc("tour_edit", "tour" :: "edit" :: Nil, "Reise bearbeiten", LoggedIn, TourModification, LocGroup("tour"))),
      Menu(Loc("tour_stage_view", "tour" :: "stage" :: "view" :: Nil, "Abschnitt ansehen", LocGroup("tour"))),
      Menu(Loc("tour_stage_add", "tour" :: "stage" :: "edit" :: Nil, "Stage bearbeiten", LoggedIn, TourModification, LocGroup("tour")))
      )

    val blogMenuEntries: List[Menu] = List(
      Menu(Loc("blog", "blog" :: "list" :: Nil, S.?("blog"), LocGroup("main"), LocGroup("blog"))),
      Menu(Loc("blog_view", "blog" :: "view" :: Nil, S.?("saveElem", S.?("blog.entry")), LocGroup("blog"))),
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

    //    LiftRules.rewrite.append {
    //      case RewriteRequest(
    //      ParsePath(List("tour", action, id), _, _, _), _, _) =>
    //        RewriteResponse("tour" :: action :: Nil, Map("id" -> id))
    //    }


    //Widgets
    TableSorter.init
    AutoComplete.init
  }
}

