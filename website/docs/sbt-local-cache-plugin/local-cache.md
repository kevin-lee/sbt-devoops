---
id: local-cache
title: "DevOopsSbtLocalCachePlugin - Build-Local Cache (sbt 2)"
sidebar_label: "Build-Local Cache (sbt 2)"
---

## Why?

sbt 2 caches the result of every task (e.g. `compile`, `test`) in a **machine-wide** disk cache
(e.g. `~/Library/Caches/sbt/v2` on macOS). The cache is content-addressed and always on, and it is
shared with **all the other builds on the machine**.

`clean` only removes `target`, so it does not remove anything from that cache. In other words,

```sbt
sbt "clean; compile"
```

is still a cache hit, and the compiled result is simply restored from the machine-wide cache.

sbt itself offers only all-or-nothing solutions.

* `cleanFull` removes the **entire machine-wide** cache, which affects every other build on the machine.
* `Global / cacheVersion` invalidates the cache, but it never removes the old cached data, so the
  cache keeps growing.

`DevOopsSbtExtraPlugin` offers a middle ground: make the cache **build-local**, so that it can be
removed for this build only, without affecting any other project on the machine.

:::note
This is an **sbt 2 only** feature. On sbt 1, which has no machine-wide task cache, all the settings,
the task and the command below do nothing.
:::

## How to Use

Set `devOopsUseLocalCache` to `true` at the `Global` scope in `build.sbt` to opt in.

```sbt
Global / devOopsUseLocalCache := true
```

It redirects sbt 2's cache to `devOopsBuildLocalCacheDirectory` which is `.sbt-local-cache` in the
build root by default. Once it is done, the build no longer shares the machine-wide cache, and the
cache can be removed with

```sbt
sbt devOopsCleanLocalCache
```

which removes the build-local cache directory (as well as `target/out`), so the next `compile` or
`test` actually runs again.

It is recommended to add the cache directory to `.gitignore`.

```gitignore
.sbt-local-cache/
```

## Settings, Task and Command

| Name | Type | Default | Description |
|---|---|---|---|
| `devOopsUseLocalCache` | `SettingKey[Boolean]` | `false` | Set to `true` at the `Global` scope to make sbt 2's cache build-local. |
| `devOopsBuildLocalCacheDirectory` | `SettingKey[File]` | `<build root>/.sbt-local-cache` | The build-local cache directory. |
| `devOopsCleanIncludesLocalCache` | `SettingKey[Boolean]` | `false` | If `true`, `clean` also removes the build-local cache. |
| `devOopsLocalCacheSize` | `TaskKey[Long]` | | Show the location and the total size of the sbt local cache in use, and return the size in bytes. |
| `devOopsCleanLocalCache` | Command | | Remove the build-local cache. |

### `devOopsBuildLocalCacheDirectory`

To use a different directory,

```sbt
Global / devOopsUseLocalCache := true

Global / devOopsBuildLocalCacheDirectory := (ThisBuild / baseDirectory).value / ".cache" / "sbt"
```

:::caution
`devOopsCleanLocalCache` and `devOopsCleanIncludesLocalCache` only remove the cache when it is
**inside the build**. If the cache directory is outside the build (e.g. the machine-wide cache
because `devOopsUseLocalCache` is not set to `true`), they log a warning and do nothing, since
removing it would affect all the other builds on the machine. Use sbt's built-in `cleanFull` if the
machine-wide cache should really be removed.
:::

### `devOopsCleanIncludesLocalCache`

By default, `clean` does not remove the build-local cache, so the cache hit is still available after
`clean`. To make `clean` remove the build-local cache as well, set
`devOopsCleanIncludesLocalCache` to `true`.

```sbt
Global / devOopsUseLocalCache := true

Global / devOopsCleanIncludesLocalCache := true

lazy val root = (project in file("."))
  .settings(
    name := "my-project"
  )
```

With this, `sbt clean` removes both `target` and the build-local cache, so the following `compile`
or `test` runs again instead of being restored from the cache.

:::note
`clean` is also a cached task in sbt 2, so the result of `clean` itself is cached again right after
it has removed the build-local cache. It means a couple of small entries for `clean` may remain in
the cache directory. It is harmless since the cached content of the actual work (e.g. `compile`,
`test`) is gone, and the next `compile` or `test` runs again.
:::

### `devOopsLocalCacheSize`

```sbt
sbt devOopsLocalCacheSize
```

```
[info] sbt local cache: /path/to/project/.sbt-local-cache (123.4 MiB)
```

## Running Tests Again

If the goal is only to run tests again regardless of the cache, sbt 2 already has `testFull` which
is never cached.

```sbt
sbt testFull
```
