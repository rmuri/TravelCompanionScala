package TravelCompanionScala {
package model {

import _root_.org.scala_libs.jpa.LocalEMF
import _root_.net.liftweb.jpa.RequestVarEM
import javax.validation.{Validation, Validator}
import net.liftweb.http.S
import scala.collection.JavaConversions._

/**
 * The Model Object is used for accessing the database. Basically it is a direct ScalaJPA EntitiyManager.
 * A RequestVar is used as a backing store. Like this, every requests gets is own EntityManager Instance to
 * work with.
 *
 * Further information on the Model Object can be found on:
 * - http://groups.google.com/group/liftweb/browse_thread/thread/792cc7e0b0b5cbed/dbb89e8b020ffd22
 *
 * @author Ralf Muri
 */
object Model extends LocalEMF("jpaweb") with RequestVarEM

/**
 * The Validator Object can be used to validate domain entity instances. Hibernate Validator is used as
 * validation framework.
 *
 * Further information on Hibernate Validator can be found on:
 * - http://docs.jboss.org/hibernate/stable/validator/reference/en/html_single/
 * - Technologiestudium (github link) Chapter 4.4 [German]
 */
object Validator {
  /**
   * The validator instance is obtained through the ValidatorFacotry
   */
  def get: Validator = Validation.buildDefaultValidatorFactory.getValidator

  /**
   * This method takes a instance of a domain entity as parameter and runs validation on it.
   * @param toCheck The domain entity to validate
   */
  def is_valid_entity_?(toCheck: Object): Boolean = {
    // validate domain entity
    val validationResult = Validator.get.validate(toCheck)
    // put the validation result (constraint vialoations...) in the S.error listing to provide user feedback
    validationResult.foreach((e) => S.error(e.getPropertyPath + " " + e.getMessage))
    // return whether the validation was successful or not
    validationResult.isEmpty
  }
}

}
}

