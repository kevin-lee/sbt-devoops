package devoops.data

import sbt.*

/** @author Kevin Lee
  * @since 2022-05-28
  */
trait CommonKeys {

  lazy val devOopsLogLevel: SettingKey[String] = settingKey(
    "Log level for DevOops tasks. It can be one of debug, info, warn and error (default: info)"
  )

}
