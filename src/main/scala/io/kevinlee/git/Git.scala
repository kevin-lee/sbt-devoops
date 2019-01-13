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
  final case class TagName(value: String) extends AnyVal
  final case class Repository(value: String) extends AnyVal

  final case class Description(value: String) extends AnyVal

  def fromProcessResultToEither(
    successHandler: List[String] => GitCommandResult
  , errorHandler: (Int, List[String]) => GitCommandError
  ): PartialFunction[ProcessResult, Either[GitCommandError, GitCommandResult]] = {
      case ProcessResult.Success(outputs) =>
        Right(successHandler(outputs))

      case ProcessResult.Failure(code, errors) =>
        Left(errorHandler(code, errors))
    }

  def git(baseDir: File, commandAndArgs: List[String]): ProcessResult =
    SysProcess.run(
      SysProcess.process(Some(baseDir), "git" :: commandAndArgs)
    )

  def git1(baseDir: File, command: String, args: String*): ProcessResult =
    git(baseDir, command :: args.toList)

  def currentBranchName(baseDir: File): Either[GitCommandError, GitCommandResult] = {
    val gitArgs = List("rev-parse", "--abbrev-ref", "HEAD")
    ProcessResult.toEither(
      git(baseDir, gitArgs)
    )(fromProcessResultToEither(
      r => GitCommandResult.gitCurrentBranchName(BranchName(r.mkString.trim), gitArgs)
    , (code, errs) => GitCommandError.gitCurrentBranchError(code, errs)
    ))
  }

  def checkIfCurrentBranchIsSame(
    branchName: BranchName
  , baseDir: File
  ): Either[GitCommandError, Vector[GitCommandResult]] = {
    def isSameCurrent(
      branchName: BranchName, currentBranchResult: GitCommandResult
    ): Either[GitCommandError, GitCommandResult] =
      currentBranchResult match {
        case g@GitCurrentBranchName(BranchName(currentBranchName), _) =>
          if (currentBranchName === branchName.value)
            Right(GitCommandResult.gitSameCurrentBranch(BranchName(currentBranchName)))
          else
            Left(GitCommandError.gitUnexpectedCommandResultError(
              g
              , s"current branch == given expected branch. expected: ${branchName.value}"
            ))
        case other =>
          Left(
            GitCommandError.gitUnexpectedCommandResultError(other, "GitCurrentBranchName")
          )
      }

    for {
      currentBranchResult <- currentBranchName(baseDir).right
      r <- isSameCurrent(branchName, currentBranchResult).right
    } yield Vector(currentBranchResult, r)
  }


  def checkout(branchName: BranchName, baseDir: File): Either[GitCommandError, GitCommandResult] =
    ProcessResult.toEither(
      git1(baseDir, "checkout", branchName.value)
    )(fromProcessResultToEither(
      _ => GitCommandResult.gitCheckoutResult(branchName)
    , (code, err) => GitCommandError.gitCheckoutError(code, err)
    ))

  def fetchTags(baseDir: File): Either[GitCommandError, GitCommandResult] = {
    val tags = "--tags"
    ProcessResult.toEither(
      git1(baseDir, "fetch", tags)
    )(
      fromProcessResultToEither(
        _ => GitCommandResult.gitFetchResult(Some(tags))
        , (code, err) => GitCommandError.gitFetchError(code, err, Some(tags))
      )
    )
  }

  def tag(tagName: TagName, baseDir: File): Either[GitCommandError, GitCommandResult] =
    ProcessResult.toEither(
      git1(baseDir, "tag", tagName.value)
    )(fromProcessResultToEither(
      _ => GitCommandResult.gitTagResult(tagName)
    , (code, err) => GitCommandError.gitTagError(code, err)
    ))

  def tagWithDescription(tagName: TagName, description: Description, baseDir: File): Either[GitCommandError, GitCommandResult] =
    ProcessResult.toEither(
      git1(baseDir, "tag", "-a", tagName.value, "-m", description.value)
    )(fromProcessResultToEither(
      _ => GitCommandResult.gitTagResult(tagName)
    , (code, err) => GitCommandError.gitTagError(code, err)
    ))

  def pushTag(repository: Repository, tagName: TagName, baseDir:File): Either[GitCommandError, GitCommandResult] = ProcessResult.toEither(
    git1(baseDir, "push", repository.value, tagName.value)
  )(fromProcessResultToEither(
    result => GitCommandResult.gitPushTagResult(repository, tagName, result)
  , (code, err) => GitCommandError.gitPushTagError(code, err, repository, tagName)
  ))

}