package devoops

import sbt.*
import sbt.Keys.*
import sbt.devoopsinternal.DevOopsWelcomeInternal

import scala.util.control.NonFatal

/** sbt 2 implementation of "always show the welcome message on an interactive start".
  *
  * sbt 2's `sbt` launcher is a thin client. When it attaches to an already running server the
  * project is not loaded, so sbt never prints `onLoadMessage` and the welcome banner configured by
  * `sbt-devoops-starter` (logo + useful tasks) disappears. It reappears only after `sbt shutdown`,
  * which is what makes it look intermittent.
  *
  * `onLoad` is applied inside `Project.setProject`, the very same gate as `onLoadMessage`, so it
  * cannot be used to detect an attach. The only surface that runs on a warm attach is
  * `colorShellPrompt`, which is why the message is emitted from there, guarded down to once per
  * attach by [[sbt.devoopsinternal.DevOopsWelcomeInternal]].
  *
  * See the scala-2 twin of this object, which is a no-op: sbt 1 has no thin client, so it always
  * loads the project and always prints the message.
  *
  * @author Kevin Lee
  * @since 2026-07-20
  */
private[devoops] object DevOopsSbtExtraCompat {

  /** `colorShellPrompt` is called on every prompt render, and `onLoad` only on an actual project
    * load. Together they give "print exactly once per interactive start": the `onLoad` hook records
    * that sbt has just printed the message itself, and the prompt hook prints it only when it finds
    * that no load did.
    *
    * Both settings are installed unconditionally and do nothing when `flag` is `false`, so the
    * shell prompt is untouched for anyone who has not opted in.
    */
  def alwaysShowOnLoadMessageSettings(flag: SettingKey[Boolean]): Seq[Def.Setting[_]] = Seq(
    Global / onLoad := {
      val enabled  = (Global / flag).value
      val previous = (Global / onLoad).value
      if enabled then {
        previous.andThen { loadedState =>
          DevOopsWelcomeInternal.onLoaded()
          loadedState
        }
      } else {
        previous
      }
    },
    Global / colorShellPrompt := {
      val enabled          = (Global / flag).value
      val shellPromptValue = shellPrompt.value
      (isColorEnabled: Boolean, state: State) => {
        if enabled then showWelcomeMessageIfNeeded(state) else ()
        DevOopsWelcomeInternal.renderPrompt(shellPromptValue, isColorEnabled, state)
      }
    },
  )

  private def showWelcomeMessageIfNeeded(state: State): Unit = {
    val channel = DevOopsWelcomeInternal.currentChannelName()
    if DevOopsWelcomeInternal.shouldShow(channel) then {
      onLoadMessageOf(state).foreach { message =>
        val _ = DevOopsWelcomeInternal.writeTo(channel, message)
      }
    } else {
      ()
    }
  }

  /** Reads whatever `onLoadMessage` the build configured, so the message shown on a warm attach is
    * exactly the one sbt would have shown on a cold start.
    */
  private def onLoadMessageOf(state: State): Option[String] =
    try Project.extract(state).getOpt(Keys.onLoadMessage).filter(_.nonEmpty)
    catch { case NonFatal(_) => None }

}
