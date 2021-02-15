---
id: how-to-use
title: DevOopsScalaPlugin - How to Use
sidebar_label: How to Use
---

## DevOopsScalaPlugin
This plugin is for a Scala project and is enabled by default.
It has quite a few `scalacOptions` added. If you'd like to see what those options are, simply run 
```sbt
show scalacOptions
```

e.g.)
```
[info] * -language:higherKinds
[info] * -encoding
[info] * UTF-8
[info] * -deprecation
[info] * -unchecked
[info] * -feature
[info] * -Xfatal-warnings
[info] * -Wdead-code
[info] * -Wvalue-discard
[info] * -Xlint:adapted-args
[info] * -Xlint:inaccessible
[info] * -Xlint:nullary-override
[info] * -Wnumeric-widen
[info] * -Wunused:imports
[info] * -Xlint:nullary-unit
[info] * -Xlint:infer-any
[info] * -Xlint:missing-interpolator
[info] * -Xlint:doc-detached
[info] * -Xlint:private-shadow
[info] * -Xlint:type-parameter-shadow
[info] * -Xlint:poly-implicit-overload
[info] * -Xlint:option-implicit
[info] * -Xlint:delayedinit-select
[info] * -Xlint:package-object-classes
[info] * -Xlint:stars-align
[info] * -Xlint:constant
```