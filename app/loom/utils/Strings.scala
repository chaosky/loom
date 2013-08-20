package loom.utils

/**
 *
 * @author chaosky
 */
object Strings {

  def addString[T](seq: Seq[T], start: String, sep: String, end: String)(f: T => String): StringBuilder = {
    val b = new StringBuilder
    var first = true
    b append start
    for (x <- seq) {
      if (first) {
        b append f(x)
        first = false
      }
      else {
        b append sep
        b append f(x)
      }
    }
    b append end

    b
  }

}
