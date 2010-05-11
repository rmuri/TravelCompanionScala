package TravelCompanionScala.comet

import _root_.net.liftweb.http._
import _root_.net.liftweb.common._
import _root_.net.liftweb.util._
import _root_.scala.xml._
import js.JsCmds
import S._


import java.text.SimpleDateFormat
import TravelCompanionScala.snippet.{tourVar, blogEntryVar}
import TravelCompanionScala.model.{Model, UserManagement, BlogEntry}
import TravelCompanionScala.controller.{AddBlogWatcher, BlogUpdate, BlogCache}

class DynamicBlogViews extends CometActor {
  override def defaultPrefix = Full("blog")

  var blog: List[BlogEntry] = Nil



  // render draws the content on the screen.
  def render = {

    def setEntry(entry: BlogEntry) = {
      JsCmds.SetHtml("blogentry_date", Text(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(entry.lastUpdated))) &
              JsCmds.SetHtml("blogentry_title", Text(entry.title)) &
              JsCmds.SetHtml("blogentry_tour",

                if (entry.tour == null) {
                  NodeSeq.Empty
                } else {
                  Text(?("blog.belongsTo") + " ") ++ SHtml.link("/tour/view", () => tourVar(entry.tour), Text(entry.tour.name))
                }

                ) &
              JsCmds.SetHtml("blogentry_content", Text(entry.content)) &
              JsCmds.JsShowId("blogentry")
    }


    bind("entry" ->
    blog.flatMap(entry =>
      bind("e", chooseTemplate("blog", "entry", defaultXml),
        "title" -> Text(entry.title),
        "content" -> Text(entry.content.substring(0, Math.min(entry.content.length, 50))),
        "readon" -> SHtml.a(() => setEntry(entry), Text(?("blog.readOn"))),
        "date" -> Text(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(entry.lastUpdated)),
        "owner" -> Text(entry.owner.name)))): NodeSeq
  }

  // localSetup is the first thing run, we use it to setup the blogid or
  // redirect them to / if no blogid was given.
  override def localSetup {
    // Let the BlogCache know that we are watching for updates for this blog.
    (BlogCache.cache !? AddBlogWatcher(this)) match {
      case BlogUpdate(entries) => this.blog = entries
    }
  }

  // lowPriority will receive messages sent from the BlogCache
  override def lowPriority: PartialFunction[Any, Unit] = {
    case BlogUpdate(entries: List[BlogEntry]) => this.blog = entries; reRender(false);
  }
}
