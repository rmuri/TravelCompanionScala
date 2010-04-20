package TravelCompanionScala {
package model  {

import javax.persistence._
import _root_.java.util._

/**
 * Created by IntelliJ IDEA.
 * User: Ralf Muri
 * Date: 16.04.2010
 * Time: 16:48:01
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "blogentries")
class BlogEntry {
  @Id
  @Version
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column
  var title: String = ""

  @Column
  var content: String = ""

  @Temporal(TemporalType.DATE)
  @Column
  var lastUpdated: Date = new Date()

  @Column
  var published: Boolean = false

  @ManyToOne
  var tour: Tour = null

  @ManyToOne
  var owner: Member = null

  @OneToMany(mappedBy = "blogEntry", targetEntity = classOf[Comment])
  var comments: List[Comment] = new ArrayList[Comment]()
}

}
}