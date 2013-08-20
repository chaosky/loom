package controllers.cadmin

import play.api.mvc._
import views._

import controllers._
import loom.models.{Status => UStatus, _}
import loom.models.admin._


/**
 *
 * @author chaosky
 */
object Users extends Controller with Secured with BaseController {

  def list() = AdminAction(APsn.Admin, APsn.User_Read) { implicit request =>
    val (pageNo, pageSize) = page(request)
    val list = User.list(PageRequest(pageNo, pageSize))

    Ok(html.admin.users.list(list))
  }

  def password() = AdminAction(APsn.Admin, APsn.User_Update) { implicit request =>
    try {
      val id = request.getQueryString("id").map(_.toLong).get
      val (ret, i18nMsg) = User.addStatus(id, UStatus.NeedResetPassword)
      if (ret) Json(Model.Success, i18nMsg) else Json(Model.Error, i18nMsg)
    } catch {
      case ex: NumberFormatException =>
        Json(Model.Error, "m.user.error.id")
    }
  }

  def status() = AdminAction(APsn.Admin, APsn.User_Update) { implicit request =>
    try {
      val id = request.getQueryString("id").map(_.toLong).get
      val (ret, event, i18nMsg) = User.toggleStatus(id)
      if (ret) Json(Model.Success, i18nMsg, "event" -> event)
      else Json(Model.Error, i18nMsg)
    } catch {
      case ex: NumberFormatException =>
        Json(Model.Error, "m.user.error.id")
    }
  }
}
