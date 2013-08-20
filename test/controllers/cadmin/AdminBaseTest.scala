package controllers.cadmin

import play.api.test._
import play.api.test.Helpers._
import loom.models.Model
import loom.BaseTest
import play.api.mvc.{Call, AnyContentAsFormUrlEncoded}

/**
 *
 * @author chaosky
 */
abstract class AdminBaseTest extends BaseTest {
  val admin_username = "admin1@example.com"

  def login(username: String, password: String = "example@example.com"): AdminTestRequest[AnyContentAsFormUrlEncoded] = {
    val fakereq = FakeRequest(POST, routes.Application.authenticate().url).withFormUrlEncodedBody(
      (Model.admin_loginname -> username), ("password" -> password)
    )

    AdminTestRequest(fakereq)
  }

  private def adminrequest(username: String, method: String, call: Call) = {
    FakeRequest(method, call.url).withSession(Model.admin_loginname -> username)
  }

  private def adminrequest(username: String, method: String, call: String) = {
    FakeRequest(method, call).withSession(Model.admin_loginname -> username)
  }

  def requestByAdmin(username: String, method: String, call: Call) = {
    val fake = adminrequest(username, method, call)
    AdminTestRequest(fake)
  }

  def requestAdmin(method: String, call: Call, data: Seq[(String, String)], username: String = admin_username) = {
    request(method, call.url, data, username)
  }

  def request(method: String, call: String): AdminTestRequest[AnyContentAsFormUrlEncoded] = {
    request(method, call, Nil, admin_username)
  }

  def request(method: String, call: String, data: Seq[(String, String)]): AdminTestRequest[AnyContentAsFormUrlEncoded] = {
    request(method, call, data, admin_username)
  }

  def request(method: String, call: String, data: Seq[(String, String)], username: String): AdminTestRequest[AnyContentAsFormUrlEncoded] = {
    val fake = if (method == GET)
      adminrequest(username, method, call).withFormUrlEncodedBody()
    else if (method == POST)
      adminrequest(username, method, call).withFormUrlEncodedBody(data: _*)
    else
      sys.error("unsupport method" + method)

    AdminTestRequest(fake)
  }

}

