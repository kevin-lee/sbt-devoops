"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[717],{8453:(e,s,t)=>{t.d(s,{R:()=>c,x:()=>o});var n=t(6540);const i={},a=n.createContext(i);function c(e){const s=n.useContext(a);return n.useMemo((function(){return"function"==typeof e?e(s):{...s,...e}}),[s,e])}function o(e){let s;return s=e.disableParentContext?"function"==typeof e.components?e.components(i):e.components||i:c(e.components),n.createElement(a.Provider,{value:s},e.children)}},9138:(e,s,t)=>{t.r(s),t.d(s,{assets:()=>r,contentTitle:()=>c,default:()=>g,frontMatter:()=>a,metadata:()=>o,toc:()=>l});var n=t(4848),i=t(8453);const a={id:"config-and-run",title:"DevOopsGitReleasePlugin - Config and Run",sidebar_label:"Config and Run"},c=void 0,o={id:"gh-release-plugin/config-and-run",title:"DevOopsGitReleasePlugin - Config and Run",description:"Enable DevOopsGitReleasePlugin",source:"@site/versioned_docs/version-1.0.3/gh-release-plugin/config-and-run.md",sourceDirName:"gh-release-plugin",slug:"/gh-release-plugin/config-and-run",permalink:"/docs/1.0.3/gh-release-plugin/config-and-run",draft:!1,unlisted:!1,tags:[],version:"1.0.3",frontMatter:{id:"config-and-run",title:"DevOopsGitReleasePlugin - Config and Run",sidebar_label:"Config and Run"},sidebar:"docs",previous:{title:"Getting Started",permalink:"/docs/1.0.3/"},next:{title:"Examples",permalink:"/docs/1.0.3/gh-release-plugin/examples"}},r={},l=[{value:"Enable DevOopsGitReleasePlugin",id:"enable-devoopsgitreleaseplugin",level:2},{value:"Tag",id:"tag",level:2},{value:"<code>gitTagFrom</code>",id:"gittagfrom",level:3},{value:"<code>gitTagDescription</code> (Optional)",id:"gittagdescription-optional",level:3},{value:"<code>gitTagName</code>",id:"gittagname",level:3},{value:"<code>gitTagPushRepo</code>",id:"gittagpushrepo",level:3},{value:"<code>gitTag</code>",id:"gittag",level:3},{value:"Artifacts",id:"artifacts",level:2},{value:"<code>devOopsCiDir</code>",id:"devoopscidir",level:3},{value:"<code>devOopsPackagedArtifacts</code>",id:"devoopspackagedartifacts",level:3},{value:"<code>devOopsCopyReleasePackages</code>",id:"devoopscopyreleasepackages",level:3},{value:"Changelog",id:"changelog",level:2},{value:"<code>changelogLocation</code>",id:"changeloglocation",level:3},{value:"GitHub Release",id:"github-release",level:2},{value:"<code>gitHubAuthTokenEnvVar</code>",id:"githubauthtokenenvvar",level:3},{value:"<code>gitHubAuthTokenFile</code>",id:"githubauthtokenfile",level:3},{value:"<code>artifactsRequiredForGitHubRelease</code>",id:"artifactsrequiredforgithubrelease",level:3},{value:"<code>gitHubRelease</code>",id:"githubrelease",level:3},{value:"<code>gitTagAndGitHubRelease</code>",id:"gittagandgithubrelease",level:3}];function d(e){const s={code:"code",h2:"h2",h3:"h3",li:"li",p:"p",pre:"pre",strong:"strong",ul:"ul",...(0,i.R)(),...e.components};return(0,n.jsxs)(n.Fragment,{children:[(0,n.jsx)(s.h2,{id:"enable-devoopsgitreleaseplugin",children:"Enable DevOopsGitReleasePlugin"}),"\n",(0,n.jsxs)(s.p,{children:["To use ",(0,n.jsx)(s.code,{children:"DevOopsGitReleasePlugin"}),", add the following line to ",(0,n.jsx)(s.code,{children:"build.sbt"}),"."]}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-sbt",children:"enablePlugins(DevOopsGitReleasePlugin)\n"})}),"\n",(0,n.jsx)(s.p,{children:"e.g.) This is an example of the minimal settings."}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-scala",children:'ThisBuild / organization := "com.example"\nThisBuild / scalaVersion := "2.12.7"\nThisBuild / version := "0.1.0"\nThisBuild / crossScalaVersions := Seq("2.11.12", "2.12.8")\n\nlazy val root = (project in file("."))\n  .enablePlugins(DevOopsGitReleasePlugin)\n  .settings(\n    name := "test-project",\n    libraryDependencies += "some" %% "lib" % "1.0.0"\n  )\n'})}),"\n",(0,n.jsx)(s.h2,{id:"tag",children:"Tag"}),"\n",(0,n.jsx)(s.h3,{id:"gittagfrom",children:(0,n.jsx)(s.code,{children:"gitTagFrom"})}),"\n",(0,n.jsxs)(s.p,{children:["The name of the branch from which it tags. So if the current branch is not the same as the ",(0,n.jsx)(s.code,{children:"gitTagFrom"})," value, ",(0,n.jsx)(s.code,{children:"gitTag"})," does not tag but throws an exception."]}),"\n",(0,n.jsx)(s.p,{children:"Default:"}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-scala",children:'gitTagFrom := "master"\n'})}),"\n",(0,n.jsxs)(s.h3,{id:"gittagdescription-optional",children:[(0,n.jsx)(s.code,{children:"gitTagDescription"})," (Optional)"]}),"\n",(0,n.jsxs)(s.p,{children:[(0,n.jsx)(s.code,{children:"gitTagDescription"})," is the setting to specify the tag description. If not set, it tags without any tag description."]}),"\n",(0,n.jsx)(s.p,{children:"Default:"}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-scala",children:"gitTagDescription := None\n"})}),"\n",(0,n.jsx)(s.p,{children:"Wihtout the description, it's equivalent to"}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-bash",children:"git tag ${gitTagFrom.value}\n"})}),"\n",(0,n.jsx)(s.p,{children:"With the description, it's equivalent to"}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-bash",children:"git tag -a ${gitTagFrom.value}, -m ${gitTagDescription.value}\n"})}),"\n",(0,n.jsx)(s.h3,{id:"gittagname",children:(0,n.jsx)(s.code,{children:"gitTagName"})}),"\n",(0,n.jsxs)(s.p,{children:["This setting decides how to name the tag. It uses the project's version (i.e. ",(0,n.jsx)(s.code,{children:"version.value"}),") with the suffix 'v'"]}),"\n",(0,n.jsxs)(s.p,{children:["e.g.) If ",(0,n.jsx)(s.code,{children:'version := "1.0.0"'}),", the tag name is ",(0,n.jsx)(s.code,{children:"v1.0.0"}),"."]}),"\n",(0,n.jsx)(s.p,{children:"Default:"}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-scala",children:'import just.semver.SemVer\n// ...\ngitTagName := s"v${SemVer.render(SemVer.parseUnsafe(version.value))}"\n'})}),"\n",(0,n.jsx)(s.h3,{id:"gittagpushrepo",children:(0,n.jsx)(s.code,{children:"gitTagPushRepo"})}),"\n",(0,n.jsxs)(s.p,{children:["This tells which remote repository to push. It's usually ",(0,n.jsx)(s.code,{children:"origin"}),". If there are multiple repositories, you can change it to the one you want."]}),"\n",(0,n.jsx)(s.p,{children:"e.g.)"}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-scala",children:'gitTagPushRepo := "github"\n'})}),"\n",(0,n.jsx)(s.p,{children:"Default:"}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-scala",children:'gitTagPushRepo := "origin"\n'})}),"\n",(0,n.jsx)(s.h3,{id:"gittag",children:(0,n.jsx)(s.code,{children:"gitTag"})}),"\n",(0,n.jsxs)(s.p,{children:["It is an sbt task to create a git tag from the branch set in ",(0,n.jsx)(s.code,{children:"gitTagFrom"}),". It may fail if the project version is no GA."]}),"\n",(0,n.jsx)(s.p,{children:"e.g.)"}),"\n",(0,n.jsx)(s.p,{children:(0,n.jsx)(s.strong,{children:"Success Case"})}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-sbtshell",children:"sbt:test-project> gitTag\ntask success>\n>> non sbt task success> The semantic version from the project version has been parsed. version: 0.1.0\n>> git rev-parse --abbrev-ref HEAD => master\n>> git fetch --tags\n>> git tag v0.1.0\n>> git push origin v0.1.0\n  |  To github.com:Kevin-Lee/test-project.git\n  |   * [new tag]         v0.1.0 -> v0.1.0\n\n[success] Total time: 7 s, completed 16 Oct. 2019, 5:19:31 pm\n"})}),"\n",(0,n.jsx)(s.p,{children:(0,n.jsx)(s.strong,{children:"Failure Case"})}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-sbtshell",children:"sbt:test-project> gitTag\nFailure]\n>> sbt task failed after finishing the following tasks\ntask success>\n>> non sbt task success> The semantic version from the project version has been parsed. version: 0.1.0-SNAPSHOT\n\n  This version is not eligible for tagging. [version: 0.1.0-SNAPSHOT]\n  It should be GA version with any pre-release or meta-info suffix\n    e.g.)\n    * 1.0.0 (\u2b55\ufe0f)\n    * 1.0.0-SNAPSHOT (\u274c)\n    * 1.0.0-beta (\u274c)\n    * 1.0.0+123 (\u274c)\n    * 1.0.0-beta+123 (\u274c)\n\n"})}),"\n",(0,n.jsx)(s.p,{children:"or"}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-sbtshell",children:"sbt:test-project> gitTag\nFailure]\n>> sbt task failed after finishing the following tasks\ntask success>\n>> non sbt task success> The semantic version from the project version has been parsed. version: 0.1.0\n>> git rev-parse --abbrev-ref HEAD => master\n>> git fetch --tags\n  | => root / gitTag 2s\n>> [cmd: git tag v0.1.0], [code: 128], [errors: fatal: tag 'v0.1.0' already exists]\n\n"})}),"\n",(0,n.jsx)(s.h2,{id:"artifacts",children:"Artifacts"}),"\n",(0,n.jsx)(s.h3,{id:"devoopscidir",children:(0,n.jsx)(s.code,{children:"devOopsCiDir"})}),"\n",(0,n.jsxs)(s.p,{children:[(0,n.jsx)(s.code,{children:"devOopsCiDir"})," is the ci directory which contains the files created in build to upload to GitHub release (e.g. packaged jar files) It can be either an absolute or relative path. When running ",(0,n.jsx)(s.code,{children:"devOopsCopyReleasePackages"}),", all the jar files with prefixed with the project name (",(0,n.jsx)(s.code,{children:"devOopsPackagedArtifacts.value"}),") are copied to ",(0,n.jsx)(s.code,{children:"${devOopsCiDir.value}/dist"}),"."]}),"\n",(0,n.jsx)(s.p,{children:"Default:"}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-scala",children:'devOopsCiDir := "ci"\n// so the artifactsare copied to ci/dist\n'})}),"\n",(0,n.jsx)(s.h3,{id:"devoopspackagedartifacts",children:(0,n.jsx)(s.code,{children:"devOopsPackagedArtifacts"})}),"\n",(0,n.jsxs)(s.p,{children:["A ",(0,n.jsx)(s.code,{children:"List"})," of packaged artifacts to be copied to ",(0,n.jsx)(s.code,{children:"PROJECT_HOME/${devOopsCiDir.value}/dist"}),"."]}),"\n",(0,n.jsx)(s.p,{children:"Default:"}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-scala",children:'devOopsPackagedArtifacts := List(s"target/scala-*/${name.value}*.jar")\n'})}),"\n",(0,n.jsx)(s.p,{children:"So for Java projects, change it to"}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-scala",children:'devOopsPackagedArtifacts := List(s"target/${name.value}*.jar")\n'})}),"\n",(0,n.jsx)(s.h3,{id:"devoopscopyreleasepackages",children:(0,n.jsx)(s.code,{children:"devOopsCopyReleasePackages"})}),"\n",(0,n.jsxs)(s.p,{children:["It is an sbt task to copy packaged artifacts to the location specified (default: ",(0,n.jsx)(s.code,{children:"devOopsPackagedArtifacts.value"})," to ",(0,n.jsx)(s.code,{children:"PROJECT_HOME/${devOopsCiDir.value}/dist"}),")."]}),"\n",(0,n.jsx)(s.p,{children:"e.g.)"}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-sbtshell",children:"sbt:test-project> devOopsCopyReleasePackages\n>> copyPackages - Files copied from:\n  - /user/home/test-project/target/scala-2.12/test-project_2.12-0.1.0.jar\n  - /user/home/test-project/target/scala-2.12/test-project_2.12-0.1.0-sources.jar\n  - /user/home/test-project/target/scala-2.12/test-project_2.12-0.1.0-javadoc.jar\n\n  to\n  - ci/dist/test-project_2.12-0.1.0-javadoc.jar\n  - ci/dist/test-project_2.12-0.1.0-sources.jar\n  - ci/dist/test-project_2.12-0.1.0.jar\n\n\n[success] Total time: 0 s, completed 6 Apr. 2019, 11:32:21 pm\n"})}),"\n",(0,n.jsx)(s.h2,{id:"changelog",children:"Changelog"}),"\n",(0,n.jsx)(s.h3,{id:"changeloglocation",children:(0,n.jsx)(s.code,{children:"changelogLocation"})}),"\n",(0,n.jsxs)(s.p,{children:["The location of changelog file. The change log filename should be the project version with the extension of ",(0,n.jsx)(s.code,{children:".md"}),"."]}),"\n",(0,n.jsxs)(s.p,{children:["e.g.) ",(0,n.jsx)(s.code,{children:'version.value := "1.0.0"'})," then the changelog file should be ",(0,n.jsx)(s.code,{children:"1.0.0.md"})," at the location set in ",(0,n.jsx)(s.code,{children:"changelogLocation"}),"."]}),"\n",(0,n.jsx)(s.p,{children:"Default:"}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-scala",children:'changelogLocation := "changelogs"\n'})}),"\n",(0,n.jsx)(s.h2,{id:"github-release",children:"GitHub Release"}),"\n",(0,n.jsx)(s.h3,{id:"githubauthtokenenvvar",children:(0,n.jsx)(s.code,{children:"gitHubAuthTokenEnvVar"})}),"\n",(0,n.jsxs)(s.p,{children:["The name of environment variable to get the GitHub auth token. It is required to do GitHub release. If the envvar is not found, it will try to read the auth token file set in ",(0,n.jsx)(s.code,{children:"gitHubAuthTokenFile"}),"."]}),"\n",(0,n.jsx)(s.p,{children:"Default:"}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-scala",children:'gitHubAuthTokenEnvVar := "GITHUB_TOKEN"\n'})}),"\n",(0,n.jsx)(s.h3,{id:"githubauthtokenfile",children:(0,n.jsx)(s.code,{children:"gitHubAuthTokenFile"})}),"\n",(0,n.jsxs)(s.p,{children:["The path to GitHub OAuth token file. The file should contain oauth=OAUTH_TOKEN (default: ",(0,n.jsx)(s.code,{children:"Some($USER/.github)"}),") If you want to have a different filename in user's home, do ",(0,n.jsx)(s.code,{children:'Some(new File(Io.getUserHome, "your_filename"))'}),"."]}),"\n",(0,n.jsx)(s.p,{children:"Default:"}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-scala",children:'gitHubAuthTokenFile := Some(new File(Io.getUserHome, ".github"))\n'})}),"\n",(0,n.jsx)(s.p,{children:(0,n.jsxs)(s.strong,{children:["NOTE: This is optional and if there's a value for the environment variable set in ",(0,n.jsx)(s.code,{children:"gitHubAuthTokenEnvVar"}),", The envvar will be used instead of using the value from the auth token file. It will not even try to read the file if the envvar is set."]})}),"\n",(0,n.jsx)(s.h3,{id:"artifactsrequiredforgithubrelease",children:(0,n.jsx)(s.code,{children:"artifactsRequiredForGitHubRelease"})}),"\n",(0,n.jsx)(s.p,{children:"A setting to decide whether to upload the packaged artifacts to GitHub when doing GitHub release."}),"\n",(0,n.jsxs)(s.p,{children:["If it's ",(0,n.jsx)(s.code,{children:"false"}),", no files are uploaded yet the changelog is still uploaded to GitHub."]}),"\n",(0,n.jsx)(s.p,{children:"Default:"}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-sbt",children:"artifactsRequiredForGitHubRelease := true\n"})}),"\n",(0,n.jsx)(s.h3,{id:"githubrelease",children:(0,n.jsx)(s.code,{children:"gitHubRelease"})}),"\n",(0,n.jsx)(s.p,{children:"Is it an sbt task to release the current version by uploading the packaged files and changelog to GitHub.\nIt does"}),"\n",(0,n.jsxs)(s.ul,{children:["\n",(0,n.jsxs)(s.li,{children:["Copy packaged files (",(0,n.jsx)(s.code,{children:"devOopsCopyReleasePackages"}),")"]}),"\n",(0,n.jsxs)(s.li,{children:["Upload the packaged files (if ",(0,n.jsx)(s.code,{children:"artifactsRequiredForGitHubRelease"})," is ",(0,n.jsx)(s.code,{children:"true"}),") and changelog to GitHub."]}),"\n"]}),"\n",(0,n.jsx)(s.p,{children:(0,n.jsxs)(s.strong,{children:["NOTE: It does not create any tag and if the tag with the project version (e.g. version: 1.0.0 => tag: v1.0.0) does not exist, ",(0,n.jsx)(s.code,{children:"gitHubRelease"})," fails"]})}),"\n",(0,n.jsxs)(s.p,{children:["e.g.) ",(0,n.jsx)(s.code,{children:"gitHubRelease"})," with uploading artifacts (",(0,n.jsx)(s.code,{children:"artifactsRequiredForGitHubRelease := true"}),")"]}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-sbtshell",children:"sbt:test-project> gitHubRelease\n>> copyPackages - Files copied from:\n  - /user/home/test-project/target/scala-2.13/test-project_2.13-0.1.0.jar\n  - /user/home/test-project/target/scala-2.13/test-project_2.13-0.1.0-sources.jar\n  - /user/home/test-project/target/scala-2.13/test-project_2.13-0.1.0-javadoc.jar\n\n  to\n  - ci/dist/test-project_2.13-0.1.0-javadoc.jar\n  - ci/dist/test-project_2.13-0.1.0-sources.jar\n  - ci/dist/test-project_2.13-0.1.0.jar\n\n\ntask success>\n>> git fetch --tags\n>> git tag\n  |  v0.1.0\n  |  v0.1.0-SNAPSHOT\n>> task success>\n>> Get GitHub OAuth tokense 7s\n\n>> task success>\n>> Get changelog\n\n>> task success>\n>> git remote get-url origin => git@github.com:Kevin-Lee/test-project.git\n\n>> task success>\n>> Get GitHub repo org and name: Kevin-Lee/test-project\n\n>> task success>\n>> Connect GitHub with OAuth\n\n>> task success>\n>> GitHub release: v0.1.0\n\n>> task success>\n>> Files uploaded:\n    - ci/dist/test-project_2.13-0.1.0-javadoc.jar\n    - ci/dist/test-project_2.13-0.1.0-sources.jar\n    - ci/dist/test-project_2.13-0.1.0.jar\n\n>> task success>\n>> Changelog uploaded:\n    # 0.1.0 - 2019-10-16\n\n    Test Release\n\n[success] Total time: 8 s, completed 16 Oct. 2019, 5:23:06 pm\n"})}),"\n",(0,n.jsxs)(s.p,{children:["e.g.) ",(0,n.jsx)(s.code,{children:"gitHubRelease"})," without uploading artifacts (",(0,n.jsx)(s.code,{children:"artifactsRequiredForGitHubRelease := false"}),")"]}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-sbtshell",children:"```sbtshell\nsbt:test-project> gitHubRelease\n>> copyPackages - Files copied from:\n  -\n\n  to\n  -\n  | => root / devOopsPackagedArtifacts 0s\n\ntask success>\n>> git fetch --tags\n>> git tag\n  |  v0.1.0\n  |  v0.1.0-SNAPSHOT\n>> task success>tHubRelease 4s\n>> Get GitHub OAuth token\n\n>> task success>\n>> Get changelog\n\n>> task success>\n>> git remote get-url origin => git@github.com:Kevin-Lee/test-project.git\n\n>> task success>\n>> Get GitHub repo org and name: Kevin-Lee/test-project\n\n>> task success>\n>> Connect GitHub with OAuth\n\n>> task success>\n>> GitHub release: v0.1.0\n\n>> task success>\n>> No files to upload\n\n>> task success>\n>> Changelog uploaded:\n    # 0.1.0 - 2019-10-16\n\n    Test Release\n\n[success] Total time: 5 s, completed 16 Oct. 2019, 5:09:42 pm\n"})}),"\n",(0,n.jsx)(s.p,{children:"e.g.) When there's no tag with the current version."}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-sbtshell",children:">> copyPackages - Files copied from:\n  - /user/home/test-project/target/scala-2.13/test-project_2.13-0.1.0.jar\n  - /user/home/test-project/target/scala-2.13/test-project_2.13-0.1.0-sources.jar\n  - /user/home/test-project/target/scala-2.13/test-project_2.13-0.1.0-javadoc.jar\n\n  to=> root / gitTagPushRepo 0s\n  - ci/dist/test-project_2.13-0.1.0-javadoc.jar\n  - ci/dist/test-project_2.13-0.1.0-sources.jar\n  - ci/dist/test-project_2.13-0.1.0.jar\n\n\nFailure]\n>> sbt task failed after finishing the following tasks\ntask success>\n>> git fetch --tags\n>> git tag => v0.1.0-SNAPSHOT\n  | => root / gitHubRelease 2s\ntask failed> git command: tag v0.1.0 does not exist. tags: [v0.1.0-SNAPSHOT]\n\n[error] task failed> git command: tag v0.1.0 does not exist. tags: [v0.1.0-SNAPSHOT]\n[error] (gitHubRelease) task failed> git command: tag v0.1.0 does not exist. tags: [v0.1.0-SNAPSHOT]\n[error] Total time: 2 s, completed 16 Oct. 2019, 5:18:05 pm\n"})}),"\n",(0,n.jsx)(s.h3,{id:"gittagandgithubrelease",children:(0,n.jsx)(s.code,{children:"gitTagAndGitHubRelease"})}),"\n",(0,n.jsx)(s.p,{children:"Is it an sbt task to release the current version by uploading the packaged files and changelog to GitHub after git tagging.\nIt does"}),"\n",(0,n.jsxs)(s.ul,{children:["\n",(0,n.jsxs)(s.li,{children:["Copy packaged files (",(0,n.jsx)(s.code,{children:"devOopsCopyReleasePackages"}),")"]}),"\n",(0,n.jsxs)(s.li,{children:["Git tag with the current version (",(0,n.jsx)(s.code,{children:"gitTag"}),")"]}),"\n",(0,n.jsxs)(s.li,{children:["Upload the packaged files (if ",(0,n.jsx)(s.code,{children:"artifactsRequiredForGitHubRelease"})," is ",(0,n.jsx)(s.code,{children:"true"}),") and changelog to GitHub."]}),"\n"]}),"\n",(0,n.jsxs)(s.p,{children:["e.g.) ",(0,n.jsx)(s.code,{children:"gitTagAndGitHubRelease"})," with uploading artifacts (",(0,n.jsx)(s.code,{children:"artifactsRequiredForGitHubRelease := true"}),")"]}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-sbtshell",children:"sbt:test-project> gitTagAndGitHubRelease\n>> copyPackages - Files copied from:\n  - /user/home/test-project/target/scala-2.13/test-project_2.13-0.1.0.jar\n  - /user/home/test-project/target/scala-2.13/test-project_2.13-0.1.0-sources.jar\n  - /user/home/test-project/target/scala-2.13/test-project_2.13-0.1.0-javadoc.jar\n\n  to=> root / devOopsPackagedArtifacts 0s\n  - ci/dist/test-project_2.13-0.1.0-javadoc.jar\n  - ci/dist/test-project_2.13-0.1.0-sources.jar\n  - ci/dist/test-project_2.13-0.1.0.jar\n\n\ntask success>\n>> task success>\n>> Get GitHub OAuth token\n\n>> non sbt task success> The semantic version from the project version has been parsed. version: 0.1.0\n>> git rev-parse --abbrev-ref HEAD => master\n>> git fetch --tags\n>> git tag v0.1.0\n>> git push origin v0.1.0\n  |  To github.com:Kevin-Lee/test-project.git\n  |   * [new tag]         v0.1.0 -> v0.1.0\n>> task success>\n>> Get changelog\n\n>> task success>\n>> git remote get-url origin => git@github.com:Kevin-Lee/test-project.git\n\n>> task success>\n>> Get GitHub repo org and name: Kevin-Lee/test-project\n\n>> task success>\n>> Connect GitHub with OAuth\n\n>> task success>\n>> GitHub release: v0.1.0\n\n>> task success>\n>> Files uploaded:\n    - ci/dist/test-project_2.13-0.1.0-javadoc.jar\n    - ci/dist/test-project_2.13-0.1.0-sources.jar\n    - ci/dist/test-project_2.13-0.1.0.jar\n\n>> task success>\n>> Changelog uploaded:\n    # 0.1.0 - 2019-10-16\n\n    Test Release\n\n[success] Total time: 12 s, completed 16 Oct. 2019, 5:28:00 pm\n"})}),"\n",(0,n.jsxs)(s.p,{children:["e.g.) ",(0,n.jsx)(s.code,{children:"gitTagAndGitHubRelease"})," without uploading artifacts (",(0,n.jsx)(s.code,{children:"artifactsRequiredForGitHubRelease := false"}),")"]}),"\n",(0,n.jsx)(s.pre,{children:(0,n.jsx)(s.code,{className:"language-sbtshell",children:"sbt:test-project> gitTagAndGitHubRelease\n>> copyPackages - Files copied from:\n  -\n\n  to\n  -\n  | => root / gitTagName 0s\n  | => root / devOopsPackagedArtifacts 0s\ntask success>\n>> non sbt task success> The semantic version from the project version has been parsed. version: 0.1.0\n>> git rev-parse --abbrev-ref HEAD => master\n>> git fetch --tags\n>> git tag v0.1.0\n>> git push origin v0.1.0\n  |  To github.com:Kevin-Lee/test-project.git\n  |   * [new tag]         v0.1.0 -> v0.1.0\n\ntask success>\n>> task success>\n>> Get GitHub OAuth token\n\n>> task success>\n>> Get changelog\n  | => root / gitTagAndGitHubRelease 2s\n>> task success>\n>> git remote get-url origin => git@github.com:Kevin-Lee/test-project.git\n\n>> task success>\n>> Get GitHub repo org and name: Kevin-Lee/test-project\n\n>> task success>\n>> Connect GitHub with OAuth\n\n>> task success>\n>> GitHub release: v0.1.0\n\n>> task success>\n>> No files to upload\n\n>> task success>\n>> Changelog uploaded:\n    # 0.1.0 - 2019-10-16\n\n    Test Release\n\n[success] Total time: 10 s, completed 16 Oct. 2019, 1:18:15 pm\n"})})]})}function g(e={}){const{wrapper:s}={...(0,i.R)(),...e.components};return s?(0,n.jsx)(s,{...e,children:(0,n.jsx)(d,{...e})}):d(e)}}}]);