package loom.utils

import scala.collection.JavaConversions._

import play.api.{Logger, Play}
import net.rubyeye.xmemcached._
import net.rubyeye.xmemcached.utils.AddrUtil
import java.io.Serializable
import scala.collection.Map

/**
 * memcached
 * @author chaosky
 */
object Memcached {
  private val defaultExpiration = 3600

  private def servers: Option[String] = {
    Play.maybeApplication.flatMap(_.configuration.getString("memcached.servers"))
  }

  private[loom] def init() {
    _client = newClient()
  }

  private[loom] def destroy() {
    client().map(c => c.shutdown())
    _client = null
  }

  private var _client: Option[MemcachedClient] = None


  def client() = _client

  private def newClient(): Option[MemcachedClient] = {
    import play.api.Play.current
    if (Play.isProd && (servers.isEmpty || servers.get.isEmpty)) {
      sys.error("You must provide an property value 'memcached.servers' of application.conf" +
        " when Application in prod mode")
    }

    servers match {
      case Some(sers) if (!sers.isEmpty) => {
        val builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(sers))
        builder.setConnectionPoolSize(Runtime.getRuntime.availableProcessors())
        val client = builder.build()
        Some(client)
      }
      case _ => None
    }
  }

  //execute memcached client operate
  private def execute[T](existClient: (MemcachedClient) => T)(nonExistentClient: => T): T = {
    client() match {
      case Some(client) => existClient(client)
      case None => nonExistentClient
    }
  }

  private def executeLeft[T](nonExistentClient: => T)(existClient: (MemcachedClient) => T): T = execute(existClient)(nonExistentClient)

  def delete(keyGen: => String) = {
    execute { client =>
      val key = keyGen
      client.delete(key)
    }(true)
  }

  def get[T](keyGen: => String): Option[T] = {
    execute { client =>
      val key = keyGen
      val ret: T = client.get(key)
      val opt = Option(ret)
      if (opt.isDefined) Logger.debug("get hit cache. key:" + key)
      opt
    }(None)
  }

  def set(keyGen: => String, value: Any, expiration: Int = defaultExpiration) = {
    execute { client =>
      val key = keyGen
      client.set(key, expiration, value)
    }(true)
  }

  // memcached get
  def getOrElse[T](kenGen: => String, expiration: Int = defaultExpiration)(misscache: => T): T = {
    execute { client =>
      val key = kenGen
      val ret: T = client.get(key)

      Option(ret) match {
        case Some(x) => Logger.debug("get hit cache. key:" + key)
          x //hit
        case None => {
          val entity = misscache //miss
          Logger.debug("miss cache. key:" + key)
          entity match {
            case null => entity
            case other => client.set(key, expiration, entity)
              entity
          }
        }
      }

    }(misscache)
  }

  def getsOrElseSeq[T <: Serializable, ID](keyGen: => Seq[(String, ID)], expiration: Int = defaultExpiration)(misscache: ID => Option[T]): List[Option[T]] = {
    val b = new java.util.LinkedHashMap[String, ID]
    for (x <- keyGen)
      b.put(x._1, x._2)
    getsOrElse[T, ID](b, expiration)(misscache)
  }

  def getsOrElse[T <: Serializable, ID](keyGen: => Map[String, ID], expiration: Int = defaultExpiration)(misscache: ID => Option[T]): List[Option[T]] = {
    val keys = keyGen
    executeLeft(keys.values.map(misscache(_)).toList) { client =>
      val rets = client.gets(keys.keys, expiration)

      val rmap = rets.map { case (key: String, resp: GetsResponse[T]) =>
        val ret: T = resp.getValue
        Option(ret) match {
          case x: Some[T] =>
            Logger.debug("gets hit cache. key: %s val %s".format(key, ret))
            x
          case None =>
            val entity = misscache(keys(key))
            Logger.debug("gets miss cache. key: %s val %s".format(key, entity))
            entity match {
              case Some(e) => client.set(key, expiration, e)
                entity
              case None => None
            }
        }

      }
      rmap.toList
    }
  }

}
