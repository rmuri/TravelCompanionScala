package TravelCompanionScala.controller

import _root_.net.liftweb.actor._
import _root_.net.liftweb.common._
import _root_.scala.collection.mutable.Map
import TravelCompanionScala.model.{UserManagement, Model, BlogEntry}

/**
 * An asynchronous cache for Blog Entries built on top of Scala Actors.
 */
class BlogCache extends LiftActor {
  private var cache: List[BlogEntry] = List()
  private var sessions: List[SimpleActor[Any]] = List()


  def getEntries(): List[BlogEntry] = Model.createNamedQuery[BlogEntry]("findEntriesByOthers").setParams("owner" -> UserManagement.currentUser).findAll.toList

  protected def messageHandler =
    {
      case AddBlogWatcher(me) =>
        // When somebody new starts watching, add them to the sessions and send
        // an immediate reply.
        val blog = getEntries
        reply(BlogUpdate(blog))
        cache :: blog
        sessions = sessions ::: List(me)
      case AddEntry(e) =>
        // When an Entry is added, place it into the cache and reply to the clients with it.
        cache = cache ::: List(e)
        // Now we have to notify all the listeners
        sessions.foreach(_ ! BlogUpdate(getEntries))
      case DeleteEntry(e) =>
        // When an Entry is added, place it into the cache and reply to the clients with it.
        cache = cache.filterNot(_ == e)
        // Now we have to notify all the listeners
        sessions.foreach(_ ! BlogUpdate(getEntries))
      case _ =>
    }
}

case class AddBlogWatcher(me: SimpleActor[Any]) // id is the blog id
case class AddEntry(entry: BlogEntry) // id is the blog id
case class DeleteEntry(entry: BlogEntry) // id is the blog id

// A response sent to the cache listeners with the top 20 blog entries.
case class BlogUpdate(xs: List[BlogEntry])

object BlogCache {
  lazy val cache = new BlogCache // {val ret = new BlogCache; ret.start; ret}
}

