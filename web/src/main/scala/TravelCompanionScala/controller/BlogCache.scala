package TravelCompanionScala.controller

import _root_.net.liftweb.actor._
import _root_.net.liftweb.common._
import TravelCompanionScala.model.{UserManagement, Model, BlogEntry}


class BlogCache extends LiftActor {
  private var cache: List[BlogEntry] = List()
  private var sessions: List[SimpleActor[Any]] = List()


  def getEntries(): List[BlogEntry] = Model.createNamedQuery[BlogEntry]("findEntriesByOthers").setParams("owner" -> UserManagement.currentUser).findAll.toList

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
      case DeleteEntry(e) =>
        println("Vorher:"+cache.size)
        cache = cache.filter(p =>  p.id != e.id)
        println("Nachher:"+cache.size)
        sessions.foreach(_ ! BlogUpdate(cache))
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

