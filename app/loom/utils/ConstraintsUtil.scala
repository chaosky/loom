package loom.utils

import play.api.data.validation._
import play.api.data.{Forms, Mapping}
import play.api.data.format.Formats._
import play.api.Logger
import language.implicitConversions
import loom.models.Account

/**
 * http://bassistance.de/jquery-plugins/jquery-plugin-validate.password/
 * @author chaosky
 */
object ConstraintsUtil {

  //  implicit def toLazyOr[T](cons: Constraint[T]) = new {
  //    def or(other: Constraint[T]) = Constraint {
  //      field: T =>
  //        cons(field) match {
  //          case Valid => other(field)
  //          case i: Invalid => i
  //        }
  //    }
  //  }

  private class ConstraintOr[T](cons: Constraint[T]) {

    def or(other: Constraint[T]): Constraint[T] = {
      Constraint {
        field =>
          cons(field) match {
            case Valid => other(field)
            case i: Invalid => i
          }
      }
    }
  }

  private implicit def toLazyOr[T](cons: Constraint[T]) = new ConstraintOr(cons)

  /**
   * Constructs a simple mapping for password field.
   *
   * Example:
   * {{{
   * Form("password" -> passwordText(minLength=3))
   * }}}
   *
   * @param minLength Text min length.
   * @param maxLength Text max length.
   */
  def passwordText(minLength: Int = 0, maxLength: Int = Int.MaxValue): Mapping[String] = {
    //    Forms.nonEmptyText(minLength, maxLength) verifying password() //chains validate
    Forms.text.verifying(Constraints.nonEmpty or Constraints.minLength(minLength) or
      Constraints.maxLength(maxLength) or password())
  }

  private val lowerRegex = "[a-z]".r
  private val upperRegex = "[A-Z]".r
  private val digitRegex = "[0-9]".r
  private val digitsRegex = "[0-9].*[0-9]".r
  private val specialRegex = "[^a-zA-Z0-9]".r
  private val sameRegex = """^(.)\1+$"""".r

  /**
   * Defines a regular expression constraint for `String` values, i.e. the string must match the regular expression pattern
   * '''name'''[constraint.password]
   * '''error'''[error.password.veryweak] or [error.password.weak]
   */
  def password(): Constraint[String] = {
    Constraint[String]("constraint.password") {
      o =>
        val lower = lowerRegex.findFirstIn(o).isDefined
        val upper = upperRegex.findFirstIn(o).isDefined
        val digit = digitRegex.findFirstIn(o).isDefined
        val digits = digitsRegex.findFirstIn(o).isDefined
        val special = specialRegex.findFirstIn(o).isDefined

        Logger.debug("lower %s upper %s digit %s digits %s special %s".format(
          lower, upper, digit, digits, special))

        if (sameRegex.findFirstIn(o).isDefined) {
          Invalid(ValidationError("error.password.veryweak"))
        } else if (lower && upper && digit || lower && digits || upper && digits || special) {
          Logger.debug("password strong: " + o)
          Valid //strong
        } else if (lower && upper || lower && digit || upper && digit) {
          Logger.debug("password good: " + o)
          Valid // good
        } else {
          Invalid(ValidationError("error.password.weak")) // weak
        }
    }

  }

  /**
   * Constructs a simple mapping for an e-mail field.
   * by Scott Gonzalez: http://projects.scottsplayground.com/email_address_validation/
   *
   * For example:
   * {{{
   *   Form("email" -> email)
   * }}}
   */
  val email: Mapping[String] = Forms.of[String] verifying Constraints.pattern(
    """((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))""".r,
    "constraint.email",
    "error.email")

  val emailAndLen = ConstraintsUtil.email.verifying(Constraints.maxLength(Account.emailMaxLen))
}
