package controllers

import play.api.mvc.{RequestHeader, WrappedRequest, Request, AnyContent}
import play.api.{Play, i18n}
import play.api.i18n.Lang
import loom.models.User

class BaseRequest[A <: AnyContent](private val request: Request[A]) extends WrappedRequest(request) {
  private var langChanged = false

  def isLangChanged() = {
    lang
    langChanged
  }

  private def getLang(langStr: String): i18n.Lang = {
    i18n.Lang.get(langStr).map { l =>
      langChanged = true
      l
    }.getOrElse(lang(this))
  }

  // http method must == GET
  lazy val lang: Lang = {
    request.getQueryString(RichRequest.hl).map(hl => getLang(hl)).getOrElse(lang(this))
  }

  //play.api.mvc.Controller implicit def lang(implicit RequestHeader):Lang
  private def lang(request: RequestHeader) = {
    play.api.Play.maybeApplication.map { implicit app =>
      val maybeLangFromCookie = request.cookies.get(Play.langCookieName).flatMap(c => Lang.get(c.value))
      maybeLangFromCookie.getOrElse(play.api.i18n.Lang.preferred(request.acceptLanguages))
    }.getOrElse(request.acceptLanguages.headOption.getOrElse(play.api.i18n.Lang.defaultLang))
  }
}

// represents an abstract request that may or may not be authenticated
sealed class RichRequest(private val request: Request[AnyContent]) extends BaseRequest(request)

object RichRequest {
  val hl = "hl"

  def apply(request: Request[AnyContent]) = {
    new RichRequest(request)
  }
}

//non-authenticated request
//case class DefaultRequest(aSession: Option[ASession],
//                          private val request: Request[AnyContent]) extends RichRequest(request) {
//
//}

case class AdminRequest(aSession: ASession,
  private val request: Request[AnyContent]) extends RichRequest(request)

sealed class UserRequest(val user: User, private val request: Request[AnyContent])
  extends RichRequest(request) {
  lazy val userId = user.id
}

case class UserRequestImpl(override val user: User, private val request: Request[AnyContent])
  extends UserRequest(user, request)

case class AppRequest(appSession: AppSession, private val request: Request[AnyContent])
  extends UserRequest(appSession.user, request) {
  lazy val appId = appSession.app.id

  def isOwner = appSession.isOwner
}