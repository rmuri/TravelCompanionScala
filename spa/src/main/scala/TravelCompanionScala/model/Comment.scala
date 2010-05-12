package TravelCompanionScala {
package model {

import javax.persistence._
import _root_.java.util._
import javax.validation.constraints._
import org.hibernate.validator.constraints.NotEmpty

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
  @NotEmpty
  var content: String = ""

  @OneToOne
  @NotNull
  var member: Member = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  @NotNull
  var dateCreated: Date = null

  @ManyToOne
  @NotNull
  var blogEntry: BlogEntry = null

  override def equals(that: Any): Boolean = that match {
    case other: Comment => id == other.id
    case _ => false
  }
}

}
}