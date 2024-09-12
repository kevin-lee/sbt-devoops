---
id: how-to-use
title: "DevOopsScalaPlugin - How to Use"
sidebar_label: "How to Use"
---

## DevOopsScalaPlugin
This plugin is for a Scala project and is enabled by default.

It includes the following compiler plugins and sbt plugins.

* `sbt-tpolecat`
* `kind-projector`
* `better-monadic-for` (only for Scala 2)
* `paradise` (for Scala &lt;= 2.12)


As of `3.0.0`, `DevOopsScalaPlugin` no longer has its own `scalacOptions`, and it uses `sbt-tpolecat` to manage `scalacOptions`.
