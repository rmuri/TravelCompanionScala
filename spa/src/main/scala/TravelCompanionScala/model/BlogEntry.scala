package TravelCompanionScala {
package model {

import javax.persistence._
import java.util.{Date, ArrayList}
import org.hibernate.validator.constraints._
import javax.validation.constraints._

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
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column
  @NotEmpty
  var title: String = ""

  @Column
  @NotEmpty
  var content: String = ""

  @Temporal(TemporalType.DATE)
  @Column
  @NotNull
  var lastUpdated: Date = null

  @Column
  var published: Boolean = false

  @ManyToOne
  @NotNull
  var tour: Tour = null

  @ManyToOne
  @NotNull
  var owner: Member = null

  @OneToMany(mappedBy = "blogEntry", cascade = Array(CascadeType.ALL), targetEntity = classOf[Comment])
  var comments: java.util.List[Comment] = new ArrayList[Comment]()
}

}
}