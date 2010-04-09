package model

import javax.persistence._

/**
 * Created by IntelliJ IDEA.
 * User: dhobi
 * Date: 08.04.2010
 * Time: 15:57:00
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name="tour")
class Tour {

  @Id
  @Version
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id : Long = _

  @Column(name="name")
  val name : String = ""

  @Column(name="description")
  val description : String = ""

  @OneToOne
  @JoinColumn(name = "start_location_id")
  val start : Location = null

  @OneToOne
  @JoinColumn(name = "owner_id")
  var belongsTo : Member = null

}