package controllers.cadmin

import play.api.test._
import Helpers._
import loom.models.admin.Role

/**
 *
 * @author chaosky
 */
class PermissionsSpec extends AdminBaseTest with loom.DBSpec {

  "permissions" should {
    "cann't view super admin permissions" in {
      val req = request(POST, url(routes.Permissions.editForm(Role.Admin_Role_Id), Nil))
      val ret = route(req.fake).get
      status_==(ret, FORBIDDEN)
    }
    "cann't edit super admin permissions" in {
      val req = request(POST, url(routes.Permissions.edit(Role.Admin_Role_Id), Nil))
      val ret = route(req.fake).get
      status_==(ret, FORBIDDEN)
    }
  }
}
