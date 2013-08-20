package loom.models

import java.util.Date
import loom.utils.{Password, Memcached, Codecs}
import loom.Global

/**
 *
 * @author chaosky
 */
@SerialVersionUID(0 - 4651849468548790589L)
abstract class Account(val id: Long,
  val loginName: String,
  val email: String,
  val password: String,
  val salt: String,
  val status: Int,
  val createDate: Date) extends Serializable {

  lazy val statusSeq: List[Status#StatusVal] = {
    Status.values.toList.map { s =>
      if ((s.id & status) != 0) {
        s.asInstanceOf[Status#StatusVal]
      } else {
        null
      }
    }
  }

  def contains(sval: Status#StatusVal): Boolean = {
    (sval.id & status) != 0
  }

  def addStatus(sval: Status#StatusVal): Int = {
    status | sval.id
  }

  def removeStatus(sval: Status#StatusVal): Int = {
    status ^ sval.id
  }

}

object Account {
  val emailMinLen = 8
  val emailMaxLen = 64
  val pwdMinLen = 8
  val pwdMaxLen = 64


}

trait AccountBuilder[T <: Account] {
  val pswHashAlgorithm = 1024
  val pswSaltLen = 64
  //abstract ==========
  protected var listTimes: Long

  protected val Cache_Model: String

  protected def updatePassword(t: T, newentryptPassword: String, newSalt: String): Unit

  protected def _update(t: T): Int

  protected def _findOne(id: Long): Option[T]

  protected def _findOneByLoginName(loginName: String): Option[T]

  protected def copy(t: T, status: Int): T

  //abstract ==========

  protected def idCacheKey(id: Long) = {
    Global.cacheNameSpace + "m/" + Cache_Model + "/id/0/" + id
  }

  protected def loginNameCacheKey(email: String) = {
    Global.cacheNameSpace + "m/" + Cache_Model + "/loginname/0/" + email
  }

  protected def countCacheKey() = {
    Global.cacheNameSpace + "m/" + Cache_Model + "/listcount/0/ts/" + listTimes
  }

  protected def listCacheKey(pageNo: Int, pageSize: Int) = {
    Global.cacheNameSpace + "m/" + Cache_Model + "/list/0/pn/" + pageNo + "/ps/" + pageSize + "/ts/" + listTimes
  }

  def update(t: T): Int = {
    cleanCache(t.id, t.loginName)
    _update(t)
  }

  def findOne(id: Long): Option[T] = {
    val ret = Memcached.getOrElse(idCacheKey(id)) {
      _findOne(id).getOrElse(null)
    }

    Option(ret.asInstanceOf[T])
  }

  protected def cleanCache(id: Long, loginName: String) {
    Memcached.delete(idCacheKey(id))
    Memcached.delete(loginNameCacheKey(loginName))
  }

  def changePassword(id: Long, currentPwd: String, newPwd: String): (Boolean, String) = {
    findOne(id) match {
      case Some(a) =>
        val digitCurrentPwd = Codecs.sha1str(currentPwd, a.salt, pswHashAlgorithm)
        if (digitCurrentPwd != a.password) {
          return (false, "m.password.error.current.incorrect")
        } else {
          val newSalt = Codecs.generateSaltStr(pswSaltLen)
          val entryptPassword = Codecs.sha1str(newPwd, newSalt, pswHashAlgorithm)
          updatePassword(a, entryptPassword, newSalt)
          (true, "m.password.msg.changed")
        }
      case None => (false, "common.not.found")
    }
  }

  def findOneByLoginName(loginName: String): Option[T] = {
    val id = Memcached.get[Long](loginNameCacheKey(loginName))
    id match {
      case Some(aid) => findOne(aid) //hit cache
      case None => {
        //miss cache
        val account = _findOneByLoginName(loginName)
        account match {
          case Some(u) => {
            //find
            Memcached.set(loginNameCacheKey(loginName), u.id)
            Memcached.set(idCacheKey(u.id), u)
            account
          }
          case None => account
        }
      }
    }
  }

  /**
   * Authenticate a Admin.
   */
  def authenticate(email: String, password: String): Boolean = {
    val ret = findOneByLoginName(email)

    ret match {
      case Some(u) => {
        val entryptPwd = Password.entryptPassword(password, u.salt, pswHashAlgorithm)
        //        Logger.debug("pwd %s entryPwd %s uPwd %s".format(password, entryptPwd, u.password))
        entryptPwd == u.password
      }
      case None => false
    }

  }

  def addStatus(id: Long, sval: Status#StatusVal): (Boolean, String) = {
    findOne(id) match {
      case Some(u) =>
        if (u.contains(sval)) {
          (true, "common.nothing.happened")
        } else {
          val newAccount = copy(u, u.addStatus(sval))
          update(newAccount)
          (true, "common.ok")
        }
      case None => (false, "common.not.found")
    }
  }


  def toggleStatus(id: Long, sval: Status#StatusVal): (Boolean, String, String) = {
    findOne(id) match {
      case Some(admin) =>
        val newadmin = if (admin.contains(sval))
          ("enable", copy(admin, admin.removeStatus(sval)))
        else
          ("disable", copy(admin, admin.addStatus(sval)))
        update(newadmin._2)
        (true, newadmin._1, "common.ok")
      case None => (false, "none", "common.not.found")
    }
  }

}