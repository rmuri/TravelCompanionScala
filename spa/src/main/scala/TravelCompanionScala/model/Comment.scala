package TravelCompanionScala {
package model {

import javax.persistence._
import _root_.java.util._
import javax.validation.constraints._

/**
 * Created by IntelliJ IDEA.
 * User: Ralf Muri
 * Date: 16.04.2010
 * Time: 16:54:47
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "comments")
class Comment {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column
  var content: String = ""

  @OneToOne
  @NotNull
  var member: Member = null

  @Temporal(TemporalType.DATE)
  @Column
  @NotNull
  var dateCreated: Date = null

  @ManyToOne
  @NotNull
  var blogEntry: BlogEntry = null
}

}
}