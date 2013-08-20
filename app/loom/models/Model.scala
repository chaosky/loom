package loom.models

/**
 *
 * @author chaosky
 */
object Model {
  val semicolon = ";"

  val Ret = "ret"
  val Msg = "msg"
  val Error = -1
  val Success = 0

  val PageNo = 1
  val PageSize = 5

  //models
  val M_Accounts = "accounts"
  val M_Roles = "roles"
  val M_Permissions = "permissions"
  val M_Users = "users"
  val M_App = "app"

  //session key
  val user_loginname = "username"
  val admin_loginname = "email"
}

object UModel {
  val M_Member = "member"
  val M_Permission = "permission"

}