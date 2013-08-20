package loom.models

import  scala.language.implicitConversions
/**
 *
 * @author chaosky
 */

/**
 * account status
 */
abstract class Status extends Enumeration {
  type Status = StatusVal

  case class StatusVal(override val id: Int, dbName: String, i18nMsg: String) extends Val(id, dbName)

}

object Status extends Status {
  val None = StatusVal(0, "none", "")
  val EmailNotVerified = StatusVal(1, "not verified", "m.status.notverified")
  val NeedResetPassword = StatusVal(2, "reset password", "m.status.resetpassword")
  //val Normal = StatusVal(4, "normal", "models.status.normal")
  val Disable = StatusVal(4, "disable", "m.status.disable")

  implicit def valToStatusVal(x: Value): StatusVal = x.asInstanceOf[StatusVal]
}