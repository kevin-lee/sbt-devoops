---
id: getting-started
title: Getting Started
sidebar_label: Getting Started
slug: /
---
import useBaseUrl from '@docusaurus/useBaseUrl';

### <img src='/img/sbt-devoops-logo-64x64.png' /> sbt DevOops

[![Build Status](https://github.com/Kevin-Lee/sbt-devoops/workflows/Build%20All/badge.svg)](https://github.com/Kevin-Lee/sbt-devoops/actions?workflow=Build+All)
[![Release Status](https://github.com/Kevin-Lee/sbt-devoops/workflows/Release/badge.svg)](https://github.com/Kevin-Lee/sbt-devoops/actions?workflow=Release)
[ ![Download](https://api.bintray.com/packages/kevinlee/sbt-plugins/sbt-devoops/images/download.svg) ](https://bintray.com/kevinlee/sbt-plugins/sbt-devoops/_latestVersion)
[![Latest version](https://index.scala-lang.org/kevin-lee/sbt-devoops/sbt-devoops/latest.svg)](https://index.scala-lang.org/kevin-lee/sbt-devoops/sbt-devoops)

SBT Plugin to help release artifacts and changelogs

## Get sbt-devoops
In the `project/plugins.sbt`, add the following line,
```scala
addSbtPlugin("io.kevinlee" % "sbt-devoops" % "1.0.3")
```

### DevOopsGitReleasePlugin

To use `DevOopsGitReleasePlugin`, add the following line to `build.sbt`.
```scala
enablePlugins(DevOopsGitReleasePlugin)
```

For more about how to set up and use, please check out the next pages [DevOopsGitReleasePlugin - Config and Run](gh-release-plugin/config-and-run).
