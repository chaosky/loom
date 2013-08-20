package controllers

import play.api.mvc._
import loom.models.{Status => UStatus, User, Model}
import loom.models.admin.{Admin, APermission}

trait Secured {this: Controller =>

  def RichAction(f: RichRequest => Result): Action[AnyContent] = Action { request =>
    f(RichRequest(request))
  }

  /**
   * Redirect to login if the user in not authorized.
   */
  //  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login()).flashing(
  //    "error" -> "Unauthorized"
  //  )

  def AdminAction(f: AdminRequest => Result): Action[AnyContent] = AdminAction(Nil)(f)

  def AdminAction(permissions: APermission.PVal*)(f: AdminRequest => Result): Action[AnyContent] = AdminAction(permissions.toList)(f)

  private def AdminAction(permissions: List[APermission.PVal])(f: AdminRequest => Result): Action[AnyContent] = Action { implicit request =>
    val email = request.session.get(Model.admin_loginname)
    email match {
      case Some(e) => {
        Admin.findOneByLoginName(e) match {
          case Some(admin) => {
            if (admin.contains(UStatus.Disable)) {
              BadRequest(views.html.error._500("m.login.account.disable"))
            } else if (admin.contains(UStatus.EmailNotVerified)) {
              BadRequest(views.html.error._500("m.login.account.need.verify.email"))
            } else if (admin.contains(UStatus.NeedResetPassword)) {
              BadRequest(views.html.error._500("m.login.account.need.reset.password"))
            } else {
              val asession = ASession(admin)

              if (permissions.nonEmpty) {
                val find = permissions.exists(ps => asession.hasPermission(ps))
                if (find) {
                  f(AdminRequest(asession, request))
                } else {
                  Results.Forbidden(views.html.defaultpages.unauthorized())
                }
              } else {
                f(AdminRequest(asession, request))
              }
            }
          }
          case None => BadRequest(views.html.error._500("m.login.account.not.found"))
        }
      }
      case None => Results.Redirect(cadmin.routes.Application.login()).flashing(
        "error" -> "Unauthorized"
      )
    }
  }

  def UserAction(f: UserRequest => Result): Action[AnyContent] = Action { implicit request =>
    val email = request.session.get(Model.user_loginname)
    email match {
      case Some(e) => {
        User.findOneByLoginName(e) match {
          case Some(user) => {
            if (user.contains(UStatus.Disable)) {
              BadRequest(views.html.error._500("m.login.account.disable"))
            } else if (user.contains(UStatus.EmailNotVerified)) {
              BadRequest(views.html.error._500("m.login.account.need.verify.email"))
            } else if (user.contains(UStatus.NeedResetPassword)) {
              BadRequest(views.html.error._500("m.login.account.need.reset.password"))
            } else {
              f(UserRequestImpl(user, request))
            }
          }
          case None => BadRequest(views.html.error._500("m.login.account.not.found"))
        }
      }
      case None => Results.Redirect(routes.Index.login()).flashing(
        "error" -> "Unauthorized"
      )
    }
  }
}

