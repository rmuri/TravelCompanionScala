package TravelCompanionScala {
package model {

import javax.persistence._
import _root_.java.util._
// Could be used for JSR 303 Validation
import org.hibernate.validator.constraints.{NotEmpty, Email}

/**
 * Created by IntelliJ IDEA.
 * User: dhobi
 * Date: 08.04.2010
 * Time: 16:09:06
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "members")
class Member() {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column(name = "city")
  var city: String = ""

  @Column(name = "email")
  @Email
  var email: String = ""

  @Column(name = "forename")
  var forename: String = ""

  @Column(name = "name")
  @NotEmpty
  var name: String = ""

  @Column(name = "password")
  @NotEmpty
  var password: String = ""

  @Column(name = "street")
  var street: String = ""

  @Column(name = "surname")
  var surname: String = ""

  @Column(name = "zipcode")
  var zipcode: String = ""

  @OneToMany(mappedBy = "owner", cascade = Array(CascadeType.ALL), targetEntity = classOf[Tour])
  var tours: List[Tour] = new ArrayList[Tour]()

  @OneToMany(mappedBy = "owner", cascade = Array(CascadeType.ALL), targetEntity = classOf[BlogEntry])
  val blogEntries: List[BlogEntry] = new ArrayList[BlogEntry]()

  @OneToMany(mappedBy = "owner", cascade = Array(CascadeType.ALL))
  val pictures: List[Picture] = new ArrayList[Picture]()

  @ManyToMany(cascade = Array(CascadeType.ALL))
  @JoinTable(name = "member_roles", joinColumns = Array(new JoinColumn(name = "member", referencedColumnName = "id")), inverseJoinColumns = Array(new JoinColumn(name = "roles", referencedColumnName = "id")))
  val roles: List[Role] = new ArrayList[Role]()

  override def equals(that: Any): Boolean = that match {
    case other: Member => id == other.id
    case _ => false
  }
}
}
}