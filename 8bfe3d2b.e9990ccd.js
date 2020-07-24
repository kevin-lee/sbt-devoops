(window.webpackJsonp=window.webpackJsonp||[]).push([[7],{144:function(e,t,n){"use strict";n.r(t),n.d(t,"frontMatter",(function(){return l})),n.d(t,"metadata",(function(){return b})),n.d(t,"rightToc",(function(){return o})),n.d(t,"default",(function(){return p}));var a=n(2),i=n(10),s=(n(0),n(159)),c=n(156),l={id:"examples",title:"DevOopsGitReleasePlugin Examples",sidebar_label:"Examples"},b={id:"gh-release-plugin/examples",isDocsHomePage:!1,title:"DevOopsGitReleasePlugin Examples",description:"With GitHub Actions",source:"@site/docs/gh-release-plugin/examples.md",permalink:"/docs/gh-release-plugin/examples",sidebar_label:"Examples",sidebar:"docs",previous:{title:"DevOopsGitReleasePlugin - Config and Run",permalink:"/docs/gh-release-plugin/config-and-run"},next:{title:"DevOopsScalaPlugin - How to Use",permalink:"/docs/scala-plugin/how-to-use"}},o=[{value:"With GitHub Actions",id:"with-github-actions",children:[]},{value:"A Single Project",id:"a-single-project",children:[{value:"<code>build.sbt</code>",id:"buildsbt",children:[]},{value:"GitHub Actions Config",id:"github-actions-config",children:[]}]},{value:"A Project with Multiple Sub-projects",id:"a-project-with-multiple-sub-projects",children:[]}],r={rightToc:o};function p(e){var t=e.components,n=Object(i.a)(e,["components"]);return Object(s.b)("wrapper",Object(a.a)({},r,n,{components:t,mdxType:"MDXLayout"}),Object(s.b)("h2",{id:"with-github-actions"},"With GitHub Actions"),Object(s.b)("p",null,Object(s.b)("inlineCode",{parentName:"p"},"PROJECT_ROOT/project/plugins.sbt")),Object(s.b)("pre",null,Object(s.b)("code",Object(a.a)({parentName:"pre"},{className:"language-scala"}),'addSbtPlugin("io.kevinlee" % "sbt-devoops" % "1.0.3")\n')),Object(s.b)("h2",{id:"a-single-project"},"A Single Project"),Object(s.b)("p",null,"A single project in the root."),Object(s.b)("h3",{id:"buildsbt"},Object(s.b)("inlineCode",{parentName:"h3"},"build.sbt")),Object(s.b)("p",null,Object(s.b)("inlineCode",{parentName:"p"},"PROJECT_ROOT/build.sbt")),Object(s.b)("pre",null,Object(s.b)("code",Object(a.a)({parentName:"pre"},{className:"language-scala"}),'ThisBuild / organization := "com.example"\nThisBuild / scalaVersion := "2.13.3"\nThisBuild / version := "0.1.0"\n\nlazy val root = (project in file("."))\n  .enablePlugins(DevOopsGitReleasePlugin)\n  .settings(\n    name := "test-project",\n    libraryDependencies += "some" %% "lib" % "1.0.0"\n  )\n')),Object(s.b)("p",null,"Make sure you have changelog in the ",Object(s.b)("inlineCode",{parentName:"p"},"PROJECT_ROOT/changelogs")," folder."),Object(s.b)("p",null,"e.g.)\nIf the project version in ",Object(s.b)("inlineCode",{parentName:"p"},"build.sbt")," is ",Object(s.b)("inlineCode",{parentName:"p"},"0.1.0"),","),Object(s.b)("p",null,Object(s.b)("inlineCode",{parentName:"p"},"PROJECT_ROOT/changelogs/0.1.0.md")," "),Object(s.b)("h3",{id:"github-actions-config"},"GitHub Actions Config"),Object(s.b)("p",null,"Before setting up GitHub Actions, make sure you have a GitHub\n",Object(s.b)("a",Object(a.a)({parentName:"p"},{href:"https://github.com/settings/tokens"}),"personal access token"),"\nwith at least the ",Object(s.b)("inlineCode",{parentName:"p"},"publis_repo")," scope.  "),Object(s.b)("ul",{className:"contains-task-list"},Object(s.b)("li",Object(a.a)({parentName:"ul"},{className:"task-list-item"}),Object(s.b)("input",Object(a.a)({parentName:"li"},{type:"checkbox",checked:!0,disabled:!0}))," ",Object(s.b)("inlineCode",{parentName:"li"},"public_repo     Access public repositories"),Object(s.b)("img",{alt:"Person Access Token Scope",src:Object(c.a)("img/github-personal-access-token.png")}))),Object(s.b)("p",null,"and add the access token to the project's ",Object(s.b)("inlineCode",{parentName:"p"},"Secrets"),"."),Object(s.b)("p",null,"e.g.)"),Object(s.b)("ul",null,Object(s.b)("li",{parentName:"ul"},"Go to ",Object(s.b)("a",Object(a.a)({parentName:"li"},{href:"https://github.com/YOUR_USERNAME/YOUR_PROJECT/settings/secrets"}),"https://github.com/YOUR_USERNAME/YOUR_PROJECT/settings/secrets")),Object(s.b)("li",{parentName:"ul"},"Click the ",Object(s.b)("inlineCode",{parentName:"li"},"New secret")," button"),Object(s.b)("li",{parentName:"ul"},"In the ",Object(s.b)("inlineCode",{parentName:"li"},"Name"),", put the access token name you want (e.g. ",Object(s.b)("inlineCode",{parentName:"li"},"RELEASE_GITHUB_TOKEN"),")"),Object(s.b)("li",{parentName:"ul"},"Add the token to the ",Object(s.b)("inlineCode",{parentName:"li"},"Value"),".")),Object(s.b)("p",null,"To release whenever tag is created and pushed to the remote repo,\nadd the GitHub Actions config yaml file like the following one to\n",Object(s.b)("inlineCode",{parentName:"p"},"PROJECT_ROOT/.github/workflows"),"."),Object(s.b)("p",null,"e.g. ",Object(s.b)("inlineCode",{parentName:"p"},"release.yml")),Object(s.b)("pre",null,Object(s.b)("code",Object(a.a)({parentName:"pre"},{className:"language-yaml"}),"name: Release\n\non:\n  push:\n    tags:\n      - '*'\n\njobs:\n  build:\n\n    runs-on: ubuntu-latest\n\n    strategy:\n      matrix:\n        scala:\n          - { version: \"2.12.11\", binary-version: \"2.12\", java-version: \"8\" }\n\n    steps:\n    - uses: actions/checkout@v2\n    - uses: actions/setup-java@v1\n      with:\n        java-version: ${{ matrix.scala.java-version }}\n\n\n    - name: Cache Coursier\n      uses: actions/cache@v1\n      with:\n        path: ~/.cache/coursier\n        key: ${{ runner.os }}-coursier-scala-${{ matrix.scala.binary-version }}-${{ hashFiles('**/*.sbt') }}-${{ hashFiles('**/build.properties') }}\n        restore-keys: |\n          ${{ runner.os }}-coursier-scala-${{ matrix.scala.binary-version }}-\n  \n    - name: Cache Ivy\n      uses: actions/cache@v1\n      with:\n        path: ~/.ivy2/cache\n        key: ${{ runner.os }}-ivy-scala-${{ matrix.scala.binary-version }}-${{ hashFiles('**/*.sbt') }}-${{ hashFiles('**/build.properties') }}\n        restore-keys: |\n          ${{ runner.os }}-ivy-scala-${{ matrix.scala.binary-version }}-\n\n\n    - name: sbt GitHub Release\n      env:\n        GITHUB_TOKEN: ${{ secrets.RELEASE_GITHUB_TOKEN }}\n      run: |\n        echo \"Run] sbt GitHub release\"\n        echo 'sbt -J-Xmx2048m ++${{ matrix.scala.version }}! clean test packagedArtifacts'\n        sbt -J-Xmx2048m \\\n          ++${{ matrix.scala.version }}! \\\n          clean \\\n          test \\\n          packagedArtifacts \\\n          gitHubRelease\n\n")),Object(s.b)("p",null,"If you want to manually run it, you need run at least the following two tasks."),Object(s.b)("pre",null,Object(s.b)("code",Object(a.a)({parentName:"pre"},{className:"language-bash"}),"sbt packagedArtifacts gitHubRelease\n")),Object(s.b)("h2",{id:"a-project-with-multiple-sub-projects"},"A Project with Multiple Sub-projects"),Object(s.b)("div",{className:"admonition admonition-caution alert alert--warning"},Object(s.b)("div",Object(a.a)({parentName:"div"},{className:"admonition-heading"}),Object(s.b)("h5",{parentName:"div"},Object(s.b)("span",Object(a.a)({parentName:"h5"},{className:"admonition-icon"}),Object(s.b)("svg",Object(a.a)({parentName:"span"},{xmlns:"http://www.w3.org/2000/svg",width:"16",height:"16",viewBox:"0 0 16 16"}),Object(s.b)("path",Object(a.a)({parentName:"svg"},{fillRule:"evenodd",d:"M8.893 1.5c-.183-.31-.52-.5-.887-.5s-.703.19-.886.5L.138 13.499a.98.98 0 0 0 0 1.001c.193.31.53.501.886.501h13.964c.367 0 .704-.19.877-.5a1.03 1.03 0 0 0 .01-1.002L8.893 1.5zm.133 11.497H6.987v-2.003h2.039v2.003zm0-3.004H6.987V5.987h2.039v4.006z"})))),"NOTE:")),Object(s.b)("div",Object(a.a)({parentName:"div"},{className:"admonition-content"}),Object(s.b)("p",{parentName:"div"},"To be added..."))))}p.isMDXComponent=!0}}]);