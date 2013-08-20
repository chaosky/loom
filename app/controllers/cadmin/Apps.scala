package controllers.cadmin

import loom.models.admin._
import loom.models.app.App
import loom.models.{Model, PageRequest}
import views.html
import play.api.mvc.Controller
import controllers._
import play.api.data.Form
import play.api.data.Forms._

/**
 *
 * @author chaosky
 */
object Apps extends Controller with BaseController with Secured {

  def list() = AdminAction(APsn.Admin, APsn.App_Read) { implicit request =>
    val (pageNo, pageSize) = page(request)
    val list = App.listIds(PageRequest(pageNo, pageSize))
    val appAndUser = capp.Apps.appsAndUser(list.result.toList)

    Ok(html.admin.apps.list(list, appAndUser))
  }


  val statusForm = Form(single("id" -> longNumber))

  def status() = AdminAction(APsn.Admin, APsn.App_Update) { implicit request =>
    statusForm.bindFromRequest().fold(
      formWithErrors => Json(Model.Error, "m.app.app.error.id"),
      id => {
        val (ret, event, i18nMsg) = App.toggleStatus(id)
        if (ret) Json(Model.Success, i18nMsg, "event" -> event)
        else Json(Model.Error, i18nMsg)
      }
    )
  }

}
