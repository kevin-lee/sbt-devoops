---
id: how-to-use
title: DevOopsJavaPlugin - How to Use
sidebar_label: How to Use
---

## DevOopsJavaPlugin
To use sbt-devoops for a Java project, add the following line to `build.sbt`
```sbt
enablePlugins(DevOopsJavaPlugin)
```

It has
- `javaVersion` setting
- set `crossPaths` and `autoScalaLibrary` to false
- default `javacOptions`: `-source` and `-target` set to the `javaVersion` above, `-Xlint:unchecked`, and set `-encoding` to `UTF-8` 

```scala
  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    javaVersion := "1.8", // 1.8 is default if not specified otherwise.
    crossPaths := false,
    autoScalaLibrary := false,
    javacOptions ++= Seq(
      "-source", javaVersion.value,
      "-target", javaVersion.value,
      "-Xlint:unchecked",
      "-encoding", "UTF-8"
    )
  )
```
