---
id: always-show-on-load-message
title: "DevOopsSbtExtraPlugin - Always Show the Welcome Message (sbt 2)"
sidebar_label: "Always Show Welcome Message (sbt 2)"
---

## Why?

On sbt 2, the welcome message set through `onLoadMessage` (for example the `sbt-welcome` banner that
`DevOopsStarterPlugin` configures with the logo and the list of useful tasks) often does not appear
when you start `sbt`. It shows up once, then seems to disappear for every later run.

That is not a bug in the banner. sbt 2's `sbt` launcher is a **thin client** (`sbtn`). When an sbt
server is already running for the build, the client simply **attaches** to it instead of loading the
project again:

```
sbt shutdown
sbt            # cold start: the project is loaded, the welcome message is printed
exit
sbt            # attaches to the running server: no load, so no welcome message
```

`onLoadMessage` is only printed while the project is being loaded, so on a warm attach it is never
printed at all. sbt's own `welcome to sbt ...` line disappears in exactly the same situations, for
exactly the same reason.

`DevOopsSbtExtraPlugin` can re-emit the welcome message when a client attaches to an already running
server, so it appears on every interactive start.

:::note
This is an **sbt 2 only** feature. On sbt 1 the setting exists but does nothing, because sbt 1 has no
thin client and therefore always loads the project and always prints the message.
:::

## How to Use

Set `devOopsAlwaysShowOnLoadMessage` to `true` at the `Global` scope in `build.sbt` to opt in.

```sbt
Global / devOopsAlwaysShowOnLoadMessage := true
```

Whatever `onLoadMessage` the build has configured is what gets shown, so the banner looks exactly the
same as it does on a cold start. It is printed **once per interactive start** and is never printed
twice: on a cold start or after a `reload`, sbt has already printed it itself, and the plugin
suppresses the duplicate.

## Setting

| Setting                            | Type      | Default | Description                                                                                     |
|:-----------------------------------|:----------|:--------|:------------------------------------------------------------------------------------------------|
| `devOopsAlwaysShowOnLoadMessage`   | `Boolean` | `false` | sbt 2 only. Show `onLoadMessage` on every interactive start, including a warm thin-client attach. |

The default is `false`, so sbt 2's stock behaviour is unchanged unless you opt in.

## Limitations

* **Interactive mode only.** Batch invocations such as `sbt -batch compile` or `sbt compile` that run
  a command and exit do not render a shell prompt, so no message is shown. The message is a greeting
  for an interactive session.
* **sbt's own `welcome to sbt ...` line is not restored.** That line is produced inside sbt itself and
  is out of scope here; only the `onLoadMessage` banner is re-emitted.
* This feature relies on sbt 2 internals that carry no compatibility guarantee. Everything is written
  so that a future sbt change degrades to "the banner is not shown" rather than breaking the build.

## Alternative Without This Setting

If you would rather not enable the setting, running sbt in the foreground instead of through the thin
client also loads the project every time, so the welcome message always appears:

```sbt
sbt --server
```

The trade-off is that you lose the fast startup that the warm server gives you.
