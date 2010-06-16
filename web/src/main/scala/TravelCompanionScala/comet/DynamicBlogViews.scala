package TravelCompanionScala.comet

import _root_.net.liftweb.http._
import _root_.net.liftweb.common._
import _root_.scala.xml.{NodeSeq, Text}
import js.jquery.JqJsCmds.JqSetHtml
import js.{JsCmd}
import js.jquery.JqJsCmds._
import S._


import java.text.SimpleDateFormat
import TravelCompanionScala.model.BlogEntry
import TravelCompanionScala.model.Comment
import TravelCompanionScala.controller._
import TravelCompanionScala.model._
import net.liftweb.util.TimeHelpers
import TravelCompanionScala.snippet.tourVarSession

/**
 * The DynamicBogViews class represents one CometActor.
 * It extends from the CometActor (SimpleActor) trait which provides the ability of getting and sending asynchronous messages.
 *
 * Further Information on Comet and creating a Comet Service can be found on:
 * - Technologiestudium (github link) Chapter 4.6 [German]
 *
 *
 * @author Daniel Hobi
 *
 */
class DynamicBlogViews extends CometActor {

  /**
   *  Overrides starting tag in view / templates
   */
  override def defaultPrefix = Full("blog")

  var blog: List[BlogEntry] = Nil
  var comments: List[Comment] = Nil
  val commentErrorDivId = "commentErrorComet"

  /**
   *  Renders the CometActor view with defaultXml
   */
  def render = {

    /**
     *  Ajax call by clicking on blog.readOn link in view
     */
    def bindEntryFull(e: BlogEntry): JsCmd = {
      /**
       *  Renders a blog entry
       */
      def getEntry(entry: BlogEntry, html: NodeSeq) = {
        bind("b", html,
          "title" -> Text(entry.title),
          "content" -> Text(entry.content),
          "tour" -> {
            if (entry.tour == null) {
              NodeSeq.Empty
            } else {
              Text(?("blog.belongsTo") + " ") ++ SHtml.link("/tour/view", () => tourVarSession(entry.tour), Text(entry.tour.name))
            }
          },
          "date" -> Text(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(entry.lastUpdated)),
          "owner" -> Text(entry.owner.name))
      }

      /**
       *  Unregister and register itself from watching comments by a blogentry and renders view afterwards
       */
      BlogCache.cache ! RemoveCommentWatcher(this)

      (BlogCache.cache !? AddCommentWatcher(this, e)) match {
        case CommentUpdate(entries) => this.comments = entries
      }

      JqSetHtml("blog_single", getEntry(e, chooseTemplate("blog", "entryfull", defaultXml))) &
              JqSetHtml("blog_comments", getComments(chooseTemplate("blog", "comments", defaultXml))) &
              JqSetHtml("blog_comments_form", renderNewCommentForm(chooseTemplate("blog", "commentForm", defaultXml), e))
    }

    /**
     *  Unregister itself from watching comments by a blogentry and renders view afterwards
     */
    BlogCache.cache ! RemoveCommentWatcher(this)

    bind("entryfull" -> NodeSeq.Empty,
      "comments" -> NodeSeq.Empty,
      "commentForm" -> NodeSeq.Empty,
      "entry" ->
              blog.flatMap(entry =>
                bind("e", chooseTemplate("blog", "entry", defaultXml),
                  "title" -> Text(entry.title),
                  "content" -> Text(entry.content.substring(0, math.min(entry.content.length, 50))),
                  "readon" -> SHtml.a(() => bindEntryFull(entry), Text(?("blog.readOn"))),
                  "date" -> Text(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(entry.lastUpdated)),
                  "owner" -> Text(entry.owner.name)))): NodeSeq
  }

  /**
   *  Renders all comments by a blog entry
   */
  def getComments(html: NodeSeq) = {
    bind("comment", html, "list" ->
            this.comments.flatMap(comment =>
              bind("c", chooseTemplate("comment", "list", html),
                "member" -> comment.member.name,
                "dateCreated" -> new SimpleDateFormat("dd.MM.yyyy HH:mm").format(comment.dateCreated),
                "content" -> comment.content)))
  }

  /**
   *  Renders an ajax comment form and saves the comment if submitted
   */
  def renderNewCommentForm(html: NodeSeq, entry: BlogEntry): NodeSeq = {
    def doSaveComment(c: Comment): JsCmd = {
      if (Validator.is_valid_entity_?(c)) {
        val merged = Model.merge(entry)
        merged.comments.add(c)
        Model.mergeAndFlush(merged)
        BlogCache.cache ! AddComment(merged)
        Hide(commentErrorDivId) /* & JqSetHtml(commentDivId + entry.id, renderComments) & JqSetHtml(commentFormDivId + entry.id, renderNewCommentForm) */
      } else {
        Show(commentErrorDivId)
      }
    }

    val newComment = new Comment
    newComment.blogEntry = entry
    newComment.member = UserManagement.currentUser
    newComment.dateCreated = TimeHelpers.now
    bind("blog", SHtml.ajaxForm(html),
      "error" -> getErrorDiv(commentErrorDivId),
      "newComment" -> SHtml.textarea(newComment.content, newComment.content = _),
      "submit" -> SHtml.ajaxSubmit(?("save"), () => doSaveComment(newComment)),
      "cancel" -> SHtml.a(() => Hide(commentErrorDivId), Text(?("cancel")), "class" -> "button"))
  }

  def getErrorDiv(divIdPrefix: String) = <div id={divIdPrefix} style="display: none;">
    <lift:Msgs>
        <lift:error_msg/>
    </lift:Msgs>
  </div>

  /**
   *  This method is called in the very first beginning
   *  Register itself in wachting blog entries
   */
  override def localSetup {
    (BlogCache.cache !? AddBlogWatcher(this)) match {
      case BlogUpdate(entries) => this.blog = entries
    }
  }

  /**
   *  LiftActor is able to call this method by sending SimpleActor ! Case class update
   */
  override def lowPriority: PartialFunction[Any, Unit] = {
    case BlogUpdate(entries: List[BlogEntry]) => this.blog = entries; reRender(false);
    case CommentUpdate(entries: List[Comment]) => {
      this.comments = entries;
      partialUpdate(JqSetHtml("blog_comments", getComments(chooseTemplate("blog", "comments", defaultXml))))
    }
  }
}
