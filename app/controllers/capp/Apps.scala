package controllers.capp

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import views._

import controllers._
import loom.models._
import loom.models.app._

/**
 *
 * @author chaosky
 */
object Apps extends Controller with BaseController with Secured {

  def appsAndUser(ids: List[Long]): List[(Option[App], User)] = {
    ids.map { appId =>
      val app = App.findOne(appId)
      if (app.isDefined)
        (app, User.findOneSrc(app.get.owner))
      else
        (None, null)
    }
  }

  def index(implicit id: Long) = AppAction { implicit request =>
    Ok(html.apps.index())
  }

  private val appForm = Form(single(
    "name" -> nonEmptyText(maxLength = App.Name_MaxLen)
  ))

  def editForm(implicit appId: Long) = AppAction(UPsn.Admin, UPsn.App_Update) { implicit request =>
    Ok(html.apps.apps.edit(appForm))
  }

  def edit(implicit appId: Long) = AppAction(UPsn.Admin, UPsn.App_Update) { implicit request =>
    appForm.bindFromRequest().fold(
      formWithErrors => BadRequest(html.apps.apps.edit(formWithErrors)),
      name => {
        val newapp = request.appSession.app.copy(name = name)
        App.update(newapp)
        Redirect(controllers.routes.Index.index()).flashing(
          "success" -> "common.success"
        )
      }
    )
  }

  def createForm() = UserAction { implicit request =>
    Ok(html.apps.apps.create(appForm))
  }

  def create() = UserAction { implicit request =>
    appForm.bindFromRequest().fold(
      formWithErrors => BadRequest(html.apps.apps.create(formWithErrors)),
      name => {
        App.create(request.user, name)
        Redirect(controllers.routes.Index.index()).flashing(
          "success" -> "common.create.success"
        )
      }
    )
  }

}
