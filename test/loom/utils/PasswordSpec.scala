package loom.utils

import org.specs2.mutable._

/**
 * password spec
 * @author chaosky
 */
class PasswordSpec extends Specification {

  "password" should {

    "codecs" in {
      Codecs.sha1str("example@example.com", "salt", 1024) must equalTo("65c2bff865381253e594bcced67d1038b7397a72")
    }

  }
}
