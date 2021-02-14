package devoops.data

import cats._
import cats.syntax.eq._
import kevinlee.sbt.SbtCommon

/** @author Kevin Lee
  * @since 2021-02-14
  */
sealed trait DevOopsLogLevel

object DevOopsLogLevel {

  case object Debug extends DevOopsLogLevel
  case object Info  extends DevOopsLogLevel
  case object Warn  extends DevOopsLogLevel
  case object Error extends DevOopsLogLevel

  def debug: DevOopsLogLevel = Debug
  def info: DevOopsLogLevel  = Info
  def warn: DevOopsLogLevel  = Warn
  def error: DevOopsLogLevel = Error

  def render(devOopsLogLevel: DevOopsLogLevel): String = devOopsLogLevel match {
    case DevOopsLogLevel.Debug =>
      "debug"
    case DevOopsLogLevel.Info  =>
      "info"
    case DevOopsLogLevel.Warn  =>
      "warn"
    case DevOopsLogLevel.Error =>
      "error"
  }

  def fromStringUnsafe(devOopsLogLevel: String): DevOopsLogLevel = devOopsLogLevel match {
    case "debug" | "DEBUG" | "Debug" =>
      DevOopsLogLevel.debug
    case "info" | "INFO" | "Info"    =>
      DevOopsLogLevel.info
    case "warn" | "WARN" | "Warn"    =>
      DevOopsLogLevel.warn
    case "error" | "ERROR" | "Error" =>
      DevOopsLogLevel.error
    case _                           =>
      SbtCommon.messageOnlyException(
        s"Unknown DevOopsLogLevel. It should be one of debug, info, warn and error. [input: $devOopsLogLevel)"
      )
  }

  implicit val eq: Eq[DevOopsLogLevel] = Eq.fromUniversalEquals[DevOopsLogLevel]

  implicit final class DevOopsLogLevelOps(val devOopsLogLevel: DevOopsLogLevel) extends AnyVal {
    def isDebug: Boolean = devOopsLogLevel === DevOopsLogLevel.Debug
    def isInfo: Boolean  = devOopsLogLevel === DevOopsLogLevel.Info
    def isWarn: Boolean  = devOopsLogLevel === DevOopsLogLevel.Warn
    def isError: Boolean = devOopsLogLevel === DevOopsLogLevel.Error

    def render: String = DevOopsLogLevel.render(devOopsLogLevel)
  }

}
