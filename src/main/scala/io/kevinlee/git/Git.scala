package io.kevinlee.git

import java.io.File

import io.kevinlee.CommonPredef._
import io.kevinlee.git.GitCommandResult.GitCurrentBranchName

/**
  * @author Kevin Lee
  * @since 2019-01-01
  */
object Git {
  // $COVERAGE-OFF$

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

  def currentBranchName(baseDir: File): Either[GitCommandError, GitCommandResult] =
    ProcessResult.toEither(
      SysProcess.run(
        SysProcess.process(Some(baseDir), "git", "rev-parse", "--abbrev-ref", "HEAD")
      )
    )(fromProcessResultToEither(
      r => GitCommandResult.gitCurrentBranchName(BranchName(r.mkString.trim))
    , (code, err) => GitCommandError.gitCurrentBranchError(code, err)
    ))

  def doIfCurrentBranchMatches(
    branchName: BranchName
  , baseDir: File
  )(f: => Either[GitCommandError, GitCommandResult]): Either[GitCommandError, GitCommandResult] =
    currentBranchName(baseDir).right.flatMap {
      case g @ GitCurrentBranchName(BranchName(currentBranchName)) =>
        if (currentBranchName === branchName.value)
          f
        else
          Left(
            GitCommandError.gitUnexpectedCommandResultError(
              g
            , s"The current branch should be the same as the given expected one. expected: ${branchName.value} / current: $currentBranchName"
            )
          )
      case other =>
        Left(
          GitCommandError.gitUnexpectedCommandResultError(other, "GitCurrentBranchName")
        )
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