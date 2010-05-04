package TravelCompanionScala.model

import net.liftweb.http._
import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.util.Helpers

/**
 * Created by IntelliJ IDEA.
 * User: rmuri
 * Date: 27.04.2010
 * Time: 08:43:29
 * To change this template use File | Settings | File Templates.
 */

object ImageLogic {
  object TestImage {
    def unapply(in: String): Option[Picture] =
      Model.find(classOf[Picture], in.toLong)
  }

  def matcher: LiftRules.DispatchPF = {
    case req@Req("image" :: "thumbnail" :: TestImage(img) :: Nil, _, GetRequest) => () => serveImage(img, req, true)
    case req@Req("image" :: "full" :: TestImage(img) :: Nil, _, GetRequest) => () => serveImage(img, req, false)
  }

  def serveImage(img: Picture, req: Req, thumbnail: Boolean): Box[LiftResponse] = {
    val imageData: Array[Byte] = if (thumbnail) img.thumbnail else img.image
    Full(InMemoryResponse(imageData, List("Last-Modified" -> toInternetDate(Helpers.millis),
      "Content-Type" -> img.imageType,
      "Content-Length" -> imageData.length.toString),
      Nil, 200))
  }

}