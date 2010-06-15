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
    em.getTransaction.begin

    //Member

    val member = new Member
    member.name = "supertestuser111"
    member.password = "1234"
    member.email = "dhobi@hsr.ch"
    member.forename = "Hobi"

    em.persist(member)


    ///Tour

    val tour = new Tour
    tour.name = "My Travel"
    tour.description = "description"
    tour.owner = member


    em.persist(tour)
    em.getTransaction.commit
    em.close

    /////assert
    em = emf.createEntityManager()
    em.getTransaction.begin

    val retrieved = em.createQuery("SELECT t from Tour t where t.name = :name").setParameter("name","My Travel").getResultList.asInstanceOf[java.util.List[Tour]]

    assertEquals("My Travel", retrieved.get(0).name)
    println("Found " + retrieved.get(0).name)

    assertEquals("Hobi",retrieved.get(0).owner.forename)
    println("Found member " + retrieved.get(0).owner.forename)

    //cleanup


    em.remove(em.getReference(classOf[Member],member.id))
    em.remove(em.getReference(classOf[Tour],tour.id))
    em.getTransaction.commit
    em.close
  }

  @Test
  def collection () = {

    var em = emf.createEntityManager()
    val tx = em.getTransaction()

    tx.begin()

    val member = new Member
    member.forename = "Hobi2"
    member.name = "supertestuser111"
    member.password = "1234"
    member.email = "dhobi@hsr.ch"

    em.persist(member)

    val tour = new Tour
    tour.name = "My Travel"
    tour.description = "description"
    tour.owner = member

    em.persist(tour)

    val tour2 = new Tour
    tour2.name = "My Travel 2"
    tour2.description = "description 2"
    tour2.owner = member
    em.persist(tour2)

    tx.commit()
    em.close()


    ///assert
    em = emf.createEntityManager()
    val retrieved = em.createQuery("SELECT m from Member m where m.forename = :forename").setParameter("forename","Hobi2").getResultList.asInstanceOf[java.util.List[Member]]

    assertEquals("Hobi2", retrieved.get(0).forename)
    println("Found " + retrieved.get(0).forename)

     ///cleanup
    em.getTransaction().begin()

    em.remove(em.getReference(classOf[Tour],tour.id))
    em.remove(em.getReference(classOf[Tour],tour2.id))
    em.remove(em.getReference(classOf[Member],member.id))

    em.getTransaction().commit()

    em.close()
  }
}

}
}
