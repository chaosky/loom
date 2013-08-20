package loom.utils

import org.specs2.mutable._

import play.api.test.Helpers._
import loom.models._
import net.rubyeye.xmemcached.MemcachedClient
import loom.Global
import loom.models.admin.Admin
import loom.BaseTest._

/**
 *
 * @author chaosky
 */
class MemcachedSpec extends Specification {

  "memcached" should {

    "client must be none" in {
      running(fakeDBApplication(Map("memcached.servers" -> ""))) {
        Memcached.client must beNone
        Memcached.getOrElse(Global.cacheNameSpace + "test/Memcached/anyval")(
          1
        ) must equalTo(1)

        val u = Admin(1, "email", "name", "password", "salt", Status.None.id, new java.util.Date())
        Memcached.getOrElse(Global.cacheNameSpace + "test/Memcached/anyref")(u) must equalTo(u)

      }
    }

    "client must be provide" in {
      running(fakeDBApplication(Map("memcached.servers" -> "localhost:11211"))) {
        Memcached.client must beSome[MemcachedClient]
      }
    }
  }

}
