package TravelCompanionScala {
package model  {

import javax.persistence._

/**
 * Created by IntelliJ IDEA.
 * User: rmuri
 * Date: 31.03.2010
 * Time: 15:09:30
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "roles")
class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column
  var name: String = ""

  override def equals(that: Any): Boolean = that match {
    case other: Role => id == other.id
    case _ => false
  }
}

}
}