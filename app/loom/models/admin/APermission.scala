package loom.models.admin

import scala.language.implicitConversions
import loom.models.{PermissionGroup, Permission}

/**
 * @author chaosky
 */
object APermissionGroup extends PermissionGroup {
  type Type = PGVal
  type PsType = APermission.Type

  val Admin = PGVal("m.apsng.admin", false)
  val Account = PGVal("m.apsng.account")
  val Role = PGVal("m.apsng.role")
  val Permissions = PGVal("m.apsng.permissions")
  val User = PGVal("m.apsng.user")
  val App = PGVal("m.apsng.app")

}

/**
 * APermission list
 *
 */
object APermission extends Permission {
  type Type = PVal
  type PsgType = APermissionGroup.Type

  // id,name, i18n key
  //admin manage user
  val Admin = PVal(99, "admin", "m.apsn.admin", APermissionGroup.Admin, false)
  // account
  val Account_Create = PVal(100, "accountCreate", "m.apsn.account.create", APermissionGroup.Account)
  val Account_Update = PVal(101, "accountUpdate", "m.apsn.account.update", APermissionGroup.Account)
  val Account_Read = PVal(102, "accountRead", "m.apsn.account.read", APermissionGroup.Account)
  //val User_Disable = PVal(103, "userList", "m.apsn.account.disable", APermissionGroup.Admin)
  val Account_ResetPassword = PVal(104, "accountResetPassword", "m.apsn.account.resetpassword", APermissionGroup.Account)
  val Account_ToggleStatus = PVal(105, "accountToggleStatus", "m.apsn.account.togglestatus", APermissionGroup.Account)

  //roles
  val Role_Create = PVal(200, "roleCreate", "m.apsn.role.create", APermissionGroup.Role)
  val Role_Update = PVal(201, "roleUpdate", "m.apsn.role.update", APermissionGroup.Role)
  val Role_Read = PVal(202, "roleRead", "m.apsn.role.read", APermissionGroup.Role)
  val Role_Disable = PVal(203, "roleDisable", "m.apsn.role.disable", APermissionGroup.Role)

  val Permission_Update = PVal(301, "permissionUpdate", "m.apsn.permission.update", APermissionGroup.Permissions)
  val Permission_Read = PVal(302, "permissionRead", "m.apsn.permission.read", APermissionGroup.Permissions)

  val User_Read = PVal(401, "userRead", "m.apsn.user.read", APermissionGroup.User)
  val User_Update = PVal(402, "userUpdate", "m.apsn.user.update", APermissionGroup.User)

  val App_Read = PVal(501, "appRead", "m.apsn.app.read", APermissionGroup.App)
  val App_Update = PVal(502, "appUpdate", "m.apsn.app.update", APermissionGroup.App)

  values.foreach(ps => APermissionGroup.addPerrmission(ps.group, ps))
}

