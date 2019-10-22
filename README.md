# sbt DevOops

[![Build Status](https://semaphoreci.com/api/v1/kevin-lee/sbt-devoops/branches/master/badge.svg)](https://semaphoreci.com/kevin-lee/sbt-devoops)
[ ![Download](https://api.bintray.com/packages/kevinlee/sbt-plugins/sbt-devoops/images/download.svg) ](https://bintray.com/kevinlee/sbt-plugins/sbt-devoops/_latestVersion)

SBT Plugin to help CI and CD

# Get sbt-devoops
In the `project/plugins.sbt`, add the following line,
```sbt
addSbtPlugin("io.kevinlee" % "sbt-devoops" % "1.0.1")
```

# DevOopsScalaPlugin
This plugin is for a Scala project and is enabled by default.
It has quite a few `scalacOptions` added. If you'd like to see what those options are, simply run 
```sbt
show scalacOptions
```

# DevOopsJavaPlugin
To use sbt-devoops for a Java project, add the following line to `build.sbt`
```sbt
enablePlugins(DevOopsJavaPlugin)
```

It has
- `javaVersion` setting
- set `crossPaths` and `autoScalaLibrary` to false
- default `javacOptions`: `-source` and `-target` set to the `javaVersion` above, `-Xlint:unchecked`, and set `-encoding` to `UTF-8` 

```sbt
  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    javaVersion := "1.8" // 1.8 is default if not specified otherwise.
  , crossPaths := false
  , autoScalaLibrary := false
  , javacOptions ++= Seq(
      "-source", javaVersion.value
    , "-target", javaVersion.value
    , "-Xlint:unchecked"
    , "-encoding", "UTF-8"
    )
  )
```


# DevOopsGitReleasePlugin

To use `DevOopsGitReleasePlugin`, add the following line to `build.sbt`.
```sbt
enablePlugins(DevOopsGitReleasePlugin)
```

## Settings

e.g.) This is an example of the minimal settings.
```sbt
lazy val root = (project in file(".")).
  enablePlugins(DevOopsGitReleasePlugin).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.7",
      version      := "0.1.0",
      crossScalaVersions := Seq("2.11.12", "2.12.8")
    )),
    name := "test-project",
    libraryDependencies += "some" %% "lib" % "1.0.0",
  )
```

### `gitTagFrom`
The name of the branch from which it tags. So if the current branch is not the same as the `gitTagFrom` value, `gitTag` does not tag but throws an exception. 

Default: 
```sbt
gitTagFrom := "master"
```

### `gitTagDescription` (Optional)
`gitTagDescription` is the setting to specify the tag description. If not set, it tags without any tag description.

Default:
```sbt
gitTagDescription := None
```

Wihtout the description, it's equivalent to 
```bash
git tag ${gitTagFrom.value}
```

With the description, it's equivalent to 

```bash
git tag -a ${gitTagFrom.value}, -m ${gitTagDescription.value}
```

### `gitTagName`
This setting decides how to name the tag. It uses the project's version (i.e. `version.value`) with the suffix 'v'

e.g.) If `version := "1.0.0"`, the tag name is `v1.0.0`.

Default:
```sbt
gitTagName := decideVersion(version.value, v => s"v${SemanticVersion.parseUnsafe(v).render}")
```

### `gitTagPushRepo`
This tells which remote repository to push. It's usually `origin`. If there are multiple repositories, you can change it to the one you want.

e.g.)
```sbt
gitTagPushRepo := "github"
```

Default:
```sbt
gitTagPushRepo := "origin"
```


### `gitTag`
It is an sbt task to create a git tag from the branch set in `gitTagFrom`. It may fail if the project version is no GA.

e.g.) 

**Success Case**
```sbtshell
sbt:test-project> gitTag
task success>
>> non sbt task success> The semantic version from the project version has been parsed. version: 0.1.0
>> git rev-parse --abbrev-ref HEAD => master
>> git fetch --tags
>> git tag v0.1.0
>> git push origin v0.1.0
  |  To github.com:Kevin-Lee/test-project.git
  |   * [new tag]         v0.1.0 -> v0.1.0

[success] Total time: 7 s, completed 16 Oct. 2019, 5:19:31 pm
```

**Failure Case**
```sbtshell
sbt:test-project> gitTag
Failure]
>> sbt task failed after finishing the following tasks
task success>
>> non sbt task success> The semantic version from the project version has been parsed. version: 0.1.0-SNAPSHOT

  This version is not eligible for tagging. [version: 0.1.0-SNAPSHOT]
  It should be GA version with any pre-release or meta-info suffix
    e.g.)
    * 1.0.0 (⭕️)
    * 1.0.0-SNAPSHOT (❌)
    * 1.0.0-beta (❌)
    * 1.0.0+123 (❌)
    * 1.0.0-beta+123 (❌)

```
or
```sbtshell
sbt:test-project> gitTag
Failure]
>> sbt task failed after finishing the following tasks
task success>
>> non sbt task success> The semantic version from the project version has been parsed. version: 0.1.0
>> git rev-parse --abbrev-ref HEAD => master
>> git fetch --tags
  | => root / gitTag 2s
>> [cmd: git tag v0.1.0], [code: 128], [errors: fatal: tag 'v0.1.0' already exists]

```

### `devOopsCiDir`
`devOopsCiDir` is the ci directory which contains the files created in build to upload to GitHub release (e.g. packaged jar files) It can be either an absolute or relative path. When running `devOopsCopyReleasePackages`, all the jar files with prefixed with the project name (`devOopsPackagedArtifacts.value`) are copied to `${devOopsCiDir.value}/dist`.

Default:
```sbt
devOopsCiDir := "ci"
// so the artifactsare copied to ci/dist
```

### `devOopsPackagedArtifacts`
A `List` of packaged artifacts to be copied to `PROJECT_HOME/${devOopsCiDir.value}/dist`.

Default:
```sbt
devOopsPackagedArtifacts := List(s"target/scala-*/${name.value}*.jar")
```

So for Java projects, change it to 
```sbt
devOopsPackagedArtifacts := List(s"target/${name.value}*.jar")
```

### `devOopsCopyReleasePackages`
It is an sbt task to copy packaged artifacts to the location specified (default: `devOopsPackagedArtifacts.value` to `PROJECT_HOME/${devOopsCiDir.value}/dist`).

e.g.)
```sbtshell
sbt:test-project> devOopsCopyReleasePackages
>> copyPackages - Files copied from:
  - /user/home/test-project/target/scala-2.12/test-project_2.12-0.1.0.jar
  - /user/home/test-project/target/scala-2.12/test-project_2.12-0.1.0-sources.jar
  - /user/home/test-project/target/scala-2.12/test-project_2.12-0.1.0-javadoc.jar

  to
  - ci/dist/test-project_2.12-0.1.0-javadoc.jar
  - ci/dist/test-project_2.12-0.1.0-sources.jar
  - ci/dist/test-project_2.12-0.1.0.jar


[success] Total time: 0 s, completed 6 Apr. 2019, 11:32:21 pm
```


### `changelogLocation`
The location of changelog file. The change log filename should be the project version with the extension of `.md`.

e.g.) `version.value := "1.0.0"` then the changelog file should be `1.0.0.md` at the location set in `changelogLocation`.

Default:
```sbt
changelogLocation := "changelogs"
```

### `gitHubAuthTokenEnvVar`
The name of environment variable to get the GitHub auth token. It is required to do GitHub release. If the envvar is not found, it will try to read the auth token file set in `gitHubAuthTokenFile`.

Default:
```sbt
gitHubAuthTokenEnvVar := "GITHUB_TOKEN"
```

### `gitHubAuthTokenFile`
The path to GitHub OAuth token file. The file should contain oauth=OAUTH_TOKEN (default: `Some($USER/.github)`) If you want to have a different filename in user's home, do `Some(new File(Io.getUserHome, "your_filename"))`.

Default:
```sbt
gitHubAuthTokenFile := Some(new File(Io.getUserHome, ".github"))
```
**NOTE: This is optional and if there's a value for the environment variable set in `gitHubAuthTokenEnvVar`, The envvar will be used instead of using the value from the auth token file. It will not even try to read the file if the envvar is set.**

### `artifactsRequiredForGitHubRelease`
A setting to decide whether to upload the packaged artifacts to GitHub when doing GitHub release.

If it's `false`, no files are uploaded yet the changelog is still uploaded to GitHub.

Default:
```sbt
artifactsRequiredForGitHubRelease := true
```

### `gitHubRelease`
Is it an sbt task to release the current version by uploading the packaged files and changelog to GitHub.
It does
* Copy packaged files (`devOopsCopyReleasePackages`)
* Upload the packaged files (if `artifactsRequiredForGitHubRelease` is `true`) and changelog to GitHub.

**NOTE: It does not create any tag and if the tag with the project version (e.g. version: 1.0.0 => tag: v1.0.0) does not exist, `gitHubRelease` fails**

e.g.) `gitHubRelease` with uploading artifacts (`artifactsRequiredForGitHubRelease := true`)
```sbtshell
sbt:test-project> gitHubRelease
>> copyPackages - Files copied from:
  - /user/home/test-project/target/scala-2.13/test-project_2.13-0.1.0.jar
  - /user/home/test-project/target/scala-2.13/test-project_2.13-0.1.0-sources.jar
  - /user/home/test-project/target/scala-2.13/test-project_2.13-0.1.0-javadoc.jar

  to
  - ci/dist/test-project_2.13-0.1.0-javadoc.jar
  - ci/dist/test-project_2.13-0.1.0-sources.jar
  - ci/dist/test-project_2.13-0.1.0.jar


task success>
>> git fetch --tags
>> git tag
  |  v0.1.0
  |  v0.1.0-SNAPSHOT
>> task success>
>> Get GitHub OAuth tokense 7s

>> task success>
>> Get changelog

>> task success>
>> git remote get-url origin => git@github.com:Kevin-Lee/test-project.git

>> task success>
>> Get GitHub repo org and name: Kevin-Lee/test-project

>> task success>
>> Connect GitHub with OAuth

>> task success>
>> GitHub release: v0.1.0

>> task success>
>> Files uploaded:
    - ci/dist/test-project_2.13-0.1.0-javadoc.jar
    - ci/dist/test-project_2.13-0.1.0-sources.jar
    - ci/dist/test-project_2.13-0.1.0.jar

>> task success>
>> Changelog uploaded:
    # 0.1.0 - 2019-10-16

    Test Release

[success] Total time: 8 s, completed 16 Oct. 2019, 5:23:06 pm
```

e.g.) `gitHubRelease` without uploading artifacts (`artifactsRequiredForGitHubRelease := false`)
```sbtshell
```sbtshell
sbt:test-project> gitHubRelease
>> copyPackages - Files copied from:
  -

  to
  -
  | => root / devOopsPackagedArtifacts 0s

task success>
>> git fetch --tags
>> git tag
  |  v0.1.0
  |  v0.1.0-SNAPSHOT
>> task success>tHubRelease 4s
>> Get GitHub OAuth token

>> task success>
>> Get changelog

>> task success>
>> git remote get-url origin => git@github.com:Kevin-Lee/test-project.git

>> task success>
>> Get GitHub repo org and name: Kevin-Lee/test-project

>> task success>
>> Connect GitHub with OAuth

>> task success>
>> GitHub release: v0.1.0

>> task success>
>> No files to upload

>> task success>
>> Changelog uploaded:
    # 0.1.0 - 2019-10-16

    Test Release

[success] Total time: 5 s, completed 16 Oct. 2019, 5:09:42 pm
```

e.g.) When there's no tag with the current version.
```sbtshell
>> copyPackages - Files copied from:
  - /user/home/test-project/target/scala-2.13/test-project_2.13-0.1.0.jar
  - /user/home/test-project/target/scala-2.13/test-project_2.13-0.1.0-sources.jar
  - /user/home/test-project/target/scala-2.13/test-project_2.13-0.1.0-javadoc.jar

  to=> root / gitTagPushRepo 0s
  - ci/dist/test-project_2.13-0.1.0-javadoc.jar
  - ci/dist/test-project_2.13-0.1.0-sources.jar
  - ci/dist/test-project_2.13-0.1.0.jar


Failure]
>> sbt task failed after finishing the following tasks
task success>
>> git fetch --tags
>> git tag => v0.1.0-SNAPSHOT
  | => root / gitHubRelease 2s
task failed> git command: tag v0.1.0 does not exist. tags: [v0.1.0-SNAPSHOT]

[error] task failed> git command: tag v0.1.0 does not exist. tags: [v0.1.0-SNAPSHOT]
[error] (gitHubRelease) task failed> git command: tag v0.1.0 does not exist. tags: [v0.1.0-SNAPSHOT]
[error] Total time: 2 s, completed 16 Oct. 2019, 5:18:05 pm
```


### `gitTagAndGitHubRelease`
Is it an sbt task to release the current version by uploading the packaged files and changelog to GitHub after git tagging.
It does
* Copy packaged files (`devOopsCopyReleasePackages`)
* Git tag with the current version (`gitTag`)
* Upload the packaged files (if `artifactsRequiredForGitHubRelease` is `true`) and changelog to GitHub.

e.g.) `gitTagAndGitHubRelease` with uploading artifacts (`artifactsRequiredForGitHubRelease := true`)
```sbtshell
sbt:test-project> gitTagAndGitHubRelease
>> copyPackages - Files copied from:
  - /user/home/test-project/target/scala-2.13/test-project_2.13-0.1.0.jar
  - /user/home/test-project/target/scala-2.13/test-project_2.13-0.1.0-sources.jar
  - /user/home/test-project/target/scala-2.13/test-project_2.13-0.1.0-javadoc.jar

  to=> root / devOopsPackagedArtifacts 0s
  - ci/dist/test-project_2.13-0.1.0-javadoc.jar
  - ci/dist/test-project_2.13-0.1.0-sources.jar
  - ci/dist/test-project_2.13-0.1.0.jar


task success>
>> task success>
>> Get GitHub OAuth token

>> non sbt task success> The semantic version from the project version has been parsed. version: 0.1.0
>> git rev-parse --abbrev-ref HEAD => master
>> git fetch --tags
>> git tag v0.1.0
>> git push origin v0.1.0
  |  To github.com:Kevin-Lee/test-project.git
  |   * [new tag]         v0.1.0 -> v0.1.0
>> task success>
>> Get changelog

>> task success>
>> git remote get-url origin => git@github.com:Kevin-Lee/test-project.git

>> task success>
>> Get GitHub repo org and name: Kevin-Lee/test-project

>> task success>
>> Connect GitHub with OAuth

>> task success>
>> GitHub release: v0.1.0

>> task success>
>> Files uploaded:
    - ci/dist/test-project_2.13-0.1.0-javadoc.jar
    - ci/dist/test-project_2.13-0.1.0-sources.jar
    - ci/dist/test-project_2.13-0.1.0.jar

>> task success>
>> Changelog uploaded:
    # 0.1.0 - 2019-10-16

    Test Release

[success] Total time: 12 s, completed 16 Oct. 2019, 5:28:00 pm
```

e.g.) `gitTagAndGitHubRelease` without uploading artifacts (`artifactsRequiredForGitHubRelease := false`)
```sbtshell
sbt:test-project> gitTagAndGitHubRelease
>> copyPackages - Files copied from:
  -

  to
  -
  | => root / gitTagName 0s
  | => root / devOopsPackagedArtifacts 0s
task success>
>> non sbt task success> The semantic version from the project version has been parsed. version: 0.1.0
>> git rev-parse --abbrev-ref HEAD => master
>> git fetch --tags
>> git tag v0.1.0
>> git push origin v0.1.0
  |  To github.com:Kevin-Lee/test-project.git
  |   * [new tag]         v0.1.0 -> v0.1.0

task success>
>> task success>
>> Get GitHub OAuth token

>> task success>
>> Get changelog
  | => root / gitTagAndGitHubRelease 2s
>> task success>
>> git remote get-url origin => git@github.com:Kevin-Lee/test-project.git

>> task success>
>> Get GitHub repo org and name: Kevin-Lee/test-project

>> task success>
>> Connect GitHub with OAuth

>> task success>
>> GitHub release: v0.1.0

>> task success>
>> No files to upload

>> task success>
>> Changelog uploaded:
    # 0.1.0 - 2019-10-16

    Test Release

[success] Total time: 10 s, completed 16 Oct. 2019, 1:18:15 pm
```
