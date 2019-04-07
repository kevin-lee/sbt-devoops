package kevinlee.git

/**
  * @author Kevin Lee
  * @since 2019-03-31
  */
final case class GitCmdAndResult(gitCmd: GitCmd, gitCommandResult: GitCommandResult)

object GitCmdAndResult {
  def render(gitCmdAndResult: GitCmdAndResult): String =
    s"${GitCmd.render(gitCmdAndResult.gitCmd)}${GitCommandResult.render(gitCmdAndResult.gitCommandResult)}"
}
