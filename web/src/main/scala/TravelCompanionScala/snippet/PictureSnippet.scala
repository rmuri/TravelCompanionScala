package TravelCompanionScala.snippet

import _root_.scala.xml.{NodeSeq, Text}

import _root_.net.liftweb._
import common._
import http._
import S._
import util._
import Helpers._
import net.liftweb.imaging._

import TravelCompanionScala.model._
import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, ByteArrayInputStream, InputStream}
import javax.imageio.ImageIO
import scala.collection.JavaConversions._

/**
 * The PictureSnippet class is responsible for the dynamic content on the pictures secion
 * of TravelCompanion. Pictures are CRUD Entities, this Snippet provides the necessary backing for
 * those operations.
 *
 * @author Ralf Muri
 *
 */

/**
 * RequestVar holding the current picture on which to operate. Used for intersnippet communication.
 */
object pictureVar extends RequestVar[Picture](new Picture)

class PictureSnippet {
  /**
   * Removes the given picture from the database
   * @param picture The picture to remove from the database
   */
  def doRemove(picture: Picture) {
    val p = Model.merge(picture)
    Model.remove(p)
    S.redirectTo("/picture/list")
  }

  /**
   * FileHolder var: contains information about the uploaded file
   * More information on file uploading can be found in:
   * - "The Definitive Guide to Lift" Chapter "Uploading Files" at
   *    http://books.google.com/books?id=5lPmFLC6sHAC&pg=PA58
   */
  var fileHolder: Box[FileParamHolder] = Empty

  /**
   * Snippet Method for the upload picture form and the related functionality
   * @param html Markup Code from the template to bind
   */
  def addPicture(html: NodeSeq): NodeSeq = {
    val currentPicture = new Picture

    /**
     * This function creates a thumbnail for a given image with a size of 125x125.
     * @see BufferedImage
     * @see ImageResizer
     *
     * @param data ByteArray of the image data uploaded
     */
    def createThumbnail(data: Array[Byte]): Array[Byte] = {
      // using java libary classes for providing a image byte stream
      val in: InputStream = new ByteArrayInputStream(data)
      val original: BufferedImage = ImageIO.read(in)
      // using Lift's ImageResizer to create the thumbnail
      val thumbnail: BufferedImage = ImageResizer.square(None, original, 125)
      val baos: ByteArrayOutputStream = new ByteArrayOutputStream()
      ImageIO.write(thumbnail, "jpg", baos)
      // return the thumbnail as ByteArray
      baos.toByteArray
    }

    /**
     * Inner function to save a picture to the database. The function checks if a picture was uploaded
     * in addition to the provided meta information
     * @param picture The picture entity to be saved to the database
     */
    def doSave(picture: Picture) {
      picture.owner = UserManagement.currentUser
      // check if a file was uploaded and if it fulfils the requirements
      fileHolder match {
      // if it is a image add the binary data to the domain entity
        case Full(FileParamHolder(_, mime: String, _, data))
          if (mime.startsWith("image/")) => {
          picture.thumbnail = createThumbnail(data)
          picture.image = data
          picture.imageType = mime
        }
        // no valid picture was provided
        case Full(_) => {
          S.error("Invalid Attachment")
          S.redirectTo("/picture/create")
        }
        case Empty => {
          S.error("Invalid Attachment")
          S.redirectTo("/picture/create")
        }
        case Failure(_, _, _) => {
          S.error("Invalid Attachment")
          S.redirectTo("/picture/create")
        }
      }
      // validate the entity
      if (Validator.is_valid_entity_?(picture)) {
        Model.mergeAndFlush(picture)
        S.redirectTo("/picture/list")
      }
    }

    // read tours by owner from the database to provide them in a select box
    val tours = Model.createNamedQuery[Tour]("findTourByOwner").setParams("owner" -> UserManagement.currentUser).findAll.toList
    // prepare a list for the select box to choose the tour from
    // items are {id,tourname} tuples
    val tchoices = List("" -> ("- " + S.?("none") + " -")) ::: tours.map(tour => (tour.id.toString -> tour.name)).toList

    // read blog entries by owner from the database to provide them in a select box
    val entries = Model.createNamedQuery[BlogEntry]("findEntriesByOwner").setParams("owner" -> UserManagement.currentUser).findAll.toList
    // prepare a list for the select box to choose the blog entry from
    // items are {id,title} tuples
    val echoices = List("" -> ("- " + S.?("none") + " -")) ::: entries.map(entry => (entry.id.toString -> entry.title)).toList

    // bind the tags from the template
    bind("picture", html,
      "name" -%> SHtml.text(currentPicture.name, currentPicture.name = _),
      "file" -%> SHtml.fileUpload(fh => fileHolder = Full(fh)),
      "description" -%> SHtml.textarea(currentPicture.description, currentPicture.description = _),
      "tour" -> SHtml.select(tchoices, Empty, (tourId: String) => {if (tourId != "") currentPicture.tour = Model.getReference(classOf[Tour], tourId.toLong) else currentPicture.tour = null}),
      "blogEntry" -> SHtml.select(echoices, Empty, (entryId: String) => {if (entryId != "") currentPicture.blogEntry = Model.getReference(classOf[BlogEntry], entryId.toLong) else currentPicture.blogEntry = null}),
      "submit" -%> SHtml.submit(?("save"), () => doSave(currentPicture)))
  }

  /**
   * Snippet Method for showing pictures that belong to a certain tour
   * @param html Markup Code from the template to bind
   */
  def showPicturesFromTour(html: NodeSeq): NodeSeq = {
    val currentTour = tourVar.is
    val pictures = Model.createNamedQuery[Picture]("findPicturesByTour").setParams("tour" -> currentTour).findAll.toList
    listPictures(html, pictures)
  }

  /**
   * Snippet Method showing a single picture
   * @param html Markup Code from the template to bind
   */
  def showPicture(html: NodeSeq): NodeSeq = {
    listPictures(html, List(pictureVar.is))
  }

  /**
   * Snippet Method displaying a list of pictures
   * @param html Markup Code from the template to bind
   * @param pictures The pictures to list
   */
  def listPictures(html: NodeSeq, pictures: List[Picture]): NodeSeq = {
    pictures.flatMap(picture => bind("picture", html,
      "thumbnail" -> SHtml.link("/picture/view", () => pictureVar(picture), <img src={"/image/thumbnail/" + picture.id}/>),
      "image" -%> <img src={"/image/full/" + picture.id}/>,
      "name" -> picture.name,
      "description" -> picture.description,
      "owner" -> picture.owner.name,
      "belongsTo" -> {
        var n: NodeSeq = NodeSeq.Empty
        // if the picture belongs to a tour then add a direct link to this tour
        if (picture.tour != null)
          n = n ++ bind("link", chooseTemplate("choose", "belongsTo", html),
            "title" -> Text(S.?("tour")),
            "link" -> SHtml.link("/tour/view", () => tourVar(picture.tour), Text(picture.tour.name)))
        // if the picture belongs to a blog entry then add a direct link to this entry
        if (picture.blogEntry != null)
          n = n ++ bind("link", chooseTemplate("choose", "belongsTo", html),
            "title" -> Text(S.?("blog.entry")),
            "link" -> SHtml.link("/blog/view", () => blogEntryVar(picture.blogEntry), Text(picture.blogEntry.title)))
        // return the concatenated NodeSeq
        n
      },
      "remove" -> SHtml.link("remove", () => doRemove(picture), Text(?("remove")))))
  }

  /**
   * Snippet Method for showing pictures that belong to others
   * @param html Markup Code from the template to bind
   */
  def listOtherPictures(html: NodeSeq): NodeSeq = {
    val pictures = Model.createNamedQuery[Picture]("findPicturesByOthers").setParams("owner" -> UserManagement.currentUser).findAll.toList
    listPictures(html, pictures)
  }

  /**
   * Snippet Method for showing pictures that belongs to the current user
   * @param html Markup Code from the template to bind
   */
  def listOwnPictures(html: NodeSeq): NodeSeq = {
    val pictures = Model.createNamedQuery[Picture]("findPicturesByOwner").setParams("owner" -> UserManagement.currentUser).findAll.toList
    listPictures(html, pictures)
  }
}