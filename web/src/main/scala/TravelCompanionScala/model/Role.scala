package TravelCompanionScala.model

import collection.immutable.Nil

/**
 * Created by IntelliJ IDEA.
 * User: rmuri
 * Date: 31.03.2010
 * Time: 15:09:30
 * To change this template use File | Settings | File Templates.
 */

class Role {

  var name : String = _

  var permissions : List[Permission] = _

}

class Permission {

  var name : String = _

  override def equals(o : Any) : Boolean = {
    true
  }
}