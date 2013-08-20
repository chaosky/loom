package loom

import org.specs2.mutable._
import org.specs2.specification._
import play.api.Play
import play.api.test._

/**
 *
 * @author chaosky
 */
trait DBSpec extends Specification {

  override def map(fs: => Fragments) = Step(DBSpec.onstart) ^ fs ^ Step(DBSpec.onstop)

}

object DBSpec {
  def onstart = {
    Play.start(FakeApplication(additionalConfiguration = BaseTest.dbConf))
  }

  def onstop = {
    Play.stop
    play.api.libs.ws.WS.resetClient()
  }
}
