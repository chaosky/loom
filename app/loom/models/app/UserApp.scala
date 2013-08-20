package loom.models.app

import scala.language.implicitConversions
import play.api.db.DB
import play.api.Play.current
import anorm._
import anorm.SqlParser._

import loom.Global
import loom.utils.Memcached
import loom.models.Model

/**
 *
 * @author chaosky
 */
case class UserApp(userId: Long, appIds: List[Long])

object UserApp {

  private val parser = {
    long("userId") ~ str("appIds") map {
      case userId ~ appIdsStr =>
        val appIds = if (appIdsStr.isEmpty) Nil else appIdsStr.split(Model.semicolon).map(_.toLong).toList
        UserApp(userId, appIds)
    }
  }

  private def _update(userapp: UserApp) = {
    DB.withConnection { implicit conn =>
      SQL("update user_app set appIds = {appIds} where userId = {userId}").on(
        'userId -> userapp.userId,
        'appIds -> userapp.appIds.mkString(Model.semicolon)
      ).executeUpdate()
    }
  }

  def update(userapp: UserApp): Int = {
    cleanCache(userapp.userId)
    _update(userapp)
  }

  private def _findOne(userId: Long): Option[UserApp] = {
    DB.withConnection { implicit conn =>
      SQL("select * from user_app where userId = {userId}").on(
        'userId -> userId
      ).as(parser.singleOpt)
    }
  }

  private def userAppCacheKey(userId: Long) = {
    Global.cacheNameSpace + "m/userapp/0/" + userId
  }

  private def cleanCache(userId: Long) {
    Memcached.delete(userAppCacheKey(userId))
  }

  def findOne(userId: Long): Option[UserApp] = {
    val ret = Memcached.getOrElse(userAppCacheKey(userId)) {
      _findOne(userId).getOrElse(null)
    }
    Option(ret)
  }

  def remove(userId: Long, appId: Long) = {
    findOne(userId) match {
      case Some(ua) =>
        val newua = ua.copy(appIds = ua.appIds.filter(_ != appId))
        update(newua)
        (true, "common.ok")
      case None => (false, "common.not.found")
    }
  }

  def add(userId: Long, appId: Long) = {
    findOne(userId) match {
      case Some(ua) =>
        val newua = ua.copy(appIds = appId :: ua.appIds)
        update(newua)
        (true, "common.ok")
      case None => (false, "common.not.found")
    }
  }
}
