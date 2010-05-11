package TravelCompanionScala {
package model {

import javax.persistence._
import _root_.java.util._
import org.hibernate.validator.constraints._
import javax.validation.constraints._

/**
 * Created by IntelliJ IDEA.
 * User: dhobi
 * Date: 13.04.2010
 * Time: 09:19:50
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "stages")
class Stage {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column(name = "description")
  var description: String = ""

  @Column(name = "name")
  @NotEmpty
  var name: String = ""

  @Temporal(TemporalType.DATE)
  @Column(name = "startdate")
  @NotNull
  var startdate: Date = null;

  @OneToOne
  @NotNull
  var destination: Location = null;

  @ManyToOne
  @NotNull
  var tour: Tour = null
}


}
}