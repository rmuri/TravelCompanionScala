package model {

import org.junit._
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
    em.close()
  }

}

}