package controllers.cadmin

import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import controllers.Msg
import loom.models.Account

/**
 *
 * @author chaosky
 */
class ApplicationSpec extends AdminBaseTest with loom.DBSpec {

  "admin login" should {
    "redirect to login page without credentials" in {
      val ret = route(FakeRequest(GET, routes.Application.index().url)).get
      redirectLocation_==(ret, Some(routes.Application.login().url))
    }
    "account disable" in {
      loginAndIndexSpec("disable_admin@example.com", "m.login.account.disable")
    }
    "account need verify email" in {
      loginAndIndexSpec("emailnotverified_admin@example.com", "m.login.account.need.verify.email")
    }
    "account need reset password" in {
      loginAndIndexSpec("resetpassword_admin@example.com", "m.login.account.need.reset.password")
    }
    "username is not email" in {
      loginSpec("username", msg = "error.email")
    }
    "username too lang" in {
      loginSpec("a".*(Account.emailMaxLen - 4) + "@c.cc", msg = "error.maxLength", args = Seq(Account.emailMaxLen))
    }
    "wrong username" in {
      loginSpec("admin1.wrong_admin@example.com", msg = "m.login.invalid")
    }
    "right username and wrong password" in {
      loginSpec("admin1@example.com", "wrong_pwd", "m.login.invalid")
    }
    "'+' in username" in {
      loginSpec("admin1+aliasname@example.com", "wrong_pwd", "m.login.invalid")
    }
    "logout" in {
      val loginret = route(login("admin1@example.com")).get
      val ret = route(requestWithCookies(loginret, GET, routes.Application.logout())).get
      redirectLocation_==(ret, Some(routes.Application.login().url))
      flash(ret).get("success") must_== Some("m.login.logout.success")
    }
    "account not found" in {
      implicit val req = requestByAdmin("notfound_admin@example.com", GET, routes.Application.index())
      val ret = route(req.fake).get
      status_==(ret, BAD_REQUEST)
      contentContain(ret, Msg("m.login.account.not.found"))
    }
  }

  def loginSpec(username: String, password: String = "example@example.com", msg: String, args: Seq[Any] = Nil) = {
    implicit val req = login(username, password)
    val ret = route(req.fake).get
    status_==(ret, BAD_REQUEST)
    contentContain(ret, Msg(msg, args: _*))
  }

  def loginAndIndexSpec(username: String, msg: String) = {
    implicit val req = login(username)
    val loginret = route(req.fake).get
    val ret = route(requestWithCookies(loginret, GET, routes.Application.index())).get
    status_==(ret, BAD_REQUEST)
    contentContain(ret, Msg(msg))
  }
}
