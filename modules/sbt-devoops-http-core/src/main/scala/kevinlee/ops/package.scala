package kevinlee

import kevinlee.http.HttpRequest.sensitiveHeadersFromHttp4sInLowerCase

import java.util.Locale

/** @author Kevin Lee
  * @since 2021-02-06
  */
package object ops {

  def shouldProtect(s: String): Boolean = {
    val stringInLower = s.toLowerCase(Locale.ENGLISH)
    (
      stringInLower.startsWith("auth") ||
      stringInLower.contains("password") ||
      stringInLower.endsWith("-key") ||
      stringInLower.endsWith("_key") ||
      stringInLower.endsWith("-token") ||
      stringInLower.endsWith("_token") ||
      sensitiveHeadersFromHttp4sInLowerCase.contains(stringInLower)
    )
  }

}
