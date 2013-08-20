package controllers.cadmin

import play.api.test.FakeRequest
import controllers.BaseRequest

case class AdminTestRequest[A <: play.api.mvc.AnyContent](fake: FakeRequest[A]) extends BaseRequest(fake)