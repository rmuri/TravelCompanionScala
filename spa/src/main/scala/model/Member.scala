package TravelCompanionScala {
package model {

import javax.persistence._
import _root_.java.util._
/**
 * Created by IntelliJ IDEA.
 * User: dhobi
 * Date: 08.04.2010
 * Time: 16:09:06
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name="members")
class Member  {

  @Id
  @Version
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id : Long = _

  @Column(name = "city")
  var city : String = ""

  @Column(name = "email")
  var email : String = ""

  @Column(name = "forename")
  var forename : String = ""

  @Column(name = "ame")
  var name : String = ""

  @Column(name = "password")
  var password : String = ""

  @Column(name = "street")
  var street: String = ""

  @Column(name = "sumame")
  var sumame : String = ""

  @Column(name = "zipcode")
  var zipcode : String = ""

  @OneToMany(mappedBy = "owner",targetEntity = classOf[Tour])
  var books : Set[Tour] = new HashSet[Tour]()
}

}
}