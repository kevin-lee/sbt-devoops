package kevinlee.github.data

import hedgehog.*
import hedgehog.runner.*

/** @author Kevin Lee
  * @since 2026-08-22
  */
object GitHubReleaseSpec extends Properties {
  override def tests: List[Test] = List(
    property(
      "appendGeneratedReleaseNote returns the generated one when the existing release note is empty",
      testAppendGeneratedReleaseNoteWithEmptyExistingOne,
    ),
    property(
      "appendGeneratedReleaseNote returns None when the existing release note already has the generated one",
      testAppendGeneratedReleaseNoteWithDuplicate,
    ),
    property(
      "appendGeneratedReleaseNote appends the generated one after the separator",
      testAppendGeneratedReleaseNote,
    ),
  )

  private def genReleaseNote: Gen[String] =
    Gen
      .string(
        Gen.frequency1(
          90 -> Gen.alphaNum,
          5  -> Gen.constant(' '),
          5  -> Gen.constant('\n'),
        ),
        Range.linear(1, 30),
      )

  def testAppendGeneratedReleaseNoteWithEmptyExistingOne: Property =
    for {
      generated <- genReleaseNote.log("generated")
    } yield {
      val actual = GitHubRelease.appendGeneratedReleaseNote(
        GitHubRelease.Description(""),
        GitHubRelease.Description(generated),
      )
      actual ==== Some(GitHubRelease.Description(generated))
    }

  def testAppendGeneratedReleaseNoteWithDuplicate: Property =
    for {
      existing  <- genReleaseNote.log("existing")
      generated <- genReleaseNote.log("generated")
    } yield {
      val alreadyAppended = s"$existing\n${GitHubRelease.ReleaseNoteSeparator}\n$generated"
      val actual          = GitHubRelease.appendGeneratedReleaseNote(
        GitHubRelease.Description(alreadyAppended),
        GitHubRelease.Description(generated),
      )
      actual ==== None
    }

  def testAppendGeneratedReleaseNote: Property =
    for {
      existing  <- genReleaseNote.log("existing")
      /* genReleaseNote never produces '-' so the generated one can never be a substring of the existing one. */
      generated <- genReleaseNote
                     .map(releaseNote => s"$releaseNote-generated")
                     .log("generated")
    } yield {
      val expected = GitHubRelease.Description(
        s"$existing\n${GitHubRelease.ReleaseNoteSeparator}\n$generated"
      )
      val actual   = GitHubRelease.appendGeneratedReleaseNote(
        GitHubRelease.Description(existing),
        GitHubRelease.Description(generated),
      )
      actual ==== Some(expected)
    }

}
