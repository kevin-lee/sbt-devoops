---
id: examples
title: DevOopsGitReleasePlugin Examples
sidebar_label: Examples
---
import useBaseUrl from '@docusaurus/useBaseUrl';

## With GitHub Actions

`PROJECT_ROOT/project/plugins.sbt`
```scala
addSbtPlugin("io.kevinlee" % "sbt-devoops" % "1.0.3")
```

## A Single Project

A single project in the root.

### `build.sbt`

`PROJECT_ROOT/build.sbt`
```scala
ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version := "0.1.0"

lazy val root = (project in file("."))
  .enablePlugins(DevOopsGitReleasePlugin)
  .settings(
    name := "test-project",
    libraryDependencies += "some" %% "lib" % "1.0.0"
  )
```

Make sure you have changelog in the `PROJECT_ROOT/changelogs` folder.

e.g.)
If the project version in `build.sbt` is `0.1.0`,

`PROJECT_ROOT/changelogs/0.1.0.md` 


### GitHub Actions Config
Before setting up GitHub Actions, make sure you have a GitHub 
[personal access token](https://github.com/settings/tokens) 
with at least the `publis_repo` scope.  
- [X] `public_repo     Access public repositories`
  <img alt="Person Access Token Scope" src={useBaseUrl('img/github-personal-access-token.png')} />

and add the access token to the project's `Secrets`.

e.g.)
* Go to https://github.com/YOUR_USERNAME/YOUR_PROJECT/settings/secrets
* Click the `New secret` button
* In the `Name`, put the access token name you want (e.g. `RELEASE_GITHUB_TOKEN`)
* Add the token to the `Value`.

To release whenever tag is created and pushed to the remote repo, 
add the GitHub Actions config yaml file like the following one to 
`PROJECT_ROOT/.github/workflows`.

e.g. `release.yml`
```yaml
name: Release

on:
  push:
    tags:
      - '*'

jobs:
  build:

    runs-on: ubuntu-latest

    strategy:
      matrix:
        scala:
          - { version: "2.12.11", binary-version: "2.12", java-version: "8" }

    steps:
    - uses: actions/checkout@v2
    - uses: actions/setup-java@v1
      with:
        java-version: ${{ matrix.scala.java-version }}


    - name: Cache Coursier
      uses: actions/cache@v1
      with:
        path: ~/.cache/coursier
        key: ${{ runner.os }}-coursier-scala-${{ matrix.scala.binary-version }}-${{ hashFiles('**/*.sbt') }}-${{ hashFiles('**/build.properties') }}
        restore-keys: |
          ${{ runner.os }}-coursier-scala-${{ matrix.scala.binary-version }}-
  
    - name: Cache Ivy
      uses: actions/cache@v1
      with:
        path: ~/.ivy2/cache
        key: ${{ runner.os }}-ivy-scala-${{ matrix.scala.binary-version }}-${{ hashFiles('**/*.sbt') }}-${{ hashFiles('**/build.properties') }}
        restore-keys: |
          ${{ runner.os }}-ivy-scala-${{ matrix.scala.binary-version }}-


    - name: sbt GitHub Release
      env:
        GITHUB_TOKEN: ${{ secrets.RELEASE_GITHUB_TOKEN }}
      run: |
        echo "Run] sbt GitHub release"
        echo 'sbt -J-Xmx2048m ++${{ matrix.scala.version }}! clean test packagedArtifacts'
        sbt -J-Xmx2048m \
          ++${{ matrix.scala.version }}! \
          clean \
          test \
          packagedArtifacts \
          gitHubRelease

```

## A Project with Multiple Sub-projects
:::caution NOTE:
To be added...
:::
