package controllers.capp

import play.api.mvc._
import controllers.{BaseController, AppSession, AppRequest}
import loom.models.app.{Member, App, UPermission}
import loom.models.{User, Model}

/**
 *
 * @author chaosky
 */
object AppAction extends Controller with BaseController {

  def apply(f: AppRequest => Result)(implicit appId: Long): Action[AnyContent] =
    apply(Nil)(appId)(f)

  def apply(permissions: UPermission.PVal*)(f: AppRequest => Result)(implicit appId: Long): Action[AnyContent] =
    apply(permissions.toList)(appId)(f)

  def apply(permissions: List[UPermission.PVal])(appId: Long)(f: AppRequest => Result): Action[AnyContent] = Action { implicit request =>
    val email = request.session.get(Model.user_loginname)
    val user = email match {
      case Some(e) => User.findOneByLoginName(e)
      case None => None
    }

    user match {
      case None => Results.Redirect(controllers.routes.Index.login()).flashing(
        "error" -> "Unauthorized"
      )
      case Some(u) => {
        App.findOne(appId) match {
          case None => _404
          case Some(app) => {
            val appSession = AppSession(u, app)
            if (app.disabled()) {
              _404
            } else if (app.owner == u.id) {
              f(AppRequest(appSession, request))
            } else if (Member.isMember(appId, u.id)) {
              if (permissions.nonEmpty) {
                val find = permissions.exists(ps => appSession.hasPermission(ps))
                if (find) {
                  f(AppRequest(appSession, request))
                } else {
                  Results.Forbidden(views.html.defaultpages.unauthorized())
                }
              } else {
                f(AppRequest(appSession, request))
              }
            } else {
              BadRequest("not member")
            }
          }
        }
      }
    }
  }

}
