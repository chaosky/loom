package loom.utils

import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * message digest
 * @author chaosky
 */
object Codecs {
  val hex = "0123456789ABCDEF"
  lazy val random = SecureRandom.getInstance("SHA1PRNG")

  def sha1(input: Array[Byte], salt: Array[Byte], iterations: Int): Array[Byte] = {
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(new SecretKeySpec(salt, "HmacSHA1"))
    val r = (1 to iterations).foldLeft(mac.doFinal(input)) {
      (result: Array[Byte], i: Int) =>
        mac.reset()
        mac.doFinal(result)
    }
    r
  }

  def sha1str(input: String, salt: String, iterations: Int): String = {
    val r = sha1(input.getBytes("utf-8"), salt.getBytes("utf-8"), iterations)
    toHexString(r)
  }

  // --

  private val hexChars = Array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

  /**
   * Converts a byte array into an array of characters that denotes a hexadecimal representation.
   */
  def toHex(array: Array[Byte]): Array[Char] = {
    val result = new Array[Char](array.length * 2)
    for (i <- 0 until array.length) {
      val b = array(i) & 0xff
      result(2 * i) = hexChars(b >> 4)
      result(2 * i + 1) = hexChars(b & 0xf)
    }
    result
  }

  /**
   * Converts a byte array into a `String` that denotes a hexadecimal representation.
   */
  def toHexString(array: Array[Byte]): String = {
    new String(toHex(array))
  }

  def generateSalt(len: Int): Array[Byte] = {
    val bytes = Array.fill(len)(0.byteValue)
    random.nextBytes(bytes)
    bytes
  }

  def generateSaltStr(len: Int): String = {
    toHexString(generateSalt(len / 2))
  }

  def main(args: Array[String]) {
    println(Codecs.sha1str("example@example.com", "salt", 1024))

    //benchmark
    val hash_algorithm = 1024
    val password = Codecs.generateSaltStr(15)
    val salt = Codecs.generateSaltStr(64)
    println("pwd " + password)
    println("salt " + salt)

    val start = System.nanoTime()
    val num = 1000

    val s = (1 to num).foldLeft(0) {
      (sum: Int, i: Int) =>
        val d = Codecs.sha1str(password, salt, hash_algorithm)
        sum + d.length
    }
    val end = System.nanoTime() - start
    println("num : " + num + " nano: " + end + " sec: " + end / (1000.0 * 1000 * 1000) + " total string len: " + s)
  }
}

object Password {

  def entryptPassword(password: String, salt: String, iterations: Int) = {
    Codecs.sha1str(password, salt, iterations)
  }
}