package controllers.cadmin

import play.api.test.Helpers._
import controllers.Msg
import loom.models.admin.Role

/**
 *
 * @author chaosky
 */
class AdminsSpec extends AdminBaseTest with loom.DBSpec {

  "admins" should {
    "list page" in {
      implicit val req = request(GET, url(routes.Admins.list(), page(1)), Nil)
      val ret = route(req.fake).get
      contentContain(ret, Msg("m.account.create"))
    }
    "cann't toggle status super admin" in {
      val req = request(POST, url(routes.Admins.toggleStatus(), Seq("id" -> Role.Admin_Role_Id)))
      val ret = route(req.fake).get
      contentContain(ret, "FORBIDDEN")
    }
  }
}
