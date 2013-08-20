package controllers

import play.api.mvc.AnyContent
import play.api.test.FakeRequest

case class UserTestRequest[A <: AnyContent](fake: FakeRequest[A]) extends BaseRequest(fake)
