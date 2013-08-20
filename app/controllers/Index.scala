package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._

import loom.models._
import loom.utils.ConstraintsUtil
import views._
import loom.models.app.UserApp

/**
 *
 * @author chaosky
 */
object Index extends Controller with BaseController with Secured {

  val loginForm = Form(tuple(
    Model.user_loginname -> ConstraintsUtil.emailAndLen,
    "password" -> text
  ).verifying("m.login.invalid", result => {
    result match {
      case (username, password) => User.authenticate(username, password)
    }
  }))

  def login = RichAction { implicit request =>
    Ok(html.login(loginForm))
  }

  def logout = Action { implicit request =>
    Redirect(routes.Index.login).withNewSession.flashing(
      "success" -> "m.login.logout.success"
    )
  }

  def authenticate = RichAction { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithError => {
        BadRequest(html.login(formWithError))
      },
      post => {
        Redirect(RCall(routes.Index.index())).withSession(Model.user_loginname -> post._1)
      }
    )
  }

  def index = UserAction { implicit request =>
    val appIds: List[Long] = UserApp.findOne(request.user.id) match {
      case Some(ua) => ua.appIds
      case None => Nil
    }

    val appsAndUser = capp.Apps.appsAndUser(appIds)
    Ok(html.index(appsAndUser))
  }

  def page404() = Action { implicit request =>
    NotFound(html.error._404())
  }

  case class SignupUser(name: String, email: String, password: (String, String))

  val signupForms = Form(
    mapping(
      "name" -> nonEmptyText,
      "email" -> ConstraintsUtil.emailAndLen,
      "password" -> tuple(
        "new" -> ConstraintsUtil.passwordText(Account.pwdMinLen, Account.pwdMaxLen),
        "confirm" -> text
      ).verifying(Constraint {
        pwd: (String, String) =>
          if (pwd._1 != pwd._2) {
            Invalid(Seq(ValidationError("m.password.verifying.dont.match")))
          } else {
            Valid
          }
      })
    )(SignupUser.apply)(SignupUser.unapply)
  )

  def signupForm() = Action { implicit request =>
    Ok(html.signup(signupForms))
  }

  def signup() = Action { implicit request =>
    val bindForm = signupForms.bindFromRequest()
    bindForm.fold(
      formWithErrors => BadRequest(html.signup(formWithErrors)),
      user => {
        User.findOneByLoginName(user.email) match {
          case Some(u) => BadRequest(html.signup(bindForm.withGlobalError("m.account.error.email.registered")))
          case None => {
            val (ret, optuser, i18nMsg) = User.create(user.name, user.email, user.password._1)
            if (ret) {
              Redirect(routes.Index.login()).flashing(
                "success" -> i18nMsg
              )
            } else {
              BadRequest(html.signup(bindForm.withGlobalError(i18nMsg)))
            }
          }
        }
      }
    )
  }

}
