package devoops.data

import loggerf.logger.CanLog

/** @author Kevin Lee
  * @since 2021-09-19
  */
object Logging {

  def printlnCanLog(logLevel: Option[DevOopsLogLevel]): CanLog = {
    val ignoreLogging                  = (_: String) => ()
    val (debugF, infoF, warnF, errorF) = logLevel match {
      case Some(DevOopsLogLevel.Debug) =>
        ((message: String) => println(s"[DEBUG] $message"), ignoreLogging, ignoreLogging, ignoreLogging)
      case Some(DevOopsLogLevel.Info)  =>
        (ignoreLogging, (message: String) => println(s"[INFO] $message"), ignoreLogging, ignoreLogging)
      case Some(DevOopsLogLevel.Warn)  =>
        (ignoreLogging, ignoreLogging, (message: String) => println(s"[WARN] $message"), ignoreLogging)
      case Some(DevOopsLogLevel.Error) =>
        (ignoreLogging, ignoreLogging, ignoreLogging, (message: String) => println(s"[ERROR] $message"))
      case None                        =>
        (ignoreLogging, ignoreLogging, ignoreLogging, ignoreLogging)
    }
    new CanLog {
      override def debug(message: => String): Unit = debugF(message)
      override def info(message: => String): Unit  = infoF(message)
      override def warn(message: => String): Unit  = warnF(message)
      override def error(message: => String): Unit = errorF(message)
    }
  }

}
