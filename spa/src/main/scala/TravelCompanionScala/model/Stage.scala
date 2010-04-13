package TravelCompanionScala {
package model  {


import javax.persistence._
import _root_.java.util._
/**
 * Created by IntelliJ IDEA.
 * User: dhobi
 * Date: 13.04.2010
 * Time: 09:19:50
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name="stages")
class Stage {

  @Id
  @Version
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id : Long = _

  @Column(name="description")
  var description : String = ""

  @Column(name="name")
  var name : String = ""

  @Temporal(TemporalType.DATE)
  @Column(name="startdate")
  var startdate : Date = new Date();

  @OneToOne 
  var destination : Location = null;

  @ManyToOne
  var tour : Tour = null
}


}
}