package controllers.cadmin

import play.api.mvc._
import play.api.i18n._
import play.api.data._
import play.api.data.Forms._
import views._

import controllers._
import loom.models._
import loom.utils._
import loom.models.admin._
import loom.models.{Status => UStatus}

/**
 * manage account
 * @author chaosky
 */
object Admins extends Controller with Secured with BaseController {

  /**
   * page list
   * @return
   */
  def list() = AdminAction(APsn.Admin, APsn.Account_Read) { implicit request =>
    val (pageNo, pageSize) = page(request)
    val list = Admin.list(PageRequest(pageNo, pageSize))
    Ok(html.admin.admins.list(list))
  }

  val aForm = Form(single(
    "email" -> ConstraintsUtil.emailAndLen
  ))

  /**
   * create form
   * @return
   */
  def createForm() = AdminAction(APsn.Admin, APsn.Account_Create) { implicit request =>
    Ok(html.admin.admins.createForm(aForm))
  }

  /**
   * create admin
   * @return
   */
  def create() = AdminAction(APsn.Admin, APsn.Account_Create) { implicit request =>
    aForm.bindFromRequest().fold(
      formWithError => {
        BadRequest(html.admin.admins.createForm(formWithError))
      },
      account => {
        val (ret, u, password, i18nMsg) = Admin.create(account)
        if (!ret) {
          val nForm = aForm.withGlobalError(i18nMsg)
          BadRequest(html.admin.admins.createForm(nForm))
        } else {
          Redirect(routes.Admins.list()).flashing(
            "success" -> "common.create.success",
            "flag" -> password
          )
        }

      }
    )
  }

  def editForm(id: Long) = AdminAction(APsn.Admin, APsn.Account_Update) { implicit request =>
    val account = Admin.findOne(id)
    account match {
      case Some(a) => Ok(html.admin.admins.editForm(aForm, a))
      case None => NotFound("common.not.found")
    }
  }

  /**
   * edit. update email
   * @param id
   * @return
   */
  def edit(id: Long) = AdminAction(APsn.Admin, APsn.Account_Update) { implicit request =>
    val account = Admin.findOne(id) //TODO need password change email
    account match {
      case None => NotFound(Messages("common.not.found"))
      case Some(a) =>
        val nForm = aForm.bindFromRequest()
        nForm.fold(
          formWithError => {
            BadRequest(html.admin.admins.editForm(formWithError, a))
          },
          post => {
            Admin.update(a, post)

            Redirect(cadmin.routes.Admins.list().url, Map("p" -> Seq(nForm.data.get("p").getOrElse("1")))).flashing(
              "success" -> "common.create.success"
            )
          }
        )
    }
  }

  /** post */
  def resetpassword() = AdminAction(APsn.Admin, APsn.Account_ResetPassword) { implicit request =>
    try {
      val id = request.getQueryString("id").map(_.toLong).get
      val (ret, i18nMsg) = Admin.addStatus(id, UStatus.NeedResetPassword)

      if (ret) Json(Model.Success, i18nMsg)
      else Json(Model.Error, i18nMsg)
    } catch {
      case ex: NumberFormatException =>
        Json(Model.Error, "common.error.id")
    }
  }

  def toggleStatus() = AdminAction(APsn.Admin, APsn.Account_ToggleStatus) { implicit request =>
    try {
      val id = request.getQueryString("id").map(_.toLong).get
      if (Role.Admin_Role_Id == id) Json(FORBIDDEN, "FORBIDDEN")
      else {
        val (ret, event, i18nMsg) = Admin.toggleStatus(id)
        if (ret) Json(Model.Success, i18nMsg, "event" -> event)
        else Json(Model.Error, i18nMsg)
      }
    } catch {
      case ex: NumberFormatException =>
        Json(Model.Error, "common.error.id")
    }
  }

}
