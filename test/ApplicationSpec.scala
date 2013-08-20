package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import loom.DBSpec

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends DBSpec {

  "Application" should {

    "send 404 on a bad request" in {
      route(FakeRequest(GET, "/boum")) must beNone
    }

  }
}