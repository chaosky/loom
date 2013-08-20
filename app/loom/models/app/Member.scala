package loom.models.app


import scala.language.implicitConversions

import loom.Global

import play.api.db.DB
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import loom.utils.Memcached
import loom.models.Model

/**
 *
 * @author chaosky
 */
case class Member(appId: Long, members: List[Long]) {

  lazy val memberSet = members.toSet

  def isMember(userId: Long) = {
    memberSet.contains(userId)
  }

}

object Member {

  private val parser = {
    long("appId") ~ str("members") map {
      case appId ~ memberStr =>
        val member = if (memberStr.isEmpty) Nil else memberStr.split(Model.semicolon).map(_.toLong).toList
        Member(appId, member)
    }
  }

  def create(appId: Long, members: List[Long]) = {
    DB.withConnection { implicit conn =>
      SQL("insert into app_member values({appId}, {members})").on(
        'appId -> appId,
        'members -> members.mkString(Model.semicolon)
      ).executeInsert()
    }
  }

  private def _update(member: Member) = {
    DB.withConnection { implicit conn =>
      SQL("update app_member set members = {members} where appId = {appId}").on(
        'members -> member.members.mkString(Model.semicolon),
        'appId -> member.appId
      ).executeUpdate()
    }
  }

  def update(member: Member): Int = {
    cleanCache(member.appId)
    _update(member)
  }

  private def _findOne(appId: Long): Option[Member] = {
    DB.withConnection { implicit conn =>
      SQL("select * from app_member where appid = {appId}").on(
        'appId -> appId
      ).as(parser.singleOpt)
    }
  }

  private def memberCacheKey(appId: Long) = {
    Global.cacheNameSpace + "m/appmember/0/" + appId
  }

  private def cleanCache(appId: Long) {
    Memcached.delete(memberCacheKey(appId))
  }

  def findOne(appId: Long): Option[Member] = {
    val ret = Memcached.getOrElse(memberCacheKey(appId)) {
      _findOne(appId).orNull
    }
    Option(ret)
  }

  def isMember(appId: Long, userId: Long): Boolean = {
    findOne(appId) match {
      case Some(app) => app.isMember(userId)
      case None => false
    }
  }

  def remove(appId: Long, userId: Long): (Boolean, String) = {
    findOne(appId) match {
      case Some(member) =>
        if (member.members.find(_ == userId).isEmpty)
          (true, "common.nothing.happened")
        else {
          update(member.copy(members = member.members.filter(_ != userId)))
          UserApp.remove(userId, appId)
          (true, "common.ok")
        }
      case None => (false, "common.not.found")
    }
  }

  def add(appId: Long, userId: Long): (Boolean, String) = {
    findOne(appId) match {
      case Some(member) =>
        if (member.members.exists(_ == userId))
          (true, "common.nothing.happened")
        else {
          update(member.copy(members = userId :: member.members))
          UserApp.add(userId, appId)
          (true, "common.ok")
        }
      case None => {
        App.findOne(appId) match {
          case Some(app) =>
            create(appId, List(userId))
            (true, "common.ok")
          case None => (false, "common.not.found")
        }
      }
    }
  }
}