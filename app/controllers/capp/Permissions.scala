package controllers.capp

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import controllers._
import loom.models._
import loom.models.app._
import views._

/**
 *
 * @author chaosky
 */
object Permissions extends Controller with BaseController with Secured {

  def list(implicit appId: Long) = AppAction(UPsn.Admin, UPsn.Permission_Read) { implicit request =>
    val uplist = Member.findOne(appId) match {
      case Some(m) => m.members.map(uid => (User.findOneSrc(uid), UserRole.findOne(uid, appId)))
      case None => Nil
    }

    val owner = if (request.appSession.app.owner == request.userId)
      request.appSession.user
    else
      User.findOneSrc(request.userId)

    val hasUpdatePsn = request.appSession.hasPermissions(UPsn.Admin, UPsn.Permission_Update)
    Ok(html.apps.permissions.list(owner, uplist, hasUpdatePsn))
  }

  private val psForm = Form(single(
    "permissions" -> Forms.list(text)
  ))


  private val updatePsn = List(UPsn.Admin, UPsn.Permission_Update)

  def editForm(appId: Long, userId: Long) = AppAction(updatePsn)(appId) { implicit request =>
    if (request.appSession.app.owner == userId) _404
    else {
      User.findOne(userId) match {
        case Some(u) => Ok(html.apps.permissions.edit(psForm, u, UserRole.findOne(userId, appId)))
        case None => _404
      }

    }
  }

  def edit(appId: Long, userId: Long) = AppAction(updatePsn)(appId) { implicit request =>
    if (request.appSession.app.owner == userId) _404
    else {
      User.findOne(userId) match {
        case Some(user) =>
          psForm.bindFromRequest().fold(
            fwe => BadRequest(html.apps.permissions.edit(fwe, user, UserRole.findOne(userId, appId))),
            permissionsStrList => {
              val permissions = permissionsStrList.map { name =>
                try {
                  Option(UPermission.withName(name).asInstanceOf[UPermission.Type])
                } catch {
                  case e: Exception =>
                    Logger.error("parser permission name %s".format(name), e)
                    None
                }
              }.flatten

              UserRole.findOne(userId, appId) match {
                case Some(ur) =>
                  val newur = ur.copy(permissions = permissions)
                  UserRole.update(newur)
                case None => UserRole.create(userId, appId, permissions)
              }

              Redirect(capp.routes.Permissions.list(appId)).flashing(
                "success" -> "common.success"
              )
            }
          )
        case None => _404
      }
    }
  }
}
