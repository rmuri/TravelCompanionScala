package TravelCompanionScala.controller

import _root_.net.liftweb.actor._
import _root_.net.liftweb.common._
import TravelCompanionScala.model.{Comment, UserManagement, Model, BlogEntry}

/**
 * The BlogCache class holds all information about registered CometActors.
 * It extends from the LiftActor trait which provides the ability of getting and sending asynchronous messages.
 *
 * Further Information on Comet and creating a Comet Service can be found on:
 * - Technologiestudium (github link) Chapter 4.6 [German]
 *
 * Known Issues:
 * - findEntriesByOthers query does not work, because BlogCache cannot detect whether the user is logged in or not and always returns 0 as user id
 *
 * @author Daniel Hobi
 *
 */

class BlogCache extends LiftActor {
  private var cache: List[BlogEntry] = List()
  private var sessions: List[SimpleActor[Any]] = List()

  private var csessions: Map[Long, List[SimpleActor[Any]]] = Map()
  private var entry: BlogEntry = _

  /**
   *  gets all entries by other users (does not work properly: see known issues)
   */
  def getEntries(): List[BlogEntry] = Model.createNamedQuery[BlogEntry]("findEntriesByOthers").setParams("owner" -> UserManagement.currentUser).findAll.toList

  /**
   *  gets all comments by a blog entry
   * @param in a blog entry
   */
  def getComments(blogEntry: BlogEntry): List[Comment] = Model.createNamedQuery[Comment]("findCommentsByEntry").setParams("entry" -> blogEntry).findAll.toList

  /**
   *  This method is called by sending: BlogCache ! (case class)
   */
  protected def messageHandler =
    {
      case AddBlogWatcher(me) =>
        val blog = getEntries
        reply(BlogUpdate(blog))
        cache = cache ::: blog
        sessions = sessions ::: List(me)
      case AddEntry(e) =>
        cache = cache ::: List(e)
        sessions.foreach(_ ! BlogUpdate(cache))
      case EditEntry(e) =>
        cache = getEntries
        sessions.foreach(_ ! BlogUpdate(cache))
      case DeleteEntry(e) =>
        cache = cache.filter(p => p.id != e.id)
        sessions.foreach(_ ! BlogUpdate(cache))

      case AddCommentWatcher(me, entry) =>
        reply(CommentUpdate(getComments(entry)))
        csessions += (entry.id -> (me :: csessions.getOrElse(entry.id, Nil)))
      case RemoveCommentWatcher(me) =>
        csessions = csessions.filter(p => me != p)
      case AddComment(e) =>
        println("Comment added")
        csessions.getOrElse(e.id, Nil).foreach(_ ! CommentUpdate(getComments(e)))
      case DeleteComment(e) =>
        println("Comment deleted")
        csessions.getOrElse(e.id, Nil).foreach(_ ! CommentUpdate(getComments(e)))

      case _ =>
    }
}

/**
 *  Case classes to keep entry watchers informed
 */
case class AddBlogWatcher(me: SimpleActor[Any])
case class AddEntry(entry: BlogEntry)
case class EditEntry(entry: BlogEntry)
case class DeleteEntry(entry: BlogEntry)

/**
 *  Case classes to keep comment watchers informed
 */
case class AddCommentWatcher(me: SimpleActor[Any], entry: BlogEntry)
case class RemoveCommentWatcher(me: SimpleActor[Any])
case class AddComment(entry: BlogEntry)
case class EditComment(entry: BlogEntry)
case class DeleteComment(entry: BlogEntry)

/**
 *  Case classes to update the listeners
 */
case class BlogUpdate(xs: List[BlogEntry])
case class CommentUpdate(xs: List[Comment])

/**
 *  Companion object to access BlogCache class from everywhere in the application
 */
object BlogCache {
  lazy val cache = new BlogCache // {val ret = new BlogCache; ret.start; ret}
}

