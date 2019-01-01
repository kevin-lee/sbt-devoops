package io.kevinlee.git

import java.io.File

/**
  * @author Kevin Lee
  * @since 2019-01-01
  */
object Git {

  final case class BranchName(value: String) extends AnyVal
  final case class TagName(name: String) extends AnyVal

  final case class Description(value: String) extends AnyVal

  def fromProcessResultToEither(
    successHandler: List[String] => GitCommandResult
  , errorHandler: (Int, String) => GitCommandError
  ): PartialFunction[ProcessResult, Either[GitCommandError, GitCommandResult]] = {
      case ProcessResult.Success(outputs) =>
        Right(successHandler(outputs))

      case ProcessResult.Failure(code, error) =>
        Left(errorHandler(code, error))
    }

  def checkout(branchName: BranchName, baseDir: File): Either[GitCommandError, GitCommandResult] =
    ProcessResult.toEither(
      SysProcess.run(
        SysProcess.process(Some(baseDir), "git", "checkout", branchName.value)
      )
    )(fromProcessResultToEither(
      r => GitCommandResult.gitCheckoutResult(r.mkString("\n"))
    , (code, err) => GitCommandError.gitCheckoutError(code, err)
    ))

  def tag(tagName: TagName, baseDir: File): Either[GitCommandError, GitCommandResult] =
    ProcessResult.toEither(
      SysProcess.run(
        SysProcess.process(Some(baseDir), "git", "tag", tagName.name)
      )
    )(fromProcessResultToEither(
      r => GitCommandResult.gitTagResult(r.mkString("\n"))
    , (code, err) => GitCommandError.gitTagError(code, err)
    ))

  def tagWithDescription(tagName: TagName, description: Description, baseDir: File): Either[GitCommandError, GitCommandResult] =
    ProcessResult.toEither(
      SysProcess.run(SysProcess.process(Some(baseDir), "git", "tag", "-a", tagName.name, "-m", description.value))
    )(fromProcessResultToEither(
      r => GitCommandResult.gitTagResult(r.mkString("\n"))
    , (code, err) => GitCommandError.gitTagError(code, err)
    ))


}