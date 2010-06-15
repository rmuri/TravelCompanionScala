package TravelCompanionScala.model

import net.liftweb.http._
import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.util.Helpers

/**
 * ImageLogic Object implements functionality which is necessary provide pictures taken out of the database
 * to a requesting client. A client request on a specifc url containing the id of a picture will delivier the
 * respective image from the database.
 * URL Rewriting is used to dispatch to the correct url and LiftResponse to return a image to the client.
 *
 * Further Information on URL Rewriting can be found on:
 * - http://www.assembla.com/wiki/show/liftweb/URL_Rewriting
 * - Technologiestudium (github link) Chapter 5.4 [German]
 *
 * Further Information on generation of http Responses can be found on:
 * - "The Definitive Guide to Lift" Chapter "Exploring LiftResponse in Detail" on
 *    http://books.google.com/books?id=5lPmFLC6sHAC&pg=PA122
 *
 * @author Ralf Muri
 *
 */
object ImageLogic {
  /**
   * Extractor: extracts a picture from the database to a given id
   * @param in Id of the desired picture
   */
  object TestImage {
    def unapply(in: String): Option[Picture] =
      Model.find(classOf[Picture], in.toLong)
  }

  /**
   * Function that defines the dispatch logic for the following URLs:
   * /image/thumbnail/<id>
   * /image/full/<id>
   */
  def matcher: LiftRules.DispatchPF = {
    case req@Req("image" :: "thumbnail" :: TestImage(img) :: Nil, _, GetRequest) => () => serveImage(img, req, true)
    case req@Req("image" :: "full" :: TestImage(img) :: Nil, _, GetRequest) => () => serveImage(img, req, false)
  }

  /**
   * This method generates a LiftResponse which represents a http response. The created response contains the
   * byte data of the requested image.
   * @param img The requested picture
   * @param req The http request
   * @param thumbnail : Boolean indicating whether to show the thumbnail or the full image
   */
  def serveImage(img: Picture, req: Req, thumbnail: Boolean): Box[LiftResponse] = {
    val imageData: Array[Byte] = if (thumbnail) img.thumbnail else img.image
    Full(InMemoryResponse(imageData, List("Last-Modified" -> toInternetDate(Helpers.millis),
      "Content-Type" -> img.imageType,
      "Content-Length" -> imageData.length.toString),
      Nil, 200))
  }

}