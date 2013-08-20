package controllers

import loom.models.admin.{APermission, Role, Admin}
import loom.models.User
import loom.models.app.{UserRole, UPermission, App}


/**
 * admin session, account session info
 * @author chaosky
 */
case class ASession(admin: Admin) {

  private lazy val permissions = Role.permissions(admin.id)

  private lazy val permissionSet = Set(permissions: _*)

  def hasPermission(permission: APermission.Type): Boolean = {
    permissionSet.contains(permission)
  }

  def hasPermissions(permissions: APermission.Type*): Boolean = {
    permissions.exists(ps => permissionSet.contains(ps))
  }
}

case class AppSession(user: User, app: App) {
  private lazy val permissionSet: Set[UPermission.Type] = UserRole.findOne(user.id, app.id) match {
    case Some(ur) => ur.permissions.toSet
    case None => Set.empty
  }

  lazy val isOwner = app.owner == user.id

  def hasPermission(permission: UPermission.Type): Boolean = {
    if (isOwner) true
    else
      permissionSet.contains(permission)
  }

  def hasPermissions(permissions: UPermission.Type*): Boolean = {
    if (isOwner) true
    else
      permissions.exists(ps => permissionSet.contains(ps))
  }

}
