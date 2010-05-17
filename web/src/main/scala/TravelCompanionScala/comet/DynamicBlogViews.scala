package TravelCompanionScala.comet

import _root_.net.liftweb.http._
import _root_.net.liftweb.common._
import _root_.net.liftweb.util._
import _root_.scala.xml._
import js.jquery.JqJsCmds.JqSetHtml
import js.{JsCmd, JsCmds}
import S._


import java.text.SimpleDateFormat
import TravelCompanionScala.snippet.tourVar
import TravelCompanionScala.model.BlogEntry
import TravelCompanionScala.controller._


class DynamicBlogViews extends CometActor {
  override def defaultPrefix = Full("blog")

  var blog: List[BlogEntry] = Nil




  // render draws the content on the screen.
  def render = {

    def bindEntryFull(e: BlogEntry): JsCmd = {

      def getEntry(entry: BlogEntry, html: NodeSeq) = {
        bind("b", html,
          "title" -> Text(entry.title),
          "content" -> Text(entry.content),
          "tour" -> {
            if (entry.tour == null) {
              NodeSeq.Empty
            } else {
              Text(?("blog.belongsTo") + " ") ++ SHtml.link("/tour/view", () => tourVar(entry.tour), Text(entry.tour.name))
            }
          },
          "date" -> Text(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(entry.lastUpdated)),
          "owner" -> Text(entry.owner.name))
      }

      def getComments(e: BlogEntry, html: NodeSeq) = {
        <lift:comet type="CommentView" name={e.id.toString}>
          {html}
        </lift:comet>
      }

      JqSetHtml("blog_single", getEntry(e, chooseTemplate("blog", "entryfull", defaultXml))) &
              JqSetHtml("blog_comments", getComments(e, chooseTemplate("blog", "comments", defaultXml)))
    }


    bind("entryfull" -> NodeSeq.Empty,
      "comments" -> NodeSeq.Empty,
      "entry" ->
              blog.flatMap(entry =>
                bind("e", chooseTemplate("blog", "entry", defaultXml),
                  "title" -> Text(entry.title),
                  "content" -> Text(entry.content.substring(0, Math.min(entry.content.length, 50))),
                  "readon" -> SHtml.a(() => bindEntryFull(entry), Text(?("blog.readOn"))),
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
