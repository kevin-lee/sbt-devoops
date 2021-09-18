package kevinlee.github.data

import cats.effect.IO
import cats.syntax.all._
import hedgehog._
import hedgehog.runner._

/** @author Kevin Lee
  * @since 2021-09-19
  */
object GitHubSpec extends Properties {
  override def tests: List[Test] = List(
    property(
      "test findGitHubRepoOrgAndName with GitHubHttps",
      testFindGitHubRepoOrgAndNameWithGitHubHttps,
    ),
    property(
      "test findGitHubRepoOrgAndName with GitHubGit",
      testFindGitHubRepoOrgAndNameWithGitHubGit,
    ),
    property(
      "test findGitHubRepoOrgAndName with GitHubSsh",
      testFindGitHubRepoOrgAndNameWithGitHubSsh,
    ),
  )

  private def genName: Gen[String] = Gen.string(
    Gen.frequency1(
      96 -> Gen.alphaNum,
      2  -> Gen.constant('-'),
      2  -> Gen.constant('_'),
    ),
    Range.linear(4, 20),
  )

  def testFindGitHubRepoOrgAndNameWithGitHubHttps: Property = for {
    org  <- genName.log("org")
    name <- genName.log("name")
  } yield {
    val remoteRepo = s"https://github.com/$org/$name.git"
    val expected   = GitHub.Repo(GitHub.Repo.Org(org), GitHub.Repo.Name(name)).some
    val actual     = GitHub
      .findGitHubRepoOrgAndName[IO](remoteRepo)
      .unsafeRunSync()
    actual ==== expected
  }

  def testFindGitHubRepoOrgAndNameWithGitHubGit: Property = for {
    org  <- genName.log("org")
    name <- genName.log("name")
  } yield {
    val remoteRepo = s"git://github.com:$org/$name.git"
    val expected   = GitHub.Repo(GitHub.Repo.Org(org), GitHub.Repo.Name(name)).some
    val actual     = GitHub
      .findGitHubRepoOrgAndName[IO](remoteRepo)
      .unsafeRunSync()
    actual ==== expected
  }

  def testFindGitHubRepoOrgAndNameWithGitHubSsh: Property = for {
    org  <- genName.log("org")
    name <- genName.log("name")
  } yield {
    val remoteRepo = s"git@github.com:$org/$name.git"
    val expected   = GitHub.Repo(GitHub.Repo.Org(org), GitHub.Repo.Name(name)).some
    val actual     = GitHub
      .findGitHubRepoOrgAndName[IO](remoteRepo)
      .unsafeRunSync()
    actual ==== expected
  }

}
