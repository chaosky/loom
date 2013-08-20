package loom.models

import scala.beans.BeanProperty
import scala.collection.mutable
import scala.language.implicitConversions

/**
 * @author chaosky
 */
abstract class PermissionGroup extends Enumeration {pgroup =>
  type Type <: PGVal
  type PsType <: Permission#PVal

  private val groupMap: mutable.Map[Type, Set[PsType]] = new mutable.HashMap

  private[models] def addPerrmission(group: Type, permission: PsType) {
    val pset = groupMap.get(group).getOrElse(Set.empty)
    groupMap(group) = (pset + permission)
  }

  def permissions(group: Type): Set[PsType] = {
    groupMap(group)
  }

  implicit def valueToPermissionsGroup(v: Value): Type = v.asInstanceOf[Type]

  case class PGVal(i18nMsg: String, visible: Boolean = true) extends Val {

    def permissions(): Set[PsType] = {
      pgroup.permissions(this)
    }

  }

}

/**
 *
 * @author chaosky
 */
abstract class Permission extends Enumeration {
  type Type <: PVal
  type PsgType <: PermissionGroup#PGVal

  case class PVal(override val id: Int, @BeanProperty enumName: String, @BeanProperty i18nMsg: String, group: PsgType, visible: Boolean = true) extends Val(id, enumName)

  def get(x: Int): Type = super.apply(x)

  implicit def valueToPVal(v: Value): Type = v.asInstanceOf[Type]
}


