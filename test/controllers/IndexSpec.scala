package controllers

import org.specs2.mutable._
import play.api.mvc._
import play.api.Logger
import play.api.test._
import play.api.test.Helpers._
import play.api.i18n.Messages
import loom.models.{Account, Model}
import loom.BaseTest

/**
 *
 * @author chaosky
 */
class IndexSpec extends BaseTest with loom.DBSpec {

  "index" should {
    "redirect to login page without credentials" in {
      val result = route(FakeRequest(GET, "/")).get
      redirectLocation(result) must_== Some(routes.Index.login().url)
      flash(result).get("error") must_== Some("Unauthorized")
    }
    "login account disable" in {
      loginAndIndexSpec("disable_user@example.com", "m.login.account.disable")
    }
    "login account need verify email" in {
      loginAndIndexSpec("emailnotverified_user@example.com", "m.login.account.need.verify.email")
    }
    "login account need reset password" in {
      loginAndIndexSpec("resetpassword_user@example.com", "m.login.account.need.reset.password")
    }
    "login form username is not email" in {
      loginSpec("username", msg = "error.email")
    }
    "login form username too lang" in {
      loginSpec("a".*(Account.emailMaxLen - 4) + "@c.cc", msg = "error.maxLength", args = Seq(Account.emailMaxLen))
    }
    "login wrong username" in {
      loginSpec("user1.wrong_user@example.com", msg = "m.login.invalid")
    }
    "login right username and wrong password" in {
      loginSpec("user1@example.com", "wrong_pwd", "m.login.invalid")
    }
    "login with '+' in username" in {
      loginSpec("user1+aliasname@example.com", "wrong_pwd", "m.login.invalid")
    }
    "logout" in {
      val loginret = route(login("user1@example.com")).get
      val ret = route(requestWithCookies(loginret, GET, routes.Index.logout())).get
      redirectLocation(ret) must equalTo(Some(routes.Index.login().url))
      flash(ret).get("success") must equalTo(Some("m.login.logout.success"))
    }
    "login account not found" in {
      val ret = route(requestByUser("notfound_user@example.com", GET, routes.Index.index)).get
      status_==(ret, BAD_REQUEST)
      contentAsString(ret) must contain(Messages("m.login.account.not.found"))
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
    val ret = route(requestWithCookies(loginret, GET, routes.Index.index())).get
    status_==(ret, BAD_REQUEST)
    contentContain(ret, Msg(msg))
  }

  def login(username: String, password: String = "example@example.com") = {
    val fake = FakeRequest(POST, routes.Index.authenticate().url).withFormUrlEncodedBody(
      (Model.user_loginname -> username), ("password" -> password)
    )
    UserTestRequest(fake)
  }


  def requestByUser(username: String, method: String, call: Call) = {
    FakeRequest(method, call.url).withSession(Model.user_loginname -> username)
  }
}

