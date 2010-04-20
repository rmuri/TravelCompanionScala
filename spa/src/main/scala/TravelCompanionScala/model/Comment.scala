package TravelCompanionScala.model

import javax.persistence._
import _root_.java.util._

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
  @Version
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column
  var content: String = ""

  @Column
  var member: Member = null

  @Temporal(TemporalType.DATE)
  @Column
  var dateCreated: Date = new Date()

  @ManyToOne
  var blogEntry: BlogEntry = null
}