package controllers

import scala.language.implicitConversions
import loom.models.Model
import play.api.mvc.{Call, Controller, RequestHeader}
import com.alibaba.fastjson.JSONObject
import play.api.http.MimeTypes

/**
 *
 * @author chaosky
 */
trait BaseController {this: Controller =>

  val Todo = NotImplemented[play.api.templates.Html](views.html.defaultpages.todo())

  def _404()(implicit request: RequestHeader) = NotFound(views.html.error._404())

  def Json(code: Int, i18nMsg: String, other: (String, Any)*) = {
    val jsonObj = new JSONObject()
    jsonObj.put(Model.Ret, code)
    jsonObj.put(Model.Msg, i18nMsg)
    other.foreach { case (k, v) => jsonObj.put(k, v) }
    Ok(jsonObj.toJSONString).as(MimeTypes.JSON)
  }

  /**
   * @param request
   * @return (pageNo: Int, pageSize: Int)
   */
  def page(request: RequestHeader): (Int, Int) = {
    val pageNo = request.getQueryString("p").getOrElse("1").toInt
    val pageSize = request.getQueryString("ps").getOrElse("5").toInt
    //5 - 100
    val ps = 5.max(pageSize).min(100)
    (pageNo, ps)
  }

}

object Implicits {

  class CallToRichCall(call: Call) {
    def r(implicit request: RichRequest) = RCall(call)
  }

  implicit def callToRichCall(call: Call) = new CallToRichCall(call)

}
