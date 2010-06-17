package TravelCompanionScala.snippet

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb._
import common.{Full, Empty}
import http._
import js.jquery.{JqJsCmds}
import js.{JsCmd}
import S._
import util._
import Helpers._
import JqJsCmds._
import TravelCompanionScala.model._
import java.text.SimpleDateFormat
import scala.collection.JavaConversions._
import TravelCompanionScala.controller._

/**
 * The BlogSnippet class is responsible for the dynamic content in the blog section of TravelCompanion.
 * It deals with CRUD Operations on BlogEntries and Comments. It is implemented as Single Page Web
 * Application using Ajax functionality. Additionally the List of Blog Entries by others is updated via
 * Comet.
 *
 * Further Information on Ajax and Coment can be found on:
 * - http://demo.liftweb.net/ajax$
 * - Technologiestudium (github link) Chapter 4.6 [German]
 *
 * Known Issues:
 * - List with blog entries by others shows also the entries by the current user
 * @see BlogCache for further details
 *
 * @author Ralf Muri
 * @see
 *
 */

// Set up a requestVar to track blog entry and comment objects for edits and adds
object blogEntryVar extends RequestVar[BlogEntry](new BlogEntry())
object commentVar extends RequestVar[Comment](new Comment())

/**
 * Function object implementing the jQuery remove function. Respective function from the Lift
 * JavaScript Libary is not working properly.
 */
object myJqRemove {
  def apply(uid: String): JsCmd = new Remove(uid)
}
class Remove(uid: String) extends JsCmd {
  def toJsCmd = "try{jQuery(" + ("#" + uid).encJs + ").remove();} catch (e) {}"
}

class BlogSnippet {

  /**
   * Frequently used constants
   */
  val entriesDivId = "entriesList"
  val entryFormDivId = "addEntryForm"
  val newEntryLink = "newEntryLink"
  val entryErrorDivId = "addEntryErrors"
  val blogEntryDivId = "blogEntry"
  val commentDivId = "comment"
  val commentErrorDivId = "commentError"
  val commentFormDivId = "commentForm"

  /**
   * Return markup code for a div rendering the Lift error messages
   */
  def getErrorDiv(divIdPrefix: String) = <div id={divIdPrefix} style="display: none;">
    <lift:Msgs>
        <lift:error_msg/>
    </lift:Msgs>
  </div>

  /**
   * Default render method for de BlogSnippet. This big method contains all the functionality
   * for the Single Page Blog Webapplication. The functionality is split within inner functions.
   */
  def render(html: NodeSeq): NodeSeq = {
    /**
     * Via the chooseTemplate Method are various often used XML fragments extracted
     */
    val entryTemplate = chooseTemplate("choose", "entry", html)
    val entryFormTemplate = chooseTemplate("choose", "form", html)
    val commentsTemplate = chooseTemplate("choose", "comments", html)
    val commentFormTemplate = chooseTemplate("choose", "commentForm", html)

    /**
     * Ajax-Link Callack function. This inner function saves a updated blog entry to the database
     * and returns the JavaScript Commands for updating the clients screen.
     * @param entry The blog entry to save
     */
    def doEditBlogEntry(entry: BlogEntry): JsCmd = {
      // define save function for the blog entry
      val save = () => {
        // only proceed when the entry is valid
        if (Validator.is_valid_entity_?(entry)) {
          val merged = Model.mergeAndFlush(entry)
          // fire the EditEntry event on the LiftActor and pass on the updated entity
          BlogCache.cache ! EditEntry(merged)
          // Update the div displaying the entry on the client side
          JqSetHtml(blogEntryDivId + entry.id, listEntries(entryTemplate, List(merged)))
        } else {
          // in case of invalid entity display error messages
          Show(entryErrorDivId)
        }
      }
      // define cancel function for the blog entry
      val cancel = () => Hide(entryErrorDivId) & JqSetHtml(blogEntryDivId + entry.id, listEntries(entryTemplate, List(Model.getReference(classOf[BlogEntry], entry.id))))
      // show the edit entry form on the client side and attach the submit and cancel function previously defined
      JqSetHtml(blogEntryDivId + entry.id, getEntryForm(entry, entryFormTemplate, save, cancel))
    }

    /**
     * Ajax-Link Callback Function. This inner function implements rendering comments, adding new comments and
     * removing comments. The different inner functions run their operation on the entity and update the clients
     * screen accorind to the made modifications.
     * @param entry The blog entry to operate on
     */
    def doComments(entry: BlogEntry): JsCmd = {

      /**
       * Removes a given comment and updates the clients screen via Ajax
       * @param c The comment to remove
       */
      def removeComment(c: Comment): JsCmd = {
        val mergedc = Model.merge(c)
        Model.removeAndFlush(mergedc)
        BlogCache.cache ! DeleteComment(entry)
        // return the JavaScript command to rerendere the comments list
        JqSetHtml(commentDivId + entry.id, renderComments)
      }

      /**
       * Displays a single comment
       * @param c The comment to display
       */
      def bindComment(c: Comment) = bind("comment", chooseTemplate("blog", "comment", html),
        "member" -> c.member.name,
        "dateCreated" -> new SimpleDateFormat("dd.MM.yyyy HH:mm").format(c.dateCreated),
        "content" -> c.content,
        "options" -> {
          // if the current member has sufficient rights a remove link is displayed on the comment
          if ((c.member == UserManagement.currentUser) || (entry.owner == UserManagement.currentUser))
            bind("link", chooseTemplate("option", "list", html), "remove" -> SHtml.a(() => removeComment(c), Text(?("remove"))))
          else
            NodeSeq.Empty
        })

      /**
       * Render a list of comments
       */
      def renderComments() = {
        val merged = Model.merge(entry)
        Model.refresh(merged)
        bind("blog", commentsTemplate, "comment" -> merged.comments.flatMap(c => bindComment(c)))
      }

      /**
       * Render a form to create a new comment and implement the necessary logic.
       */
      def renderNewCommentForm(): NodeSeq = {
        /**
         * Saves a new comment to the database and updates the clients screen
         * @param c The newly created comment to save
         */
        def doSaveComment(c: Comment): JsCmd = {
          // process onlyif entity is valid
          if (Validator.is_valid_entity_?(c)) {
            val merged = Model.merge(entry)
            merged.comments.add(c)
            Model.mergeAndFlush(merged)
            // fire the addComment event on the LiftActor and pass on the updated entry
            BlogCache.cache ! AddComment(merged)
            // update the clients screen: hode the error div and rerendere the comments list
            Hide(commentErrorDivId) & JqSetHtml(commentDivId + entry.id, renderComments) & JqSetHtml(commentFormDivId + entry.id, renderNewCommentForm)
          } else {
            // if invalid entity show the Lift error messages
            Show(commentErrorDivId)
          }
        }

        val newComment = new Comment
        newComment.blogEntry = entry
        newComment.member = UserManagement.currentUser
        newComment.dateCreated = TimeHelpers.now
        // bind the markup code to the dynamic content
        bind("blog", SHtml.ajaxForm(commentFormTemplate),
          "error" -> getErrorDiv(commentErrorDivId),
          "newComment" -> SHtml.textarea(newComment.content, newComment.content = _),
          // submit event calls the doSaveComment function
          "submit" -> SHtml.ajaxSubmit(?("save"), () => doSaveComment(newComment)),
          // the cancel link hides the commentslist
          "cancel" -> SHtml.a(() => Hide(commentErrorDivId) &
                  JqSetHtml(blogEntryDivId + entry.id, listEntries(entryTemplate, List(entry))), Text(?("cancel")), "class" -> "button"))
      }
      // updates the clients screen: show the commentslist and the comment form
      JqSetHtml(commentDivId + entry.id, renderComments) & JqSetHtml(commentFormDivId + entry.id, renderNewCommentForm)
    }

    /**
     * Ajax-Link Callback function. This function removes a blog entry and updates the clients screen
     * @params entry The blog entry to remove
     */
    def doRemoveBlogEntry(entry: BlogEntry): JsCmd = {
      val e = Model.merge(entry)
      Model.removeAndFlush(e)
      // fire DeleteEntry event on the LiftActot and pass on the removed entry
      BlogCache.cache ! DeleteEntry(e)
      // remove the div of the respective blog entry
      myJqRemove(blogEntryDivId + entry.id)
    }

    /**
     * Function that renders a list of all blog entries and returns them as xml tree
     * @param html Markup code to perform the binding on
     * @param entries List of blog entries to render
     */
    def listEntries(html: NodeSeq, entries: List[BlogEntry]): NodeSeq = {
      /**
       * Returns a xml fragement containing direct links to the tour if the
       * blog entry belongs to one.
       * @param entry Blog entry for which to produce the links
       */
      def belongsTo(entry: BlogEntry): NodeSeq = {
        if (entry.tour == null) {
          NodeSeq.Empty
        } else {
          Text(?("blog.belongsTo") + " ") ++ SHtml.link("/tour/view", () => tourVar(entry.tour), Text(entry.tour.name))
        }
      }
      entries.flatMap(entry => bind("entry", html,
        // The FuncAttrBindParam binds a attribute of a xhtml element
        // the entry div id is bound to the right entry id
        FuncAttrBindParam("id", _ => Text(blogEntryDivId + entry.id), "id"),
        "title" -> entry.title,
        "tour" -> belongsTo(entry),
        "content" -> entry.content,
        "edit" -> SHtml.a(() => doEditBlogEntry(entry), Text(?("edit"))),
        "comments" -> SHtml.a(() => doComments(entry), Text(?("blog.comments"))),
        "remove" -> SHtml.a(() => doRemoveBlogEntry(entry), Text(?("remove"))),
        "preview" -> entry.content.substring(0, math.min(entry.content.length, 50)),
        "readOn" -> SHtml.link("/blog/view", () => blogEntryVar(entry), Text(?("blog.readOn"))),
        "lastUpdated" -> new SimpleDateFormat("dd.MM.yyyy HH:mm").format(entry.lastUpdated),
        "creator" -> entry.owner.name,
        // The FuncAttrBindParam binds a attribute of a xhtml element
        // the comments div is bound to the right entry id
        FuncAttrBindParam("commentsId", _ => Text(commentDivId + entry.id), "id"),
        // The FuncAttrBindParam binds a attribute of a xhtml element
        // the comments form div is bound to the right entry id
        FuncAttrBindParam("commentFormId", _ => Text(commentFormDivId + entry.id), "id")))
    }

    /**
     * Returns a xhtml fragement to render a form to create a new entry.
     * @param e A blog entry instance to bind the input fields to
     * @param html The markup code to bind
     * @param submitFunc function object to execute on submit
     * @param cancelFunc function object to execute on cancel
     */
    def getEntryForm(e: BlogEntry, html: NodeSeq, submitFunc: () => JsCmd, cancelFunc: () => JsCmd): NodeSeq = {
      // read tours by owner from the database to provide them in a select box
      val tours = Model.createNamedQuery[Tour]("findTourByOwner").setParams("owner" -> UserManagement.currentUser).findAll.toList
      // prepare a list for the select box to choose the tour from
      // items are {id,tourname} tuples
      val choices = List("" -> ("- " + S.?("none") + " -")) ::: tours.map(tour => (tour.id.toString -> tour.name)).toList
      bind("entry", SHtml.ajaxForm(html),
        "error" -> getErrorDiv(entryErrorDivId),
        "title" -> SHtml.text(e.title, e.title = _),
        "content" -> SHtml.textarea(e.content, e.content = _),
        "tour" -> SHtml.select(choices, if (e.tour == null) Empty else Full(e.tour.id.toString), (tourId: String) => {
          if (tourId != "") e.tour = Model.getReference(classOf[Tour], tourId.toLong) else e.tour = null
        }),
        "owner" -> SHtml.text(e.owner.name, e.owner.name = _),
        "submit" -> SHtml.ajaxSubmit(?("save"), submitFunc),
        "cancel" -> SHtml.a(cancelFunc, Text(?("cancel")), "class" -> "button"))
    }

    /**
     * Provides a link to create a new blog entry and provides the backing functionality
     */
    def doNewEntry() = {
      /**
       * Persist the given entry to the database and provides JavaScript Command for clientside feedback
       * @param entry Blog entry to persist
       */
      def save(entry: BlogEntry): JsCmd = {
        // only proceed if valid entry
        if (Validator.is_valid_entity_?(entry)) {
          val merged = Model.mergeAndFlush(entry)
          // Fire the addEntry event to the LiftActor and pass on the saved blog entry
          BlogCache.cache ! AddEntry(merged)
          // provide clientside feedback via ajax using JavaScript Commands
          Hide(entryErrorDivId) &
                  Hide(entryFormDivId) &
                  Show(newEntryLink) &
                  AppendHtml(entriesDivId, listEntries(entryTemplate, List(merged)))
        } else {
          // if entry is invalid show Lift error messages
          Show(entryErrorDivId)
        }
      }

      /**
       * Provides the xhtml for the new blog entry form
       * @param html Markup code to bind form elements to
       */
      def addEntryForm(html: NodeSeq): NodeSeq = {
        val e = new BlogEntry
        e.owner = UserManagement.currentUser
        e.lastUpdated = TimeHelpers.now
        getEntryForm(e, html, () => save(e), () => Hide(entryFormDivId) & Hide(entryErrorDivId) & Show(newEntryLink))
      }

      // return the new blog entry link and attach the JavaScript commands for the desired client side feedback
      SHtml.a(
        () => Hide(newEntryLink) & Show(entryFormDivId) & JqSetHtml(entryFormDivId, addEntryForm(entryFormTemplate)),
        Text(?("blog.addEntry")),
        "class" -> "button")
    }

    /**
     * Renders a list containig blog entries by the current user
     * @param html The markup code to bind the lisst to
     */
    def listOwnEntries(html: NodeSeq): NodeSeq = {
      val entries = Model.createNamedQuery[BlogEntry]("findEntriesByOwner").setParams("owner" -> UserManagement.currentUser).findAll.toList
      listEntries(html, entries)
    }

    // binds some tags to the main container divs of the single page application
    bind("ajax", html,
      "entriesList" -> <div id={entriesDivId}>
        {listOwnEntries(entryTemplate)}
      </div>,
      "template" -> NodeSeq.Empty,
      "newEntry" -> <div id={newEntryLink} class="content">
        {doNewEntry}
      </div>,
      "newEntryForm" -> <div id={entryFormDivId}></div>)
  }


  /**
   * The following code is to provide traditional multi page support for blog entries and comments
   */

  /**
   * access method for the request var
   */
  def blogEntry = blogEntryVar.is

  /**
   * Removes a given blog entry from the database
   * @param entry Blog entry to remove
   */
  def removeBlogEntry(entry: BlogEntry) {
    val e = Model.merge(entry)
    Model.remove(e)
    // fire the DeleteEntry event to the LiftActor and pass on the removed entry
    BlogCache.cache ! DeleteEntry(e)
    S.redirectTo("/blog/list")
  }

  /**
   * Render method for the edit blog entry page containing also the necessary backing logic
   * @param html Markup code to bind the form to
   */
  def editBlogEntry(html: NodeSeq): NodeSeq = {
    /**
     * Update the entry in the database
     */
    def doEdit() = {
      // proceed only if valid entry
      if (Validator.is_valid_entity_?(blogEntry)) {
        val newEntry = Model.mergeAndFlush(blogEntry)
        // fire the AddEntry event to the LiftActor and pass on the updated entry
        BlogCache.cache ! AddEntry(newEntry)
        S.redirectTo("/blog/list")
      }
    }

    val currentEntry = blogEntry

    currentEntry.owner = UserManagement.currentUser
    currentEntry.lastUpdated = TimeHelpers.now

    // read tours by owner from the database to provide them in a select box
    val tours = Model.createNamedQuery[Tour]("findTourByOwner").setParams("owner" -> UserManagement.currentUser).findAll.toList
    // prepare a list for the select box to choose the tour from
    // items are {id,tourname} tuples
    val choices = List("" -> ("- " + S.?("none") + " -")) ::: tours.map(tour => (tour.id.toString -> tour.name)).toList

    bind("entry", html,
      "title" -> SHtml.text(currentEntry.title, currentEntry.title = _),
      "content" -> SHtml.textarea(currentEntry.content, currentEntry.content = _),
      "tour" -> SHtml.select(choices, if (currentEntry.tour == null) Empty else Full(currentEntry.tour.id.toString), (tourId: String) => {if (tourId != "") currentEntry.tour = Model.getReference(classOf[Tour], tourId.toLong) else currentEntry.tour = null}),
      "owner" -> SHtml.text(currentEntry.owner.name, currentEntry.owner.name = _),
      "submit" -> SHtml.submit(?("save"), () => {blogEntryVar(currentEntry); doEdit}))
  }

  /**
   * Render method for displaying a single blog entry
   * @param html The markup code to bind the entry to
   */
  def showEntry(html: NodeSeq): NodeSeq = {
    val currentEntry = blogEntry
    listEntries(html, List(blogEntry))
  }

  /**
   * Helper method for rendering a list of blog entries
   * @param html The makup code to bind the list to
   * @param entries A list of blog entries to display
   */
  def listEntries(html: NodeSeq, entries: List[BlogEntry]): NodeSeq = {
    entries.flatMap(entry => bind("entry", html,
      "title" -> entry.title,
      "tour" -> {
        // if the entry is bound to a tour provide a direct link to the tour
        if (entry.tour == null) {
          NodeSeq.Empty
        } else {
          Text(?("blog.belongsTo") + " ") ++ SHtml.link("/tour/view", () => tourVar(entry.tour), Text(entry.tour.name))
        }
      },
      "content" -> entry.content,
      "edit" -> SHtml.link("/blog/edit", () => blogEntryVar(entry), Text(?("edit"))),
      "comments" -> SHtml.link("/blog/view", () => blogEntryVar(entry), Text(?("blog.comments"))),
      "remove" -> SHtml.link("/blog/remove", () => removeBlogEntry(entry), Text(?("remove"))),
      "preview" -> entry.content.substring(0, math.min(entry.content.length, 50)),
      "readOn" -> SHtml.link("/blog/view", () => blogEntryVar(entry), Text(?("blog.readOn"))),
      "lastUpdated" -> new SimpleDateFormat("dd.MM.yyyy HH:mm").format(entry.lastUpdated),
      "creator" -> entry.owner.name))
  }

  /**
   * Render method to list all blog entries belonging to a specific tour
   * @param html Markupcode to bind the list to
   */
  def showBlogEntriesFromTour(html: NodeSeq): NodeSeq = {
    val currentTour = tourVar.is
    val entries = Model.createNamedQuery[BlogEntry]("findEntriesByTour").setParams("tour" -> currentTour).findAll.toList
    listEntries(html, entries)
  }

  /**
   * Render method to list all blog entries belonging to other users
   * @param html Markupcode to bind the list to
   */
  def listOtherEntries(html: NodeSeq): NodeSeq = {
    val entries = Model.createNamedQuery[BlogEntry]("findEntriesByOthers").setParams("owner" -> UserManagement.currentUser).findAll.toList
    listEntries(html, entries)
  }

  /**
   * Render method to list all blog entries belonging to the current user
   * @param html Markupcode to bind the list to
   */
  def listOwnEntries(html: NodeSeq): NodeSeq = {
    val entries = Model.createNamedQuery[BlogEntry]("findEntriesByOwner").setParams("owner" -> UserManagement.currentUser).findAll.toList
    listEntries(html, entries)
  }

  /**
   * Render method for the add comment for including the backing functionality
   * @param html The markupcode to bind the form to
   */
  def addComment(html: NodeSeq): NodeSeq = {
    /**
     * Inner function to add a comment to the database
     * @param c The comment to add
     */
    def doAdd(c: Comment) = {
      // only add if valid comment
      if (Validator.is_valid_entity_?(c))
        Model.mergeAndFlush(c)
    }

    val currentEntry = blogEntry
    val newComment = new Comment
    newComment.blogEntry = blogEntry
    newComment.member = UserManagement.currentUser
    newComment.dateCreated = TimeHelpers.now

    // bind the form
    bind("comment", html,
      "content" -> SHtml.textarea(newComment.content, newComment.content = _),
      "submit" -> SHtml.submit(?("save"), () => {
        blogEntryVar(currentEntry);
        doAdd(newComment)
      }))
  }

  /**
   * Removes a given comment
   * @param comment The comment to remove from the database
   */
  def doRemoveComment(comment: Comment) {
    val c = Model.merge(comment)
    Model.remove(c)
    S.redirectTo("/blog/view", () => blogEntryVar(c.blogEntry))
  }

  /**
   * Renders a list of comments
   * @param html The markup code to bind the list to
   */
  def showComments(html: NodeSeq): NodeSeq = {
    val comments = Model.createNamedQuery[Comment]("findCommentsByEntry").setParams("entry" -> blogEntry).findAll.toList
    comments.flatMap(comment =>
      bind("comment", html,
        "member" -> comment.member.name,
        "dateCreated" -> new SimpleDateFormat("dd.MM.yyyy HH:mm").format(comment.dateCreated),
        "content" -> comment.content,
        "options" -> {
          // provide remove link if user has the respective privilegess
          if ((comment.member == UserManagement.currentUser) || (blogEntry.owner == UserManagement.currentUser))
            bind("link", chooseTemplate("option", "list", html), "remove" -> SHtml.link("remove", () => {
              blogEntryVar(comment.blogEntry);
              doRemoveComment(comment)
            }, Text(?("remove"))))
          else
            NodeSeq.Empty
        }))
  }
}