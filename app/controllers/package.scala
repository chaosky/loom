import scala.language.implicitConversions

/**
 *
 * @author chaosky
 */
package object controllers {

  implicit def optionFoldLeft[A](o: Option[A]) = new {
    def foldLeft[B](f: A => B)(ifEmpty: => B): B = {
      o.fold(ifEmpty)(f)
    }

    def foldNull[B](f: A => B)(implicit ev: Null <:< B): B = if (o.isDefined) f(o.get) else null

  }
}
