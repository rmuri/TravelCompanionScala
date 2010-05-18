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
import TravelCompanionScala.model.Comment
import TravelCompanionScala.controller._


class DynamicBlogViews extends CometActor {
  override def defaultPrefix = Full("blog")

  var blog: List[BlogEntry] = Nil
  var comments: List[Comment] = Nil


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

      BlogCache.cache ! RemoveCommentWatcher(this)

      (BlogCache.cache !? AddCommentWatcher(this, e)) match {
        case CommentUpdate(entries) => this.comments = entries
      }

      JqSetHtml("blog_single", getEntry(e, chooseTemplate("blog", "entryfull", defaultXml))) &
              JqSetHtml("blog_comments", getComments(chooseTemplate("blog", "comments", defaultXml)))
    }

    BlogCache.cache ! RemoveCommentWatcher(this)

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

  def getComments(html: NodeSeq) = {
    bind("comment", html, "list" ->
            this.comments.flatMap(comment =>
              bind("c", chooseTemplate("comment", "list", html),
                "member" -> comment.member.name,
                "dateCreated" -> new SimpleDateFormat("dd.MM.yyyy HH:mm").format(comment.dateCreated),
                "content" -> comment.content)))
  }


  override def localSetup {
    (BlogCache.cache !? AddBlogWatcher(this)) match {
      case BlogUpdate(entries) => this.blog = entries
    }
  }

  override def lowPriority: PartialFunction[Any, Unit] = {
    case BlogUpdate(entries: List[BlogEntry]) => this.blog = entries; reRender(false);
    case CommentUpdate(entries: List[Comment]) => {
      this.comments = entries;
      partialUpdate(JqSetHtml("blog_comments", getComments(chooseTemplate("blog", "comments", defaultXml))))
    }
  }
}
