package loom.models.app

import scala.language.implicitConversions

import loom.utils._
import loom.Global

import play.api.db.DB
import play.api.Play.current
import play.api.Logger
import anorm.SQL
import anorm.~
import anorm.SqlParser._
import loom.models.Model

/**
 *
 * @author chaosky
 */
case class UserRole(userId: Long, appId: Long, permissions: List[UPermission.Type]) {

  lazy val permissionSet = permissions.toSet

  def contains(permission: UPermission.Type) = {
    permissionSet.contains(permission)
  }

  def id(): String = UserRole.id(userId, appId)
}

object UserRole {
  // id = userId_appId
  private val Spliter = "_"

  private val parser = {
    str("userId") ~ str("permissions") map {
      case id ~ pIds =>
        val sarr = id.split(Spliter)
        val userId = sarr(0).toLong
        val appId = sarr(1).toLong
        val pslist = pIds.split(Model.semicolon).map { pid =>
          try {
            UPermission.get(pid.toInt)
          } catch {
            case e: Exception => Logger.error("userId %s PVal %s".format(userId, e.getMessage))
              null
          }
        }.filter(_ != null).toList
        UserRole(userId, appId, pslist)
    }
  }

  private def _update(ur: UserRole) = {
    DB.withConnection { implicit conn =>
      SQL("update user_role set permissions = {permissions} where userId = {userId}").on(
        'userId -> ur.id,
        'permissions -> ur.permissions.map(_.id).mkString(Model.semicolon)
      ).executeUpdate()
    }
  }

  def update(ur: UserRole) = {
    cleanCache(ur.userId, ur.appId)
    _update(ur)
  }

  def create(userId: Long, appId: Long, permissions: List[UPermission.Type]) = {
    DB.withConnection { implicit conn =>
      SQL("insert into user_role values({userId}, {permissions})").on(
        'userId -> id(userId, appId),
        'permissions -> permissions.map(_.id).mkString(Model.semicolon)
      ).executeInsert()
    }
  }

  private def _findOne(userId: Long, appId: Long): Option[UserRole] = {
    DB.withConnection { implicit conn =>
      SQL("select * from user_role where userId = {userId}").on(
        'userId -> id(userId, appId)
      ).as(parser.singleOpt)
    }
  }

  private def id(userId: Long, appId: Long): String = "" + userId + Spliter + appId

  private def cleanCache(userId: Long, appId: Long) {
    Memcached.delete(userRoleCacheKey(userId, appId))
  }

  private def userRoleCacheKey(userId: Long, appId: Long) = {
    Global.cacheNameSpace + "m/userrole/1/" + id(userId, appId)
  }

  def findOne(userId: Long, appId: Long): Option[UserRole] = {
    val ret = Memcached.getOrElse(userRoleCacheKey(userId, appId)) {
      _findOne(userId, appId).getOrElse(null)
    }
    Option(ret)
  }


}