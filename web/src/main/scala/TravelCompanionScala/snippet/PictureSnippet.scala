package TravelCompanionScala.snippet

import _root_.scala.xml.{NodeSeq, Text}

import _root_.net.liftweb._
import common.Empty
import http._
import S._
import util._
import Helpers._
import net.liftweb.common._

import TravelCompanionScala.model._

/**
 * Created by IntelliJ IDEA.
 * User: Ralf Muri
 * Date: 26.04.2010
 * Time: 11:06:04
 * To change this template use File | Settings | File Templates.
 */

class PictureSnippet {
  def doRemove(picture: Picture) {
    val p = Model.merge(picture)
    Model.remove(p)
    S.redirectTo("/picture/list")
  }

  var fileHolder: Box[FileParamHolder] = Empty

  def addPicture(html: NodeSeq): NodeSeq = {
    val currentPicture = new Picture

    def doSave(picture: Picture) {
      picture.owner = UserManagement.currentUser
      fileHolder match {
        case Full(FileParamHolder(_, mime, _, data))
          if (mime.startsWith("image/")) => {
          picture.image = data
          picture.imageType = mime
          Model.mergeAndFlush(picture)
          S.redirectTo("/picture/list")
        }
        case Full(_) => {
          S.error("Invalid Attachment")
          S.redirectTo("/picture/create")
        }
      }
    }

    bind("picture", html,
      "name" -%> SHtml.text(currentPicture.name, currentPicture.name = _),
      "file" -%> SHtml.fileUpload(fh => fileHolder = Full(fh)),
      "description" -%> SHtml.textarea(currentPicture.description, currentPicture.description = _),
      "tour" -> NodeSeq.Empty,
      "blogEntry" -> NodeSeq.Empty,
      "submit" -%> SHtml.submit(?("save"), () => doSave(currentPicture)))
  }

  def listPictures(html: NodeSeq, pictures: List[Picture]): NodeSeq = {
    pictures.flatMap(picture => bind("pictures", html,
      "thumbnail" -> picture.image.map(blob => InMemoryResponse(blob.im)),
      "description" -> picture.description,
      "owner" -> picture.owner.name,
      "belongsTo" -> NodeSeq.Empty,
      "remove" -> SHtml.link("remove", () => doRemove(picture), Text(?("remove")))))
  }

  def listOtherPictures(html: NodeSeq): NodeSeq = {
    val pictures = Model.createNamedQuery[Picture]("findPicturesByOthers").setParams("owner" -> UserManagement.currentUser).findAll.toList
    listPictures(html, pictures)
  }

  def listOwnPictures(html: NodeSeq): NodeSeq = {
    val pictures = Model.createNamedQuery[Picture]("findPicturesByOwner").setParams("owner" -> UserManagement.currentUser).findAll.toList
    listPictures(html, pictures)
  }
}