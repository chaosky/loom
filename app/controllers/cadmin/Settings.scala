package controllers.cadmin

import views._

import loom.models.admin.Admin
import controllers._

/**
 *
 * @author chaosky
 */
object Settings extends AbstractSettings {

  def password() = AdminAction {
    implicit request =>
      Ok(html.admin.settings.password(passwordForm))
  }

  def changePassword() = AdminAction { implicit request =>
    passwordForm.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(html.admin.settings.password(formWithErrors))
      },
      passwords => {
        val (ret, i18nMsg) = Admin.changePassword(request.aSession.admin.id
          , passwords._1, passwords._2)
        ret match {
          case false =>
            val nForm = passwordForm.withGlobalError(i18nMsg)
            BadRequest(html.admin.settings.password(nForm))
          case true =>
            Redirect(routes.Settings.password()).flashing(
              "success" -> i18nMsg
            )
        }
      }
    )
  }

}
