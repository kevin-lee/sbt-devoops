---
sidebar_position: 1
id: getting-started
title: Getting Started
sidebar_label: "Getting Started"
slug: /
---
import useBaseUrl from '@docusaurus/useBaseUrl';

### <img src='/img/sbt-devoops-logo-64x64.png' /> sbt DevOops

[![Build Status](https://github.com/Kevin-Lee/sbt-devoops/workflows/Build%20All/badge.svg)](https://github.com/Kevin-Lee/sbt-devoops/actions?workflow=Build+All)
[![Release Status](https://github.com/Kevin-Lee/sbt-devoops/workflows/Release/badge.svg)](https://github.com/Kevin-Lee/sbt-devoops/actions?workflow=Release)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.kevinlee/sbt-devoops/badge.svg)](https://search.maven.org/artifact/io.kevinlee/sbt-devoops)
[![Latest version](https://index.scala-lang.org/kevin-lee/sbt-devoops/sbt-devoops/latest.svg)](https://index.scala-lang.org/kevin-lee/sbt-devoops/sbt-devoops)

SBT Plugin to help release artifacts and changelogs

## Get sbt-devoops

:::tip Note

Documentation for `3.3.1` is a work in progress.

:::

In the `project/plugins.sbt`, add the following line,
```scala
addSbtPlugin("io.kevinlee" % "sbt-devoops-scala"     % "3.3.1")
addSbtPlugin("io.kevinlee" % "sbt-devoops-sbt-extra" % "3.3.1")
addSbtPlugin("io.kevinlee" % "sbt-devoops-starter"   % "3.3.1")
```

### DevOopsGitHubReleasePlugin

To use `DevOopsGitHubReleasePlugin`, add the following line to `build.sbt`.
```scala
enablePlugins(DevOopsGitHubReleasePlugin)
```

For more about how to set up and use, please check out the next pages [DevOopsGitHubReleasePlugin - Config and Run](gh-release-plugin/config-and-run).
