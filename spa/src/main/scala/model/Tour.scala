package TravelCompanionScala.model {

import javax.persistence._

/**
 * Created by IntelliJ IDEA.
 * User: dhobi
 * Date: 08.04.2010
 * Time: 15:57:00
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "tour")
class Tour {
  @Id
  @Version
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column(name = "name")
  val name: String = ""

  @Column(name = "description")
  val description: String = ""


}

object Tour {
  def apply(name: String, description: String): Tour = {
    var t = new Tour
    t.name = name
    t.description = description
  }
}

}