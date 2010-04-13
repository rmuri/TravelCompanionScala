package TravelCompanionScala {
package model {

import javax.persistence._
import _root_.java.util._
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
  @Version
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column(name = "name")
  var name: String = ""

  @Column(name = "description")
  var description: String = ""

  @ManyToOne
  var owner : Member = null

  @OneToMany(mappedBy = "tour",targetEntity = classOf[Stage])
  var stages : Set[Stage] = new HashSet[Stage]()

}


}
}