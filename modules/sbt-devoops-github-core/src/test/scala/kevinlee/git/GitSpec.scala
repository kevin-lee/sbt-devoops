package kevinlee.git

import cats.Id
import effectie.instances.id.fx.*
import hedgehog.*
import hedgehog.runner.*
import kevinlee.git.Git.{BranchName, Description, Repository, TagName}

/** @author Kevin Lee
  * @since 2026-09-19
  */
object GitSpec extends Properties {
  override def tests: List[Test] = List(
    property(
      "test Git.updateHistory with Right records the command and its result in the history",
      testUpdateHistoryRight,
    ),
    property(
      "test Git.updateHistory with Left records nothing in the history",
      testUpdateHistoryLeft,
    ),
  )

  private val git: Git[Id] = Git.gitF[Id]

  private def genNonEmptyString: Gen[String] =
    Gen.string(Gen.alphaNum, Range.linear(1, 20))

  private def genGitCmd: Gen[GitCmd] =
    Gen.choice1(
      Gen.constant(GitCmd.currentBranchName),
      genNonEmptyString.map(name => GitCmd.checkout(BranchName(name))),
      Gen.constant(GitCmd.fetchTags),
      Gen.constant(GitCmd.getTag),
      genNonEmptyString.map(name => GitCmd.tag(TagName(name))),
      for {
        name <- genNonEmptyString
        desc <- genNonEmptyString
      } yield GitCmd.tagWithDescription(TagName(name), Description(desc)),
      for {
        repo <- genNonEmptyString
        name <- genNonEmptyString
      } yield GitCmd.push(Repository(repo), TagName(name)),
      genNonEmptyString.map(repo => GitCmd.remoteGetUrl(Repository(repo))),
    )

  private def genOutputs: Gen[List[String]] =
    Gen.string(Gen.alphaNum, Range.linear(0, 20)).list(Range.linear(0, 10))

  private def genGitCommandResult: Gen[GitCommandResult] =
    genOutputs.map(GitCommandResult.genericResult)

  private def genGitCommandError(gitCmd: GitCmd): Gen[GitCommandError] =
    for {
      code   <- Gen.int(Range.linear(1, 255))
      errors <- genOutputs
    } yield GitCommandError.genericGitCommandResultError(gitCmd, code, errors)

  def testUpdateHistoryRight: Property =
    for {
      gitCmd    <- genGitCmd.log("gitCmd")
      cmdResult <- genGitCommandResult.log("cmdResult")
      a         <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("a")
    } yield {
      val input: Id[Either[GitCommandError, (GitCommandResult, Int)]] = Right((cmdResult, a))

      val expected: (Git.CmdHistory, Either[GitCommandError, Int]) =
        (List(GitCmdAndResult(gitCmd, cmdResult)), Right(a))

      val actual = git.updateHistory[Int](gitCmd, input).value.run

      actual ==== expected
    }

  def testUpdateHistoryLeft: Property =
    for {
      gitCmd <- genGitCmd.log("gitCmd")
      error  <- genGitCommandError(gitCmd).log("error")
    } yield {
      val input: Id[Either[GitCommandError, (GitCommandResult, Int)]] = Left(error)

      val expected: (Git.CmdHistory, Either[GitCommandError, Int]) =
        (List.empty[GitCmdAndResult], Left(error))

      val actual = git.updateHistory[Int](gitCmd, input).value.run

      actual ==== expected
    }
}
