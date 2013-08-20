package loom.models.admin

import scala.language.implicitConversions
import scala.collection.JavaConversions._
import scala.beans.BeanProperty

import play.api.db.DB
import play.api.Play.current
import play.api.Logger
import anorm.SQL
import anorm.~
import anorm.SqlParser._

import java.util.Date

import loom.utils.Memcached
import loom.Global
import loom.models._

/**
 * Admin (1) -> (N) Role (1) -> (N) APermission
 */

/**
 * user & role
 */
case class AdminRole(userId: Long, roleIds: List[Long]) {
  private lazy val rolesSet = roleIds.toSet

  def contains(roleId: Long) = rolesSet.contains(roleId)
}

object AdminRole {

  def create(userId: Long, roleIds: List[Long]) = {
    DB.withConnection { implicit conn =>
      SQL(
        """insert into opt_admin_role VALUES ({userId}, {roleIds})""").on(
        'userId -> userId,
        'roleIds -> roleIds.mkString(Model.semicolon)
      ).executeInsert()
    }
  }

  def _update(userId: Long, roleIds: List[Long]) = {
    DB.withConnection { implicit conn =>
      SQL( """update opt_admin_role set roleIds = {roleIds} where userId = {userId}""").on(
        'userId -> userId,
        'roleIds -> roleIds.mkString(Model.semicolon)
      ).executeUpdate()
    }
  }

  def update(userId: Long, roleIds: List[Long]) = {
    cleanCache(userId)
    _update(userId, roleIds)
  }

  private def _findOne(userId: Long): Option[AdminRole] = {
    DB.withConnection { implicit conn =>
      val ret = SQL(
        """select * from opt_admin_role where userId = {userId}"""
      ).on(
        'userId -> userId
      ).as(
        {
          long("userId") ~ str("roleIds") map {
            case uId ~ rIds => {
              val roleIds = if (rIds.length == 0) Nil else rIds.split(Model.semicolon).map(_.toLong).toList
              AdminRole(uId, roleIds)
            }
          }
        }.singleOpt
      )

      ret
    }
  }

  private def cleanCache(userId: Long) {
    Memcached.delete(userRoleCacheKey(userId))
  }

  private def userRoleCacheKey(userId: Long) = {
    Global.cacheNameSpace + "m/AdminRole/0/" + userId
  }

  def findOne(userId: Long): Option[AdminRole] = {
    val ret: AdminRole = Memcached.getOrElse(userRoleCacheKey(userId)) {
      _findOne(userId).getOrElse(null)
    }

    Option(ret)
  }

}

/**
 * opt role info.
 */
case class Role(@BeanProperty id: Long, @BeanProperty name: String, permissions: List[APermission.Type], status: Int, @BeanProperty visible: Boolean, createDate: Date) {

  def enable() = { status == Role.Status_Enable }

  def disable() = { status == Role.Status_Disable }

  private lazy val permissionsSet = Set() ++ permissions

  def hasPermissions(psVal: APermission.Type) = {
    permissionsSet.contains(psVal)
  }

  def getPermissions(): java.util.List[APermission.Type] = {
    permissions
  }
}

object Role {
  val Admin_Role_Id = 1

  val Status_Disable = 1
  val Status_Enable = 2

  val nameMinLen = 2
  val nameMaxLen = 8
  /** list cache timestamp. update when create role */
  @volatile private var listTimes = System.currentTimeMillis()

  // -- Parsers
  /**
   * Parse a Role from a ResultSet
   */
  private val simple = {
    get[Long]("id") ~
      get[String]("name") ~
      get[String]("permissions") ~
      get[Int]("status") ~
      get[Boolean]("visiable") ~
      get[Date]("createDate") map { case id ~ name ~ permissions ~ status ~ visiable ~ createDate =>
      val pslist = permissions.split(Model.semicolon).map { pid =>
        try {
          APermission.get(pid.toInt)
        } catch {
          case e: Exception => Logger.error("roleId %s PVal %s".format(id, e.getMessage))
            null
        }
      }.filter(_ != null).toList

      Role(id, name, pslist, status, visiable, createDate)
    }
  }

  /**
   *
   * @param name
   * @param psList
   * @return (success: Boolean, role: Role, i18nMsg: String)
   */
  def create(name: String, psList: List[APermission.PVal]): (Boolean, Role, String) = {
    val permissions = psList.map(_.id).mkString(Model.semicolon)
    val status = Role.Status_Enable
    val visiable = true
    val createDate = new java.util.Date
    val id: Option[Long] = DB.withConnection { implicit conn =>
      SQL(
        """insert into opt_role(name, permissions, status, visiable,
          | createDate)
          | values ({name}, {permissions}, {status}, {visiable},
          | {createDate})""".stripMargin).on(
        'name -> name,
        'permissions -> permissions,
        'status -> status,
        'visiable -> visiable,
        'createDate -> createDate
      ).executeInsert()
    }

    id match {
      case Some(i) =>
        listTimes = System.currentTimeMillis()
        val r = Role(i, name, psList, status, visiable, createDate)
        (true, r, "common.create.success")
      case None => (false, null, "common.error.unknown")
    }
  }

  private def _findOne(roleId: Long): Option[Role] = {
    DB.withConnection {
      implicit c =>
        val ret = SQL( """select * from opt_role where id = {id}"""
        ).on(
          'id -> roleId
        ).as(
          simple.singleOpt
        )

        ret
    }
  }


  def findOne(roleId: Long): Option[Role] = {
    val role = Memcached.getOrElse(roleCacheKey(roleId)) {
      _findOne(roleId).getOrElse(null)
    }
    Option(role)
  }

  def roles(accountId: Long): List[Option[Role]] = {
    val ur = AdminRole.findOne(accountId)

    ur match {
      case Some(userRole) => userRole.roleIds.map(rId => Role.findOne(rId))
      case None => Nil
    }
  }

  private def _updateRole(role: Role) = {
    val pms = role.permissions.map(_.id).mkString(Model.semicolon)

    DB.withConnection { implicit conn =>
      SQL( """update opt_role set name = {name},
             | permissions = {permissions}, status = {status}
             | where id = {id}
           """.stripMargin).on(
        'name -> role.name,
        'permissions -> pms,
        'status -> role.status,
        'id -> role.id
      ).executeUpdate()
    }
  }

  private def updateRole(role: Role) = {
    cleanCache(role.id)
    _updateRole(role)
  }

  def updateRole(role: Role, name: String, permissions: List[APermission.PVal]) {
    val newr = role.copy(name = name, permissions = permissions)
    updateRole(newr)
  }

  private def roleCacheKey(roleId: Long) = {
    Global.cacheNameSpace + "m/Role/0/" + roleId
  }

  private def permissionsCacheKey(userId: Long) = {
    Global.cacheNameSpace + "m/APermission/0/" + userId
  }

  private def countCacheKey() = {
    Global.cacheNameSpace + "m/Role/listcount/0/ts/" + listTimes
  }

  private def listCacheKey(pageNo: Int, pageSize: Int) = {
    Global.cacheNameSpace + "m/Role/list/0/pn/" + pageNo + "/ps/" + pageSize + "/ts/" + listTimes
  }

  private def cleanCache(roleId: Long) {
    Memcached.delete(roleCacheKey(roleId))

  }


  def permissions(userId: Long): List[APermission.Type] = {
    Memcached.getOrElse(permissionsCacheKey(userId)) {
      val rs = Role.roles(userId).flatten
      rs.flatMap(role => role.permissions)
    }
  }

  private def _list(page: PageRequest): PageImpl[Long] = {
    val limit = if (Global.isDbH2) {
      "order by id desc limit {rowcount} offset {offset}"
    } else if (Global.isDbMysql) {
      "order by id desc limit {offset} , {rowcount}"
    } else {
      ""
    }

    val sql = "select id from opt_role " + limit

    val list: List[Long] = DB.withConnection { implicit conn =>
      SQL(sql).on(
        'offset -> page.offset,
        'rowcount -> page.pageSize).as(long("id").*)
    }

    val c: Long = Memcached.getOrElse(countCacheKey()) {
      DB.withConnection { implicit conn =>
        SQL("select count(*) from opt_role").as(scalar[Long].single)
      }
    }

    PageImpl(page.pageNo, page.pageSize, list, c)
  }


  def list(page: PageRequest): PageImpl[Role] = {
    val idsPage = Memcached.getOrElse(listCacheKey(page.pageNo, page.pageSize)) {
      _list(page)
    }
    val rlist = idsPage.result.map(id => findOne(id)).flatten
    PageImpl(page.pageNo, page.pageSize, rlist, idsPage.total)
  }


  def toggleStatus(roleId: Long): (Boolean, String, String) = {
    findOne(roleId) match {
      case Some(role) =>
        val newrole = if (role.disable())
          ("enable", role.copy(status = Role.Status_Enable))
        else
          ("disable", role.copy(status = Role.Status_Disable))
        updateRole(newrole._2)
        (true, newrole._1, "common.ok")
      case None => (false, "none", "common.not.found")
    }
  }
}


