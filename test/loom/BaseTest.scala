package loom

import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import org.specs2.mutable.Specification
import loom.utils.Strings

/**
 *
 * @author chaosky
 */
object BaseTest {

  def dbConf: Map[String, String] = {
    inMemoryDatabase() + ("logger.com.jolbox" -> "ERROR")
  }

  def fakeDBApplication(additionalConf: Map[String, _ <: Any] = Map.empty) = {
    FakeApplication(additionalConfiguration = dbConf ++ additionalConf)
  }

}

abstract class BaseTest extends Specification {

  def status_==(ret: Result, statusI: Int) = {
    status(ret) must_== statusI
  }

  def redirectLocation_==(ret: Result, other: => scala.Any) = {
    redirectLocation(ret) must_== other
  }

  def requestWithCookies(ret: Result, method: String, call: Call) = {
    val cookie = cookies(ret)
    FakeRequest(method, call.url).withCookies(cookie(Session.COOKIE_NAME))
  }

  def contentContain(ret: Result, str: => String) = {
    contentAsString(ret) must contain(str)
  }

  def page(page: Int, pageSize: Int = 5): Seq[(String, String)] = {
    Seq("p" -> page.toString, "ps" -> pageSize.toString())
  }

  def url[A <: Any](call: Call, args: Seq[(String, A)]): String = {
    if (call.url.contains("?")) {
      call.url + Strings.addString(args, "&", "&", "")(t => t._1 + "=" + t._2)
    } else {
      call.url + Strings.addString(args, "?", "&", "")(t => t._1 + "=" + t._2)
    }
  }
}
