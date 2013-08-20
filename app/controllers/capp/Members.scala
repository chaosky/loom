package controllers.capp

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import controllers.{Secured, BaseController}
import loom.models._
import loom.models.app._
import views._
import loom.utils.ConstraintsUtil

/**
 * app member
 * @author chaosky
 */
object Members extends Controller with BaseController with Secured {

  def list(implicit appId: Long) = AppAction(UPsn.Admin, UPsn.Member_Read) { implicit request =>
    val ulist = Member.findOne(appId) match {
      case Some(m) => m.members.map(uId => User.findOne(uId)).flatten
      case None => Nil
    }
    val owner = if (request.appSession.app.owner == request.userId)
      request.appSession.user
    else
      User.findOneSrc(request.userId)

    val hasUpdatePsn = request.appSession.hasPermissions(UPsn.Admin, UPsn.Member_Add_Del)
    Ok(html.apps.members.list(owner, ulist, hasUpdatePsn))
  }

  val memberForm = Form(single(
    "account" -> ConstraintsUtil.emailAndLen
  ))

  def addForm(implicit appId: Long) = AppAction(UPsn.Admin, UPsn.Member_Add_Del) { implicit request =>
    Ok(html.apps.members.add(memberForm))
  }

  def add(implicit appId: Long) = AppAction(UPsn.Admin, UPsn.Member_Add_Del) { implicit request =>
    val bindForm = memberForm.bindFromRequest()
    bindForm.fold(
      formWithError => BadRequest(html.apps.members.add(formWithError)),
      account => {
        val adduser = User.findOneByLoginName(account)
        adduser match {
          case None =>
            BadRequest(html.apps.members.add(bindForm.withGlobalError("common.not.found")))
          case Some(user) => {
            if (request.appSession.app.owner == user.id) {
              BadRequest(html.apps.members.add(bindForm.withGlobalError("m.app.member.error.add.self")))
            } else {
              val (ret, i18nMsg) = Member.add(appId, user.id)
              if (!ret) {
                BadRequest(html.apps.members.add(bindForm.withGlobalError(i18nMsg)))
              } else {
                Redirect(routes.Members.list(appId)).flashing(
                  "success" -> i18nMsg
                )
              }
            }
          }

        }
      }
    )
  }

  val delFrom = Form(single(
    "userId" -> longNumber
  ))

  def del(implicit appId: Long) = AppAction(UPsn.Admin, UPsn.Member_Add_Del) { implicit request =>
    delFrom.bindFromRequest().fold(
      formWithErrors => Json(Model.Error, "m.user.error.id"),
      userId => {
        val deluser = User.findOne(userId)
        deluser match {
          case None =>
            Json(NOT_FOUND, "common.not.found")
          case Some(user) => {
            val (ret, i18nMsg) = Member.remove(appId, user.id)
            if (!ret) {
              Json(Model.Error, i18nMsg)
            } else {
              Json(Model.Success, "common.success")
            }
          }
        }
      })

  }

}
