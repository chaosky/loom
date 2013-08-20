package loom.models

import scala.beans.BeanProperty
import scala.collection.JavaConversions._

trait Page {
  def pageNo: Int

  def pageSize: Int

  def total: Long

  def resultSize: Int

  lazy val offset = pageSize * pageNo

  lazy val totalPages = {
    if (pageSize == 0) {
      0
    } else {
      val tp = total / pageSize
      val remainder = total % pageSize
      if (remainder > 0) {
        tp + 1
      } else {
        tp
      }
    }
  }

  lazy val prev = Option(pageNo - 1).filter(_ > 0)
  lazy val next = Option(pageNo + 1).filter(_ <= totalPages)

}

case class PageRequest(private val pNo: Int, pageSize: Int) {
  val pageNo = pNo.max(1)
  lazy val offset = pageSize * (pageNo - 1)
}

case class PageImpl[T](@BeanProperty pageNo: Int, @BeanProperty pageSize: Int, result: Seq[T], @BeanProperty total: Long) extends Page {

  def this(pageNo: Int, pageSize: Int) = this(pageNo, pageSize, Nil, 0)

  override def resultSize = result.size

  def getResult(): java.util.List[T] = {
    result
  }
}

object PageImpl {

  def apply[T](p: PageRequest, total: Long, result: Seq[T]): PageImpl[T] = {
    PageImpl(p.pageNo, p.pageSize, result, total)
  }
}