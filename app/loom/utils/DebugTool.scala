package loom.utils

/**
 *
 * @author chaosky
 */
object DebugTool {

  def printlnStackTrace() {
    Thread.currentThread().getStackTrace.foreach(se =>
      println(se.getClassName + "." + se.getMethodName + " "
        + se.getFileName + ":" + se.getLineNumber)
    )
  }
}
