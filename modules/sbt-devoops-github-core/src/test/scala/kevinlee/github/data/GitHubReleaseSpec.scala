package kevinlee.github.data

import cats.syntax.all.*
import hedgehog.*
import hedgehog.runner.*
import io.circe.parser.*

/** @author Kevin Lee
  * @since 2026-08-22
  */
object GitHubReleaseSpec extends Properties {
  override def tests: List[Test] = List(
    property(
      "appendGeneratedReleaseNote returns the generated one when there is no existing release note",
      testAppendGeneratedReleaseNoteWithNoExistingOne,
    ),
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
    example(
      "Response decoder accepts null name, null body, null asset label and null asset uploader",
      testResponseDecoderWithNullableFields,
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

  def testAppendGeneratedReleaseNoteWithNoExistingOne: Property =
    for {
      generated <- genReleaseNote.log("generated")
    } yield {
      val actual = GitHubRelease.appendGeneratedReleaseNote(
        none[GitHubRelease.Description],
        GitHubRelease.Description(generated),
      )
      actual ==== Some(GitHubRelease.Description(generated))
    }

  def testAppendGeneratedReleaseNoteWithEmptyExistingOne: Property =
    for {
      generated <- genReleaseNote.log("generated")
    } yield {
      val actual = GitHubRelease.appendGeneratedReleaseNote(
        GitHubRelease.Description("").some,
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
        GitHubRelease.Description(alreadyAppended).some,
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
        GitHubRelease.Description(existing).some,
        GitHubRelease.Description(generated),
      )
      actual ==== Some(expected)
    }

  /* GitHub's schema declares release `name` / `body` and asset `label` / `uploader` as nullable,
   * so the decoder has to accept an explicit null for each of them.
   */
  def testResponseDecoderWithNullableFields: Result = {
    val json =
      """{
        |  "id": 12345678,
        |  "url": "https://api.github.com/repos/Kevin-Lee/test-project/releases/12345678",
        |  "assets_url": "https://api.github.com/repos/Kevin-Lee/test-project/releases/12345678/assets",
        |  "upload_url": "https://uploads.github.com/repos/Kevin-Lee/test-project/releases/12345678/assets{?name,label}",
        |  "author": {
        |    "id": 1,
        |    "login": "Kevin-Lee",
        |    "url": "https://api.github.com/users/Kevin-Lee",
        |    "name": null,
        |    "avatar_url": null
        |  },
        |  "tag_name": "v0.1.1",
        |  "name": null,
        |  "body": null,
        |  "draft": false,
        |  "prerelease": false,
        |  "created_at": "2026-08-22T12:00:00Z",
        |  "published_at": null,
        |  "assets": [
        |    {
        |      "id": 87654321,
        |      "url": "https://api.github.com/repos/Kevin-Lee/test-project/releases/assets/87654321",
        |      "browser_download_url": "https://github.com/Kevin-Lee/test-project/releases/download/v0.1.1/test.jar",
        |      "name": "test.jar",
        |      "label": null,
        |      "state": "uploaded",
        |      "content_type": "application/zip",
        |      "size": 1024,
        |      "download_count": 0,
        |      "created_at": "2026-08-22T12:00:00Z",
        |      "updated_at": "2026-08-22T12:00:00Z",
        |      "uploader": null
        |    }
        |  ]
        |}""".stripMargin

    decode[GitHubRelease.Response](json) match {
      case Right(response) =>
        Result.all(
          List(
            response.name ==== None,
            response.body ==== None,
            response.publishedAt ==== None,
            response.assets.map(_.label) ==== List(None),
            response.assets.map(_.uploader) ==== List(None),
          )
        )

      case Left(decodingError) =>
        Result
          .failure
          .log(s"Decoding the release JSON with nullable fields failed: ${decodingError.toString}")
    }
  }

}
