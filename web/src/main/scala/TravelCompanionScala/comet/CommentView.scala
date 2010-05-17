package TravelCompanionScala.comet

import net.liftweb.http.CometActor
import java.text.SimpleDateFormat
import TravelCompanionScala.controller._
import net.liftweb.common.Full
import TravelCompanionScala.model.{Model, BlogEntry, Comment}
import net.liftweb.util.Helpers

/**
 * Created by IntelliJ IDEA.
 * User: dhobi
 * Date: 13.05.2010
 * Time: 16:39:44
 * To change this template use File | Settings | File Templates.
 */

class CommentView extends CometActor {
  var comments: List[Comment] = Nil
  var entry: BlogEntry = _

  def render = {

    bind("comment", defaultXml, "list" ->
            this.comments.flatMap(comment =>
              bind("c", chooseTemplate("comment", "list", defaultXml),
                "member" -> comment.member.name,
                "dateCreated" -> new SimpleDateFormat("dd.MM.yyyy HH:mm").format(comment.dateCreated),
                "content" -> comment.content)))

  }

  override def localSetup {
    name match {
      case Full(t) => { this.entry = Model.createQuery[BlogEntry]("SELECT e from BlogEntry e WHERE e.id = :id").setParams("id" ->Helpers.toLong(t)).findOne.getOrElse(new BlogEntry) }
    }



    // Let the BlogCache know that we are watching for updates for this blog.
    (BlogCache.cache !? AddCommentWatcher(this,this.entry)) match {
      case CommentUpdate(entries) => this.comments = entries
    }
  }

  override def localShutdown {
      BlogCache.cache ! RemoveCommentWatcher(this)
  }

  // lowPriority will receive messages sent from the BlogCache
  override def lowPriority: PartialFunction[Any, Unit] = {
    case CommentUpdate(entries: List[Comment]) => {
      this.comments = entries;
      println("********** "+this+" ********************")
      this.comments.foreach(c => println(c))
      reRender(true)
    };
  }
}