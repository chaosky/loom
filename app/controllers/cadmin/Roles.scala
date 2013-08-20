package controllers.cadmin

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import format.Formats._
import com.alibaba.fastjson.serializer._
import com.alibaba.fastjson.{JSON => FJSON}

import views._
import loom.models._
import loom.models.admin.{Role, APermission}
import controllers._

/**
 *
 * @author chaosky
 */
object Roles extends Controller with Secured with BaseController {

  private val permissionFilter = new PropertyFilter() {
    override def apply(source: Any, name: String, value: Any): Boolean = {
      Logger.debug("class %s name %s".format(source.getClass, name))
      if (name.indexOf("$") >= 0) false else true
    }
  }

  def list() = AdminAction(APermission.Admin, APermission.Role_Read) { implicit request =>
    val (pageNo, pageSize) = page(request)
    val list = Role.list(PageRequest(pageNo, pageSize))
    val format = request.getQueryString("format").getOrElse("html")

    render {
      case Accepts.Html() if (format == "json") => Ok(FJSON.toJSONString(list, permissionFilter, SerializerFeature.PrettyFormat, SerializerFeature.DisableCircularReferenceDetect)).as(JSON)
      case Accepts.Json() => Ok(FJSON.toJSONString(list, permissionFilter)).as(JSON)
      case Accepts.Html() => Ok(html.admin.roles.list(list))
    }
  }

  val roleForm = Form(tuple(
    "name" -> nonEmptyText(minLength = Role.nameMinLen, maxLength = Role.nameMaxLen),
    "permissions" -> Forms.list(text)
  ))

  def createForm() = AdminAction(APermission.Admin, APermission.Role_Create) { implicit request =>
    Ok(html.admin.roles.createForm(roleForm))
  }

  def create() = AdminAction(APermission.Admin, APermission.Role_Create) { implicit request =>
    roleForm.bindFromRequest().fold(
      formWithError => {
        BadRequest(html.admin.roles.createForm(formWithError))
      },
      rolePost => {
        val psList = permissions(rolePost._2)

        val (ret, role, i18nMsg) = Role.create(rolePost._1, psList)
        if (!ret) {
          val nForm = roleForm.withGlobalError(i18nMsg)
          BadRequest(html.admin.roles.createForm(nForm))
        } else {
          Redirect(routes.Roles.list()).flashing(
            "success" -> "common.create.success"
          )
        }
      }
    )
  }

  private def permissions(permissionsStr: List[String])(implicit request: AdminRequest) = {
    val pslist =
      if (request.aSession.hasPermissions(APermission.Admin, APermission.Permission_Read)) {
        val permissions = permissionsStr.map { p =>
          try {
            APermission.withName(p).asInstanceOf[APermission.PVal]
          } catch {
            case e: Exception =>
              Logger.error(e.getMessage())
              null
          }
        }

        permissions.filter(p => p != null || p != APermission.Admin)
      } else Nil
    pslist
  }

  def editForm(id: Long) = AdminAction(APermission.Admin, APermission.Role_Update) { implicit request =>
    val role = Role.findOne(id)
    role match {
      case Some(r) => Ok(html.admin.roles.editForm(roleForm, r))
      case None => _404
    }

  }

  def edit(id: Long) = AdminAction(APermission.Admin, APermission.Role_Update) { implicit request =>
    val role = Role.findOne(id)
    role match {
      case None => NotFound("not found")
      case Some(r) =>
        val nForm = roleForm.bindFromRequest()
        nForm.fold(
          formWithError => {
            BadRequest(html.admin.roles.editForm(formWithError, r))
          },
          rolePost => {
            val psList = permissions(rolePost._2)

            Role.updateRole(r, rolePost._1, psList)

            Redirect(routes.Roles.list().url, Map("p" -> Seq(nForm.data.get("p").getOrElse("1")))).flashing(
              "success" -> "common.create.success"
            )
          }
        )
    }
  }

  val toggleForm = Form(single("id" -> of[Long]))

  def togglestatus() = AdminAction(APermission.Admin, APermission.Role_Disable) { implicit reqeust =>

    toggleForm.bindFromRequest().fold(
      formWithError => {
        Json(Model.Error, "m.role.error.id")
      },
      form => {
        val id = form
        val (ret, event, i18nMsg) = Role.toggleStatus(id)
        if (ret) Json(Model.Success, i18nMsg, "event" -> event)
        else Json(Model.Error, i18nMsg)
      }
    )
  }

}
