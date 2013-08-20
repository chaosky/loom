package loom.models.app

import scala.language.implicitConversions

import loom.Global

import play.api.db.DB
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import loom.utils.{Codecs, Memcached}
import java.util.Date

import loom.models._

/**
 *
 * @author chaosky
 */
@SerialVersionUID(0 - 8181856739675748583L)
case class App(id: Long,
  name: String,
  appkey: String,
  owner: Long,
  status: Int,
  createDate: Date) {

  def disabled() = status == App.Status_Disable

  def enable() = status == App.Status_Normal
}

object App {
  val Status_Normal = 1
  val Status_Disable = 2

  val Name_MaxLen = 8
  val AppKeyLen = 32

  @volatile private var listTimes = System.currentTimeMillis()

  private val parser = {
    long("id") ~
      str("name") ~
      str("appkey") ~
      long("owner") ~
      int("status") ~
      date("create_date") map {
      case id ~ name ~ appkey ~ owner ~ status ~ createDate =>
        App(id, name, appkey, owner, status, createDate)
    }
  }

  def create(user: User, name: String) = {
    val appkey = Codecs.generateSaltStr(AppKeyLen)
    val status = Status_Disable
    val createDate = new java.util.Date
    val id: Option[Long] = DB.withConnection { implicit conn =>
      SQL( """insert into app (name, appkey, owner, status, create_date)
             | values({name}, {appkey}, {owner},
             | {status}, {create_date})""".stripMargin).on(
        'name -> name,
        'appkey -> appkey,
        'owner -> user.id,
        'status -> status,
        'create_date -> createDate
      ).executeInsert()
    }

    id match {
      case Some(i) =>
        listTimes = System.currentTimeMillis()
        UserApp.add(user.id, i)
        Some(App(i, name, appkey, user.id, status, createDate))
      case None => None
    }
  }

  private def _update(app: App) = {
    DB.withConnection { implicit conn =>
      SQL("update app set name = {name}, status = {status} where id = {id}").on(
        'name -> app.name,
        'status -> app.status,
        'id -> app.id
      ).executeUpdate()
    }
  }

  def update(app: App) = {
    cleanCache(app.id)
    _update(app)
  }

  private def _findOne(appId: Long): Option[App] = {
    DB.withConnection { implicit conn =>
      SQL("select * from app where id = {id}").on(
        'id -> appId
      ).as(parser.singleOpt)
    }
  }

  private def count(): Long = {
    Memcached.getOrElse(countCacheKey()) {
      val count = DB.withConnection { implicit conn =>
        SQL("select count(*) from app").as(scalar[Long].single)
      }
      count
    }
  }

  private def _list(page: PageRequest): PageImpl[Long] = {
    val limit = if (Global.isDbH2) {
      "order by id desc limit {rowcount} offset {offset}"
    } else if (Global.isDbMysql) {
      "order by id desc limit {offset}, {rowcount}"
    } else {"" }

    val sql = "select id from app " + limit

    val list: List[Long] = DB.withConnection { implicit conn =>
      SQL(sql).on('offset -> page.offset, 'rowcount -> page.pageSize).as(long("id").*)
    }
    val c: Long = count()
    PageImpl(page.pageNo, page.pageSize, list, c)
  }


  private def cleanCache(appId: Long) {
    Memcached.delete(appCacheKey(appId))
  }

  private def appCacheKey(appId: Long) = {
    Global.cacheNameSpace + "m/app/0/" + appId
  }

  private def countCacheKey() = {
    Global.cacheNameSpace + "m/app/count/0/ts/" + listTimes
  }

  private def listCacheKey(pageNo: Int, pageSize: Int) = {
    Global.cacheNameSpace + "m/app/list/0/pn/" + pageNo + "/ps/" + pageSize + "/ts/" + listTimes
  }

  def findOne(appId: Long): Option[App] = {
    val ret = Memcached.getOrElse(appCacheKey(appId)) {
      _findOne(appId).getOrElse(null)
    }
    Option(ret)
  }

  def finds(appIds: List[Long]): List[Option[App]] = {
    Memcached.getsOrElse(appIds.map(id => (appCacheKey(id), id)).toMap) { id =>
      _findOne(id)
    }
  }

  def listIds(page: PageRequest): PageImpl[Long] = {
    Memcached.getOrElse(listCacheKey(page.pageNo, page.pageSize)) {
      _list(page)
    }
  }

  def list(page: PageRequest): PageImpl[App] = {
    val idsPage = Memcached.getOrElse(listCacheKey(page.pageNo, page.pageSize)) {
      _list(page)
    }
    val applist = Memcached.getsOrElseSeq(idsPage.result.map(id => (appCacheKey(id), id))) { id =>
      _findOne(id)
    }.flatten
    PageImpl(page, idsPage.total, applist)
  }

  def toggleStatus(appId: Long): (Boolean, String, String) = {
    findOne(appId) match {
      case Some(app) =>
        val newapp = if (app.disabled())
          ("enable", app.copy(status = App.Status_Normal))
        else
          ("disable", app.copy(status = App.Status_Disable))
        update(newapp._2)
        (true, newapp._1, "common.ok")
      case None => (false, "none", "common.not.found")
    }
  }
}
