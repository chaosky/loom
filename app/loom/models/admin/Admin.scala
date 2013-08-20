package loom.models.admin

import play.api.db.DB
import play.api.Play.current

import anorm.SQL
import anorm.~
import anorm.SqlParser._
import java.util.Date
import loom.Global
import loom.utils._
import loom.models._

//import play.api.Logger

import scala.language.implicitConversions

/**
 * opt account info
 *
 * @author chaosky
 */
/**
 * account info
 */
case class Admin(override val id: Long,
  override val loginName: String,
  override val email: String,
  override val password: String,
  override val salt: String,
  override val status: Int,
  override val createDate: Date) extends Account(id, loginName, email, password, salt, status, createDate)

object Admin extends AccountBuilder[Admin] {
  protected val Cache_Model = "admin"
  /** list cache timestamp. update when create account */
  @volatile protected var listTimes = System.currentTimeMillis()

  // -- Parsers
  /**
   * Parse a Admin from a ResultSet
   */
  private val simple = {
    get[Long]("id") ~
      get[String]("login_name") ~
      get[String]("email") ~
      get[String]("password") ~
      get[String]("salt") ~
      get[Int]("status") ~
      get[Date]("create_date") map { case id ~ loginName ~ email ~ password ~ salt ~ status ~ createDate =>
      Admin(id, loginName, email, password, salt, status, createDate)
    }
  }

  protected def _findOneByLoginName(loginName: String): Option[Admin] = {
    val ret = DB.withConnection { implicit connection =>
      SQL(
        """select * from opt_admin where login_name = {loginName}""").on(
        'loginName -> loginName).as(simple.singleOpt)
    }
    ret
  }

  protected def _findOne(id: Long): Option[Admin] = {
    val ret = DB.withConnection { implicit connection =>
      SQL(
        """select * from opt_admin where id = {id}""").on(
        'id -> id).as(simple.singleOpt)
    }
    ret
  }

  /**
   * create account & generate password
   * @return (flat, Option,newPassword, i18nMsg)
   */
  def create(email: String): (Boolean, Option[Admin], String, String) = {
    findOneByLoginName(email) match {
      case Some(_) => return (false, None, "", "m.account.error.email.registered")
      case None => None
    }

    val status = Status.None
    val createDate = new Date()
    val salt = Codecs.generateSaltStr(pswSaltLen)
    val password = Codecs.generateSaltStr(Account.pwdMaxLen)
    val entryptPassword = Codecs.sha1str(password, salt, pswHashAlgorithm)

    val id: Option[Long] = DB.withConnection { implicit conn =>
      SQL(
        """INSERT INTO opt_admin (login_name, email, password, salt,
          | status, create_date)
          | VALUES ({login_name}, {email}, {password}, {salt}, {status}, {createDate})
        """.stripMargin).on(
        'email -> email,
        'login_name -> email,
        'password -> entryptPassword,
        'salt -> salt,
        'status -> status.id,
        'createDate -> createDate).executeInsert()
    }

    id match {
      case Some(i) =>
        listTimes = System.currentTimeMillis()
        val u = Some(Admin(i, email, email, entryptPassword, salt, status.id, createDate))
        (true, u, password, "common.create.success")
      case None => (false, None, "", "common.error.unknown")
    }
  }

  protected def _update(u: Admin) = {
    DB.withConnection { implicit conn =>
      SQL(
        """UPDATE opt_admin SET email = {email},
          | password = {password}, salt = {salt}, status = {status}
          | WHERE id = {id}
        """.stripMargin).on(
        'email -> u.email,
        'password -> u.password,
        'salt -> u.salt,
        'status -> u.status,
        'id -> u.id).executeUpdate()
    }
  }

  protected def count(): Long = {
    Memcached.getOrElse(countCacheKey()) {
      val count = DB.withConnection { implicit conn =>
        SQL("select count(*) from opt_admin").as(scalar[Long].single)
      }
      count
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

    val sql = "select id from opt_admin " + limit

    val list: List[Long] = DB.withConnection { implicit conn =>
      SQL(sql).on(
        'offset -> page.offset,
        'rowcount -> page.pageSize).as(long("id").*)
    }

    val c: Long = count()
    PageImpl(page.pageNo, page.pageSize, list, c)
  }

  def update(a: Admin, email: String): Int = {
    val newa = a.copy(email = email)
    update(newa)
  }

  def list0[B](page: PageRequest)(f: Long => B): PageImpl[B] = {
    val idsPage = Memcached.getOrElse(listCacheKey(page.pageNo, page.pageSize)) {
      _list(page)
    }
    val ulist = idsPage.result.map(id => f(id))
    PageImpl(page.pageNo, page.pageSize, ulist, idsPage.total)
  }

  def list(page: PageRequest): PageImpl[Admin] = {
    list0(page)(id => findOne(id).get)
  }

  def listWithRole(page: PageRequest): PageImpl[(Admin, List[Option[Role]])] = {
    list0(page)(id => (findOne(id).get, Role.roles(id)))
  }

  def toggleStatus(id: Long): (Boolean, String, String) = toggleStatus(id, Status.Disable)

  protected def updatePassword(admin: Admin, newentryptPassword: String, newSalt: String) {
    val newAccount = admin.copy(password = newentryptPassword, salt = newSalt)
    update(newAccount)
  }

  protected def copy(admin: Admin, status: Int): Admin = admin.copy(status = status)

}
