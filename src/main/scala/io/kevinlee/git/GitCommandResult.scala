package io.kevinlee.git

import io.kevinlee.git.Git.{BranchName, TagName}

sealed trait GitCommandResult

/**
  * @author Kevin Lee
  * @since 2019-01-01
  */
object GitCommandResult {
  // $COVERAGE-OFF$

  final case class GitCurrentBranchName(name: BranchName) extends GitCommandResult
  final case class GitCheckoutResult(name: BranchName) extends GitCommandResult
  final case class GitFetchResult(arg: Option[String]) extends GitCommandResult
  final case class GitTagResult(tagName: TagName) extends GitCommandResult

  def gitCurrentBranchName(name: BranchName): GitCommandResult =
    GitCurrentBranchName(name)

  def gitCheckoutResult(name: BranchName): GitCommandResult =
    GitCheckoutResult(name)

  def gitFetchResult(arg: Option[String]): GitCommandResult =
    GitFetchResult(arg)

  def gitTagResult(tagName: TagName): GitCommandResult =
    GitTagResult(tagName)

  def render(gitCommandResult: GitCommandResult): String = gitCommandResult match {

    case GitCurrentBranchName(BranchName(branchName)) =>
      s"git current branch name: $branchName"

    case GitCheckoutResult(BranchName(branchName)) =>
      s"git checkout $branchName"

    case GitFetchResult(arg) =>
      s"git fetch${arg.fold("")(a => s" $a")}"

    case GitTagResult(TagName(tagName)) =>
      s"git tag $tagName"

  }

  // $COVERAGE-ON$
}