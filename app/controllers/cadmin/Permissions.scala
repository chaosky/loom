package controllers.cadmin

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import loom.models._
import views._
import loom.models.admin.{Role, AdminRole, APermission, Admin}
import controllers.{Secured, BaseController}

object Permissions extends Controller with Secured with BaseController {

  def list() = AdminAction(APermission.Admin, APermission.Permission_Read) { implicit request =>
    val (pageNo, pageSize) = page(request)
    val list = Admin.listWithRole(PageRequest(pageNo, pageSize))
    Ok(html.admin.permissions.list(list))
  }

  def roles() = AdminAction(APermission.Admin, APermission.Role_Read) { implicit request =>
    val (pageNo, pageSize) = page(request)
    val list = Role.list(PageRequest(pageNo, pageSize))
    Ok(html.admin.permissions.roles(list))
  }

  val psForm = Form(single(
    "roles" -> Forms.list(longNumber)
  ))

  def editForm(id: Long) = AdminAction(APermission.Admin, APermission.Permission_Update) { implicit request =>
    if (Role.Admin_Role_Id == id) Forbidden(html.defaultpages.unauthorized())
    else {
      Admin.findOne(id) match {
        case Some(a) => Ok(html.admin.permissions.edit(psForm, a, Role.roles(id), AdminRole.findOne(id)))
        case None => _404 // TODO implements 404 501 pages
      }
    }
  }

  def edit(id: Long) = AdminAction(APermission.Admin, APermission.Permission_Update) { implicit request =>
    if (Role.Admin_Role_Id == id) Forbidden(html.defaultpages.unauthorized())
    else {
      val account = Admin.findOne(id)
      if (account.isEmpty)
        _404
      else {
        psForm.bindFromRequest().fold(
          formWithError => BadRequest(html.admin.permissions.edit(formWithError, account.get, Role.roles(id), AdminRole.findOne(id))),
          roleIds => {
            val rs = roleIds.map { roleId =>
              Role.findOne(roleId) match {
                case Some(role) => if (role.visible) Option(role.id) else None
                case None => None
              }
            }.flatten

            Logger.debug("roleIds " + rs)
            AdminRole.findOne(id) match {
              case Some(ar) => AdminRole.update(ar.userId, rs)
              case None => AdminRole.create(id, rs)
            }
            Redirect(routes.Permissions.list()).flashing(
              "success" -> "common.success"
            )
          }
        )
      }
    }
  }
}