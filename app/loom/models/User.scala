package loom.models

import java.util.Date
import loom.utils.{Codecs, Memcached}
import loom.Global

import play.api.db.DB
import play.api.Play.current

import anorm.SQL
import anorm.~
import anorm.SqlParser._

import scala.language.implicitConversions

/**
 *
 * @author chaosky
 */
@SerialVersionUID(7860183100747008853L)
case class User(override val id: Long,
  override val loginName: String,
  name: String,
  override val email: String,
  newEmail: String,
  override val password: String,
  override val salt: String,
  passwordVersion: Int,
  override val status: Int,
  override val createDate: Date) extends Account(id, loginName, email, password, salt, status, createDate) {

}

object User extends AccountBuilder[User] {
  protected val Cache_Model = "user"
  /** list cache timestamp. update when create account */
  @volatile protected var listTimes = System.currentTimeMillis()

  private val simple = {
    get[Long]("id") ~
      get[String]("login_name") ~
      get[String]("name") ~
      get[String]("email") ~
      get[String]("new_email") ~
      get[String]("password") ~
      get[String]("salt") ~
      get[Int]("pv") ~
      get[Int]("status") ~
      get[Date]("create_date") map {
      case id ~ loginName ~ name ~ email ~ newEmail ~ password ~ salt ~ pv ~ status ~ createDate =>
        User(id, loginName, name, email, newEmail, password, salt, pv, status, createDate)
    }
  }

  def create(name: String, email: String, password: String) = {
    val status = Status.Disable
    val createDate = new Date
    val salt = Codecs.generateSaltStr(pswSaltLen)
    val entryptPwd = Codecs.sha1str(password, salt, pswHashAlgorithm)

    val id: Option[Long] = DB.withConnection { implicit conn =>
      SQL(
        """insert into opt_user (login_name, name, email, password, salt,
          | status, create_date)
          | values ({loginName}, {name}, {email}, {password}, {salt}, {status}, {createDate})
        """.stripMargin).on(
        'loginName -> email,
        'name -> name,
        'email -> email,
        'password -> entryptPwd,
        'salt -> salt,
        'status -> status.id,
        'createDate -> createDate
      ).executeInsert()
    }

    id match {
      case Some(i) =>
        listTimes = System.currentTimeMillis()
        val u = Some(User(i, email, name, email, "", entryptPwd, salt, 0, status.id, createDate))
        (true, u, "common.create.success")
      case None => (false, None, "common.error.unknown")
    }
  }

  protected def _update(user: User) = {
    DB.withConnection { implicit conn =>
      SQL(
        """update opt_user set name = {name}, email = {email},
          | new_email = {newEmail}, password = {password}, salt = {salt},
          | pv = {pv}, status = {status} where id = {id}
        """.stripMargin).on(
        'name -> user.name,
        'email -> user.email,
        'newEmail -> user.newEmail,
        'password -> user.password,
        'salt -> user.salt,
        'pv -> user.passwordVersion,
        'status -> user.status,
        'id -> user.id
      ).executeUpdate()
    }
  }

  protected def _findOne(id: Long): Option[User] = {
    val ret = DB.withConnection { implicit conn =>
      SQL("select * from opt_user where id = {id}").on(
        'id -> id).as(simple.singleOpt)
    }
    ret
  }

  protected def _findOneByLoginName(loginName: String): Option[User] = {
    DB.withConnection { implicit conn =>
      SQL("select * from opt_user where login_name = {loginName}").on(
        'loginName -> loginName).as(simple.singleOpt)
    }
  }

  def findOneSrc(userId: Long): User = {
    Memcached.getOrElse(idCacheKey(userId)) {
      _findOne(userId).getOrElse(null)
    }
  }

  def count(): Long = {
    Memcached.getOrElse(countCacheKey()) {
      val count = DB.withConnection { implicit conn =>
        SQL("select count(*) from opt_user").as(scalar[Long].single)
      }
      count
    }
  }

  def _list(page: PageRequest): PageImpl[Long] = {
    val limit = if (Global.isDbH2) {
      "order by id desc limit {rowcount} offset {offset}"
    } else if (Global.isDbMysql) {
      "order by id desc limit {offset}, {rowcount}"
    } else {"" }

    val sql = "select id from opt_user " + limit

    val list: List[Long] = DB.withConnection { implicit conn =>
      SQL(sql).on('offset -> page.offset, 'rowcount -> page.pageSize).as(long("id").*)
    }

    val c: Long = count()
    PageImpl(page, c, list)
  }

  def list(page: PageRequest): PageImpl[User] = {
    val idsPage = Memcached.getOrElse(listCacheKey(page.pageNo, page.pageSize)) {
      _list(page)
    }
    val ulist = Memcached.getsOrElseSeq(idsPage.result.map(id => (idCacheKey(id), id))) { id =>
      _findOne(id)
    }.flatten
    PageImpl(page, idsPage.total, ulist)
  }

  protected def updatePassword(user: User, newentryptPassword: String, newSalt: String) {
    val newuser = user.copy(password = newentryptPassword, salt = newSalt)
    update(newuser)
  }

  protected def copy(user: User, status: Int): User = user.copy(status = status)

  def toggleStatus(id: Long): (Boolean, String, String) = toggleStatus(id, Status.Disable)

  def main(args: Array[String]) {
    val template =
      """|(%1$s, 'user%1$s@example.com', 'user%1$s@example.com', '',
        |'65c2bff865381253e594bcced67d1038b7397a72', 'salt', 1, 0,
        |  PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss'))""".stripMargin

    val str = for (id <- 1 until 11)
    yield template.format(id)

    val id = 999999999
    str foreach (s => println(s + ","))
    println(template.format(id))
  }
}
