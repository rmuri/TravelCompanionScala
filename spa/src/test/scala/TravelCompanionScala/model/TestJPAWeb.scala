package TravelCompanionScala {
package model {

import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.Assert._
import javax.persistence.{Persistence, EntityManagerFactory}

/**
 * Created by IntelliJ IDEA.
 * User: dhobi
 * Date: 09.04.2010
 * Time: 09:06:08
 * To change this template use File | Settings | File Templates.
 */

class TestJPAWeb {
  var emf : EntityManagerFactory = _

  @Before
  def initEMF () = {
    try {
      emf = Persistence.createEntityManagerFactory("jpaweb")
    } catch {
      case e: Exception => {
        def printAndDescend(ex : Throwable) : Unit = {
        println(e.getMessage())
          if (ex.getCause() != null) {
            printAndDescend(ex.getCause())
          }
        }
        printAndDescend(e)
      }
    }
  }

  @After
  def closeEMF () = {
    if (emf != null) emf.close()
  }


  @Test
  def save_stuff () = {

    var em = emf.createEntityManager()
    val tx = em.getTransaction()

    tx.begin()

    val member = new Member
    member.name = "Hobi"

    em.persist(member)

    val tour = new Tour
    tour.name = "My Travel"
    tour.description = "description"
    tour.owner = member


    em.persist(tour)

    tx.commit()

    em.close()

    /////assert
    em = emf.createEntityManager()

    val retrieved = em.createNamedQuery("findAllTours").getResultList().asInstanceOf[java.util.List[Tour]]

    assertEquals("My Travel", retrieved.get(retrieved.size-1).name)
    println("Found " + retrieved.get(retrieved.size-1).name)

    assertEquals("Hobi",retrieved.get(retrieved.size-1).owner.name)
    println("Found member " + retrieved.get(retrieved.size-1).owner.name)

    ///cleaup
    em.getTransaction().begin()

    em.remove(em.getReference(classOf[Tour],tour.id))
    em.remove(em.getReference(classOf[Member],member.id))

    em.getTransaction().commit()

    em.close()
  }

}

}
}
