package loom

import play.api.{Mode, Play, Logger, Application}
import loom.utils.Memcached

/**
 *
 * @author chaosky
 */
object Global {
  // s -> scala
  val cacheNameSpace = Play.mode(play.api.Play.current) match {
    case Mode.Prod => "s/loom/"
    case m => m.toString + "/s/loom/"
  }

  var isDbMysql: Boolean = false
  var isDbH2: Boolean = false

  def onStart(app: Application) {
    Logger.info("application start")
    Memcached.init()

    val dbType = Play.maybeApplication.flatMap(_.configuration.getString("database.type")).getOrElse("None")
    Logger.debug("db type " + dbType)
    dbType match {
      case "mysql" => isDbMysql = true
      case "h2" => isDbH2 = true
    }
  }

  def onStop(app: Application) {
    Logger.info("application stop")
    Memcached.destroy()
  }
}
