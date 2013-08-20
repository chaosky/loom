package controllers

import play.api.Logger
import play.api.mvc.{AnyContent, Call}
import play.api.i18n.Messages


class RichCall(val hasLang: Boolean, method: String, url: String) extends Call(method, url) {

  import loom.utils.Strings._

  def tourl(params: (String, Any)*) = {
    if (hasLang) {
      url + addString(params, "&", "&", "")(t => t._1 + "=" + t._2)
    } else {
      url + addString(params, "?", "&", "")(t => t._1 + "=" + t._2)
    }
  }
}

object RCall {

  def apply(call: Call)(implicit request: RichRequest) = {
    // Logger.debug("call => hl %s method %s url %s".format(request.isLangChanged(), call.method, call.url))
    if (!request.isLangChanged) new RichCall(false, call.method, call.url)
    else new RichCall(true, call.method, call.url + "?" + RichRequest.hl + "=" + request.lang.code)
  }
}

object Msg {

  def apply[A <: AnyContent](key: String, args: Any*)(implicit request: BaseRequest[A]): String = {
    Messages(key, args: _*)(request.lang)
  }

}

