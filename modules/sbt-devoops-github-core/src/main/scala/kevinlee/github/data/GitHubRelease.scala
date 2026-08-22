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
    *   - If there is no existing release note, or it is empty, the generated one becomes the whole release note.
    *     GitHub's release `body` is nullable, so absent and empty are treated the same way.
    *   - If the existing release note already contains the generated one, it returns `None` so that the caller can
    *     skip updating the release and avoid a duplicate.
    */
  def appendGeneratedReleaseNote(
    existingReleaseNote: Option[Description],
    generatedReleaseNote: Description,
  ): Option[Description] = {
    val generated = generatedReleaseNote.description
    existingReleaseNote.map(_.description).filter(_.nonEmpty) match {
      case None =>
        Description(generated).some

      case Some(existing) =>
        if (existing.contains(generated))
          none[Description]
        else
          Description(s"$existing\n$ReleaseNoteSeparator\n$generated").some
    }
  }

}
