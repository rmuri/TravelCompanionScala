package TravelCompanionScala {
package model {

import javax.persistence._
import _root_.java.util._
import org.hibernate.validator.constraints._
import javax.validation.constraints._

/**
 * Created by IntelliJ IDEA.
 * User: dhobi
 * Date: 08.04.2010
 * Time: 15:57:00
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "tours")
class Tour {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column(name = "name")
  @NotEmpty
  var name: String = ""

  @Column(name = "description")
  var description: String = ""

  @ManyToOne
  @NotNull
  var owner: Member = null

  @OneToMany(mappedBy = "tour", cascade = Array(CascadeType.ALL), targetEntity = classOf[Stage])
  var stages: List[Stage] = new ArrayList[Stage]()

  @OneToMany(mappedBy = "tour", cascade = Array(CascadeType.ALL), targetEntity = classOf[BlogEntry])
  var blogEntries: List[BlogEntry] = new ArrayList[BlogEntry]()

}


}
}