package TravelCompanionScala.controller

import _root_.net.liftweb.actor._
import _root_.net.liftweb.common._
import TravelCompanionScala.model.{Comment, UserManagement, Model, BlogEntry}

class BlogCache extends LiftActor {
  private var cache: List[BlogEntry] = List()
  private var sessions: List[SimpleActor[Any]] = List()

  private var csessions: Map[Long,List[SimpleActor[Any]]] = Map()
  private var entry : BlogEntry = _

  def getEntries(): List[BlogEntry] = Model.createNamedQuery[BlogEntry]("findEntriesByOthers").setParams("owner" -> UserManagement.currentUser).findAll.toList

  def getComments(blogEntry: BlogEntry): List[Comment] = Model.createNamedQuery[Comment]("findCommentsByEntry").setParams("entry" -> blogEntry).findAll.toList

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
        csessions.getOrElse(e.id,Nil).foreach(_ ! CommentUpdate(getComments(e)))
        println(csessions)
      case EditComment(e) =>
        csessions.getOrElse(e.id,Nil).foreach(_ ! CommentUpdate(getComments(e)))
      case DeleteComment(e) =>
        csessions.getOrElse(e.id,Nil).foreach(_ ! CommentUpdate(getComments(e)))

      case _ =>
    }
}

case class AddBlogWatcher(me: SimpleActor[Any]) // id is the blog id
case class AddEntry(entry: BlogEntry) // id is the blog id
case class EditEntry(entry: BlogEntry) // id is the blog id
case class DeleteEntry(entry: BlogEntry) // id is the blog id

case class AddCommentWatcher(me: SimpleActor[Any], entry: BlogEntry)
case class RemoveCommentWatcher(me: SimpleActor[Any]) // id is the blog id
case class AddComment(entry: BlogEntry) // id is the blog id
case class EditComment(entry: BlogEntry) // id is the blog id
case class DeleteComment(entry: BlogEntry) // id is the blog id

// A response sent to the cache listeners with the top 20 blog entries.
case class BlogUpdate(xs: List[BlogEntry])
case class CommentUpdate(xs: List[Comment])

object BlogCache {
  lazy val cache = new BlogCache // {val ret = new BlogCache; ret.start; ret}
}

