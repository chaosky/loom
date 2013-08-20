package loom.models.app

import loom.models.{PermissionGroup, Permission}

/**
 *
 * @author chaosky
 */
object UPermissionGroup extends PermissionGroup {
  type Type = PGVal
  type PsType = UPermission.Type

  val Admin = PGVal("m.upsng.admin", false)

  val Project = PGVal("m.upsng.project")
  val Member = PGVal("m.upsng.member")
  val Permission = PGVal("m.upsng.permission")
}

object UPermission extends Permission {
  type Type = PVal
  type PsgType = UPermissionGroup.Type

  val Admin = PVal(99, "admin", "m.upsn.admin", UPermissionGroup.Admin)

  val Member_Read = PVal(101, "memberRead", "m.upsn.member.read", UPermissionGroup.Member)
  val Member_Add_Del = PVal(102, "memberAddDel", "m.upsn.member.adddel", UPermissionGroup.Member)

  val App_Update = PVal(201, "appUpdate", "m.upsn.app.update", UPermissionGroup.Project)

  val Permission_Update = PVal(301, "permissionUpdate", "m.upsn.permission.update", UPermissionGroup.Permission)
  val Permission_Read = PVal(302, "permissionRead", "m.upsn.permission.read", UPermissionGroup.Permission)

  values.foreach(ps => UPermissionGroup.addPerrmission(ps.group, ps))
}
