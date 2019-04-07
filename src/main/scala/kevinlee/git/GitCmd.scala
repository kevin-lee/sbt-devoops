package kevinlee.git

import kevinlee.git.Git.{BranchName, Description, Repository, TagName}

/**
  * @author Kevin Lee
  * @since 2019-03-17
  */
sealed trait GitCmd

object GitCmd {
  final case object CurrentBranchName extends GitCmd
  final case class Checkout(branchName: BranchName) extends GitCmd
  final case object FetchTags extends GitCmd
  final case class Tag(tagName: TagName) extends GitCmd
  final case class TagWithDescription(tagName: TagName, description: Description) extends GitCmd
  final case class Push(repository: Repository, tagName: TagName) extends GitCmd
  final case class RemoteGetUrl(repository: Repository) extends GitCmd

  def currentBranchName: GitCmd = CurrentBranchName

  def checkout(branchName: BranchName): GitCmd = Checkout(branchName)

  def fetchTags: GitCmd = FetchTags

  def tag(tagName: TagName): GitCmd = Tag(tagName)

  def tagWithDescription(tagName: TagName, description: Description): GitCmd =
    TagWithDescription(tagName, description)

  def push(repository: Repository, tagName: TagName): GitCmd =
    Push(repository, tagName)

  def remoteGetUrl(repository: Repository): GitCmd =
    RemoteGetUrl(repository)

  def cmdAndArgs(gitCmd: GitCmd): List[String] = gitCmd match {
    case CurrentBranchName =>
      List("rev-parse", "--abbrev-ref", "HEAD")

    case Checkout(branchName) =>
      List("checkout", branchName.value)

    case FetchTags =>
      List("fetch", "--tags")

    case Tag(tagName) =>
      List("tag", tagName.value)

    case TagWithDescription(tagName, description) =>
      List("tag", "-a", tagName.value, "-m", description.value)

    case Push(repository, tagName) =>
      List("push", repository.value, tagName.value)

    case RemoteGetUrl(repository) =>
      List("remote", "get-url", repository.value)
  }

  def render(gitCmd: GitCmd): String = s"git ${cmdAndArgs(gitCmd).mkString(" ")}"

}
