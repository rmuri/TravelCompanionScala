package TravelCompanionScala.model

import _root_.net.liftweb.util._
import Helpers._;
import xml.NodeSeq

/**
 * Created by IntelliJ IDEA.
 * User: dhobi
 * Date: 23.03.2010
 * Time: 09:08:05
 * To change this template use File | Settings | File Templates.
 */

class Login  {

  def login(xhtml: NodeSeq) = {
        bind("f",xhtml,
          "username" -> text("", println(_)),
          "password" -> text("", prinln(_)))
  }

}