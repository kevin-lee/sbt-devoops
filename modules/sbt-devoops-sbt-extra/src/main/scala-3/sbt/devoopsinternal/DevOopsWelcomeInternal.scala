package sbt.devoopsinternal

import sbt.State

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}

import scala.util.control.NonFatal

/** sbt 2 internals required to re-emit `onLoadMessage` when a client attaches to a warm server.
  *
  * ==Why this lives in a subpackage of `sbt`==
  *
  * sbt 2's `sbt` launcher is a thin client (`sbtn`). When it attaches to an already running server
  * no project load happens, so `doLoadProject` -- which is the only thing that prints
  * `onLoadMessage` -- never runs and the welcome banner silently disappears.
  *
  * Re-emitting the message means writing to the attaching client's terminal, and the only way to
  * reach it is `sbt.StandardMain.exchange`, which is `private[sbt]`. That is a hard compile error
  * from `package devoops` ("can only be accessed from package sbt"), not something a `try`/`catch`
  * can rescue. Declaring this object in a subpackage of `sbt` inherits that access. Nothing else in
  * sbt-devoops needs it: the plugin, its setting keys and the sbt 1 twin all stay in `devoops`.
  *
  * ==Why the package is NOT named `sbt.devoops`==
  *
  * `sbt.devoops` would add a `devoops` member to package `sbt`, and every file in this codebase
  * that sits in `package devoops` and does `import sbt.*` would then resolve `devoops` to
  * `sbt.devoops`. That breaks unrelated modules at compile time, e.g.
  * `import devoops.data.{CommonKeys, DevOopsLogLevel}` fails with "value data is not a member of
  * sbt.devoops". `private[sbt]` is granted to any subpackage of `sbt`, so the segment name is free
  * as long as it differs from `devoops`.
  *
  * ==Stability==
  *
  * These internals carry no compatibility guarantee. Every use of them here is wrapped so that a
  * future sbt change degrades to "the banner is not shown" rather than breaking the user's build.
  *
  * This object is sbt 2 only; there is no `scala-2` twin because nothing outside the sbt 2 compat
  * object refers to it.
  *
  * @author Kevin Lee
  * @since 2026-07-20
  */
object DevOopsWelcomeInternal {

  /** The name of the per-channel UI thread sbt 2 runs the shell prompt on, e.g.
    * `sbt-network-1-ui-thread`.
    */
  private val UiThreadPrefix: String = "sbt-"
  private val UiThreadSuffix: String = "-ui-thread"

  /** Bumped on every project load so that a `reload` re-arms the suppression below. */
  private val loadGeneration: AtomicLong = new AtomicLong(0L)

  /** Raised whenever sbt itself has just printed `onLoadMessage` during a load, so that the next
    * prompt render suppresses the duplicate instead of printing it a second time.
    */
  private val suppressOnce: AtomicBoolean = new AtomicBoolean(false)

  /** Channel name -> the load generation already handled for it. This is what turns a
    * once-per-prompt-render hook into a once-per-attach one.
    */
  private val handledByChannel: ConcurrentHashMap[String, java.lang.Long] =
    new ConcurrentHashMap[String, java.lang.Long]()

  /** Identifies the client channel the current prompt is being rendered for.
    *
    * sbt 2 gives every channel its own UI thread named `sbt-<channelName>-ui-thread`, and there is
    * no public way to obtain the channel from inside a `colorShellPrompt` function
    * (`sbt.internal.util.Terminal.get` is `private[sbt]` and `State.currentCommand` is `None` at
    * prompt time). Only the per-channel uniqueness of this string matters, not its exact shape.
    */
  def currentChannelName(): String =
    Thread.currentThread.getName.stripPrefix(UiThreadPrefix).stripSuffix(UiThreadSuffix)

  /** Called from the `onLoad` hook, i.e. exactly when sbt has loaded the project and printed
    * `onLoadMessage` itself.
    */
  def onLoaded(): Unit = {
    val _ = loadGeneration.incrementAndGet()
    suppressOnce.set(true)
  }

  /** Whether the welcome message should be printed for `channel` on this prompt render.
    *
    *   - already handled this generation for this channel -> `false` (prevents printing on every
    *     prompt render within one session)
    *   - sbt has just printed it during a load -> `false`, consuming the flag (prevents the
    *     duplicate on a cold start and on `reload`)
    *   - otherwise -> `true` (a warm attach, where no load happened)
    */
  def shouldShow(channel: String): Boolean = {
    val generation = loadGeneration.get()
    val previous   = Option(handledByChannel.put(channel, java.lang.Long.valueOf(generation)))
    if (previous.exists(_.longValue == generation)) false
    else !suppressOnce.compareAndSet(true, false)
  }

  /** Writes directly to the given channel's terminal.
    *
    * `state.log.info` cannot be used here: at attach time there is no current exec, so
    * `CommandExchange.logMessage` has no owning channel to route to, and calling it from the prompt
    * thread deadlocks sbt. Returning the text as the prompt string does not work either -- the line
    * editor discards it.
    *
    * Returns `false` (and prints nothing) if the channel is gone or sbt's internals have changed.
    */
  def writeTo(channel: String, message: String): Boolean =
    try
      sbt.StandardMain.exchange.channelForName(channel) match {
        case Some(commandChannel) =>
          commandChannel.terminal.printStream.println(message)
          true
        case None =>
          false
      }
    catch {
      case NonFatal(_) => false
    }

  /** Reproduces sbt 2's own `colorShellPrompt` default, so that overriding it to hook the welcome
    * message leaves the prompt itself exactly as it was: a user-supplied `shellPrompt` still wins,
    * and otherwise sbt's stock prompt is used.
    *
    * `shellPromptFromState` lives on `sbt.Classpaths`, not `sbt.Defaults`: `Defaults.scala` declares
    * both objects and this one belongs to the latter.
    */
  def renderPrompt(shellPromptValue: State => String, isColorEnabled: Boolean, state: State): String =
    shellPromptValue match {
      case sbt.internal.ui.UITask.NoShellPrompt => sbt.Classpaths.shellPromptFromState(isColorEnabled)(state)
      case userSuppliedPrompt                   => userSuppliedPrompt(state)
    }

}
