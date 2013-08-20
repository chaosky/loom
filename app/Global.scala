import play.api.{Application, GlobalSettings}

/**
 *
 * @author chaosky
 */
object Global extends GlobalSettings {

  override def onStart(app: Application) {
    super.onStart(app)

    _root_.loom.Global.onStart(app)
  }

  override def onStop(app: Application) {
    super.onStop(app)

    _root_.loom.Global.onStop(app)
  }
}
