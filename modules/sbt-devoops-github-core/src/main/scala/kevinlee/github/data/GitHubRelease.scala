package kevinlee.github.data

import cats.syntax.all.*

/** @author Kevin Lee
  * @since 2021-01-16
  */
object GitHubRelease extends GitHubReleaseBase {

  /** The Markdown thematic break put between an existing release note and the appended generated one. */
  val ReleaseNoteSeparator: String = "***"

  /** The existing release note with the generated one appended after [[ReleaseNoteSeparator]].
    *
    *   - If the existing release note is empty, the generated one becomes the whole release note.
    *   - If the existing release note already contains the generated one, it returns `None` so that the caller can
    *     skip updating the release and avoid a duplicate.
    */
  def appendGeneratedReleaseNote(
    existingReleaseNote: Description,
    generatedReleaseNote: Description,
  ): Option[Description] = {
    val existing  = existingReleaseNote.description
    val generated = generatedReleaseNote.description
    if (existing.isEmpty)
      Description(generated).some
    else if (existing.contains(generated))
      none[Description]
    else
      Description(s"$existing\n$ReleaseNoteSeparator\n$generated").some
  }

}
