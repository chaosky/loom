package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._
import views._

import loom.utils.ConstraintsUtil
import loom.models._

trait AbstractSettings extends Controller with Secured {

  val passwordText = ConstraintsUtil.passwordText(Account.pwdMinLen, Account.pwdMaxLen)

  val passwordForm = Form(
    mapping(
      "password" -> tuple(
        "current" -> passwordText,
        "new" -> passwordText,
        "confirm" -> text
      ).verifying(Constraint {
        pwd: (String, String, String) =>
          Logger.debug("pwd " + pwd)
          if (pwd._1 == pwd._2) {
            Invalid(Seq(ValidationError("m.password.verifying.current.same.new")))
          } else if (pwd._2 != pwd._3) {
            Invalid(Seq(ValidationError("m.password.verifying.dont.match")))
          } else {
            Valid
          }
      })
    )((passwords) => passwords)((t: (String, String, String)) => Some(t))
  )

}

/**
 *
 * @author chaosky
 */
object Settings extends AbstractSettings {

  def password() = UserAction {
    implicit request =>
      Ok(html.settings.password(passwordForm))
  }

  def changePassword() = UserAction { implicit request =>
    passwordForm.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(html.settings.password(formWithErrors))
      },
      passwords => {
        val (ret, i18nMsg) = User.changePassword(request.userId, passwords._1, passwords._2)
        ret match {
          case false =>
            val nForm = passwordForm.withGlobalError(i18nMsg)
            BadRequest(html.settings.password(nForm))
          case true =>
            Redirect(routes.Settings.password()).flashing(
              "success" -> i18nMsg
            )
        }
      }
    )
  }
}
