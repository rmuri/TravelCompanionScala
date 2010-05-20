package TravelCompanionScala {
package api {

import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.Assert._
import java.util.Date
import model.EntityConverter._
import scala.collection.JavaConversions._
import model.{Member, Tour, Comment, BlogEntry}
import xml.{Elem, Utility, NodeSeq}


class TestEntityConverter {
  var testEntry: BlogEntry = null
  val now: Date = new Date

  def getCorrectXml(e: BlogEntry): Elem = {
    <BlogEntry>
      <id>
        {e.id}
      </id>
      <title>
        {e.title}
      </title>
      <content>
        {e.content}
      </content>
      <lastUpdated>
        {e.lastUpdated}
      </lastUpdated>
      <published>
        {e.published}
      </published>
      <tour>
        {e.tour.id}
      </tour>
      <owner>
        {e.owner.id}
      </owner>
      <comments>
        {e.comments.flatMap(c => c.toXml)}
      </comments>
    </BlogEntry>
  }


  @Before
  def doInitialization {
    testEntry = new BlogEntry
    testEntry.id = 5
    testEntry.title = "Test Eintrag"
    testEntry.content = "The quick brown fox jumps over the lazy old dog."
    testEntry.lastUpdated = now
    testEntry.tour = new Tour
    testEntry.tour.id = 352
    testEntry.owner = new Member
    testEntry.owner.id = 301
    val c = new Comment
    c.id = 2501
    c.content = "The quick brown fox jumps over the lazy old dog."
    c.member = new Member
    c.dateCreated = now
    c.blogEntry = testEntry
    testEntry.comments.add(c)
  }

  @After
  def doCleanUp = {
    testEntry = null
  }

  @Test
  def convertXml() = {
    val asXml = testEntry.toXml
    assertEquals("XML", asXml.flatMap(n => Utility.trim(n)), getCorrectXml(testEntry).flatMap(n => Utility.trim(n)))
  }

  @Test
  def fromXml() = {
    val e = getCorrectXml(testEntry).entryFromXml
    assertEquals(testEntry.id, e.id)
    assertEquals(testEntry.title.trim, e.title.trim)
    assertEquals(testEntry.content.trim, e.content.trim)
    //    assertEquals(testEntry.lastUpdated, e.lastUpdated)
    //    assertEquals(testEntry.tour, e.tour)
    //    assertEquals(testEntry.owner, e.owner)
    //    assertEquals(testEntry.comments, e.comments)
  }

  @Test
  def convertJson() = {
    assertTrue(true)
  }


}

}
}
