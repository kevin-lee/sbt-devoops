---
id: examples
title: DevOopsGitHubReleasePlugin Examples
sidebar_label: Examples
---

## With GitHub Actions

`PROJECT_ROOT/project/plugins.sbt`
```scala
addSbtPlugin("io.kevinlee" % "sbt-devoops" % "3.8.0")
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
  .enablePlugins(DevOopsGitHubReleasePlugin)
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
GitHub Actions provides a built-in `GITHUB_TOKEN`, so no personal access token is required.
You only need to allow the workflow to write to the repository.

e.g.)
* Go to `https://github.com/YOUR_USERNAME/YOUR_PROJECT/settings/actions`
* Under `Workflow permissions`, select `Read and write permissions`
* Click the `Save` button

Alternatively, you can grant the permission per workflow (or per job) with the `permissions` key
instead of changing the repository settings.
```yaml
permissions:
  contents: write
```

Then pass `${{ secrets.GITHUB_TOKEN }}` to the `GITHUB_TOKEN` environment variable in the steps
running the release tasks.

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
          - { version: "2.13.3", binary-version: "2.13", java-version: "11" }

    steps:
    - uses: actions/checkout@v2
    - uses: actions/setup-java@v1
      with:
        java-version: ${{ matrix.scala.java-version }}

    - name: Cache SBT
      uses: actions/cache@v2
      with:
        path: |
          ~/.ivy2/cache
          ~/.cache/coursier
          ~/.sbt
        key: ${{ runner.os }}-sbt-${{ matrix.scala.binary-version }}-${{ hashFiles('**/*.sbt') }}-${{ hashFiles('**/build.properties') }}
        restore-keys: |
          ${{ runner.os }}-sbt-${{ matrix.scala.binary-version }}-

    - name: sbt GitHub Release
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
      run: |
        echo "Run] sbt GitHub release"
        echo 'sbt -J-Xmx2048m ++${{ matrix.scala.version }}! clean test packagedArtifacts'
        sbt -J-Xmx2048m \
          ++${{ matrix.scala.version }}! \
          clean \
          test \
          packagedArtifacts \
          devOopsGitHubRelease \
          devOopsGitHubReleaseUploadArtifacts

    - name: sbt Append Generated Release Notes
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        TAG_NAME: ${{ github.ref_name }}
      run: |
        echo "Run] sbt append generated release notes"
        echo 'sbt -J-Xmx2048m ++${{ matrix.scala.version }}! "devOopsReleaseFromTag ${TAG_NAME}"'
        sbt \
          -J-Xmx2048m \
          ++${{ matrix.scala.version }}! \
          "devOopsReleaseFromTag ${TAG_NAME}"
```

The last step appends GitHub's
[automatically generated release notes](https://docs.github.com/en/repositories/releasing-projects-on-github/automatically-generated-release-notes)
to the release note uploaded by `devOopsGitHubRelease`.
Since the workflow is triggered by a tag push, `${{ github.ref_name }}` is the tag name.
`devOopsReleaseFromTag` skips appending when the generated release notes are already there, so re-running the
workflow does not duplicate them.
For more details, see [devOopsReleaseFromTag](config-and-run.md#devoopsreleasefromtag).

If you want to manually run it, note that `secrets.GITHUB_TOKEN` exists only inside GitHub Actions,
so a local run still needs your own
[personal access token](https://github.com/settings/tokens)
with at least the `public_repo` scope (`repo` for a private repository).
Make it available in one of the following ways.
* Set it to the `GITHUB_TOKEN` environment variable
  (the name is configurable with [devOopsGitHubAuthTokenEnvVar](config-and-run.md#devoopsgithubauthtokenenvvar)).
  ```bash
  export GITHUB_TOKEN=YOUR_PERSONAL_ACCESS_TOKEN
  ```
* Or put it in `$USER_HOME/.github` as `oauth=YOUR_PERSONAL_ACCESS_TOKEN`
  (the path is configurable with [devOopsGitHubAuthTokenFile](config-and-run.md#devoopsgithubauthtokenfile)).

Then you need to run at least the following three tasks.
```bash
sbt packagedArtifacts devOopsGitHubRelease devOopsGitHubReleaseUploadArtifacts
```

and then, to append the generated release notes,
```bash
sbt 'devOopsReleaseFromTag v0.1.0'
```

## A Project with Multiple Sub-projects
:::caution NOTE:
To be added...
:::
