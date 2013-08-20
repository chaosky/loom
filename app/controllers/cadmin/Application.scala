package controllers.cadmin

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import loom.models._
import loom.utils.ConstraintsUtil
import loom.models.admin.Admin
import controllers._
import views._

/**
 * @author chaosky
 */
object Application extends Controller with BaseController with Secured {

  val loginForm = Form(tuple(
    "email" -> ConstraintsUtil.emailAndLen,
    "password" -> text
  ).verifying("m.login.invalid", result =>
    result match {
      case (email, password) => Admin.authenticate(email, password)
    }))

  /**
   * Login page.
   */
  def login = RichAction { implicit request =>
    Ok(html.admin.login(loginForm))
  }

  /**
   * Handle login form submission.
   */
  def authenticate = RichAction { implicit request =>
    val bindForm = loginForm.bindFromRequest()
    bindForm.fold(
      formWithErrors => {
        BadRequest(html.admin.login(formWithErrors))
      },
      account => {
        Redirect(RCall(routes.Application.index())).withSession(Model.admin_loginname -> account._1)
      }
    )
  }

  // -- Authentication
  def index = AdminAction { implicit request =>
    Ok(html.admin.index())
  }

  /**
   * Logout and clean the session.
   */
  def logout = RichAction { implicit request =>
    Redirect(routes.Application.login).withNewSession.flashing(
      "success" -> "m.login.logout.success"
    )
  }
}