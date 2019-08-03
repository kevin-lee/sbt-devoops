package kevinlee.git

import java.io.File

import kevinlee.fp.JustSyntax._

import scala.sys.process.ProcessLogger

// $COVERAGE-OFF$
/**
  * @author Kevin Lee
  * @since 2019-01-01
  */
final case class ResultCollector() extends ProcessLogger {

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var outs: List[String] = Nil

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var errs: List[String] = Nil

  def outputs: List[String] = outs.reverse
  def errors: List[String] = errs.reverse

  override def out(s: => String): Unit = {
    outs = s :: outs
    ()
  }

  override def err(s: => String): Unit = {
    errs = s :: errs
    ()
  }

  override def buffer[T](f: => T): T = f

  override def toString: String =
    s"${getClass.getSimpleName}(outputs=$outputs, errors=$errors)"
}

sealed trait ProcessResult {
  def code: Int
}

object ProcessResult {

  final case class Success(outputs: List[String]) extends ProcessResult {
    val code: Int = 0
  }
  final case class Failure(override val code: Int, errors: List[String]) extends ProcessResult

  def success(outputs: List[String]): ProcessResult =
    Success(outputs)

  def failure(code: Int, errors: List[String]): ProcessResult =
    Failure(code, errors)

  def processResult(code: Int, resultCollector: ResultCollector): ProcessResult =
    if (code === 0) {
      /* Why concatenate outputs and errors in success?
       * Sometimes errors has some part of success result. :(
       */
      success(resultCollector.outputs ++ resultCollector.errors )
    } else {
      failure(code, resultCollector.errors)
    }

  def toEither[A, B](processResult: ProcessResult)(resultToEither: PartialFunction[ProcessResult, Either[A, B]]): Either[A, B] =
    resultToEither(processResult)
}

sealed trait SysProcess

object SysProcess {
  final case class SingleSysProcess(commands: Seq[String], baseDir: Option[File]) extends SysProcess

  def process(baseDir: Option[File], commands: Seq[String]): SysProcess =
    SingleSysProcess(commands, baseDir)

  def run(sysProcess: SysProcess): ProcessResult = sysProcess match {
    case SingleSysProcess(commands, baseDir) =>
      val resultCollector = ResultCollector()
      val code =
        baseDir.fold(
          sys.process.Process(commands)
        )(
          dir => sys.process.Process(commands, dir)
        ).!(resultCollector)
      ProcessResult.processResult(code, resultCollector)
  }

}