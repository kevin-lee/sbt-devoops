"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[205],{3905:(e,t,n)=>{n.d(t,{Zo:()=>p,kt:()=>b});var a=n(7294);function i(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function r(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function l(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?r(Object(n),!0).forEach((function(t){i(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):r(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,a,i=function(e,t){if(null==e)return{};var n,a,i={},r=Object.keys(e);for(a=0;a<r.length;a++)n=r[a],t.indexOf(n)>=0||(i[n]=e[n]);return i}(e,t);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);for(a=0;a<r.length;a++)n=r[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(i[n]=e[n])}return i}var o=a.createContext({}),c=function(e){var t=a.useContext(o),n=t;return e&&(n="function"==typeof e?e(t):l(l({},t),e)),n},p=function(e){var t=c(e.components);return a.createElement(o.Provider,{value:t},e.children)},u="mdxType",m={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},d=a.forwardRef((function(e,t){var n=e.components,i=e.mdxType,r=e.originalType,o=e.parentName,p=s(e,["components","mdxType","originalType","parentName"]),u=c(n),d=i,b=u["".concat(o,".").concat(d)]||u[d]||m[d]||r;return n?a.createElement(b,l(l({ref:t},p),{},{components:n})):a.createElement(b,l({ref:t},p))}));function b(e,t){var n=arguments,i=t&&t.mdxType;if("string"==typeof e||i){var r=n.length,l=new Array(r);l[0]=d;var s={};for(var o in t)hasOwnProperty.call(t,o)&&(s[o]=t[o]);s.originalType=e,s[u]="string"==typeof e?e:i,l[1]=s;for(var c=2;c<r;c++)l[c]=n[c];return a.createElement.apply(null,l)}return a.createElement.apply(null,n)}d.displayName="MDXCreateElement"},6406:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>c,contentTitle:()=>s,default:()=>m,frontMatter:()=>l,metadata:()=>o,toc:()=>p});var a=n(7462),i=(n(7294),n(3905)),r=n(4996);const l={id:"examples",title:"DevOopsGitReleasePlugin Examples",sidebar_label:"Examples"},s=void 0,o={unversionedId:"gh-release-plugin/examples",id:"version-1.0.3/gh-release-plugin/examples",title:"DevOopsGitReleasePlugin Examples",description:"With GitHub Actions",source:"@site/versioned_docs/version-1.0.3/gh-release-plugin/examples.md",sourceDirName:"gh-release-plugin",slug:"/gh-release-plugin/examples",permalink:"/docs/1.0.3/gh-release-plugin/examples",draft:!1,tags:[],version:"1.0.3",frontMatter:{id:"examples",title:"DevOopsGitReleasePlugin Examples",sidebar_label:"Examples"},sidebar:"version-1.0.3/docs",previous:{title:"Config and Run",permalink:"/docs/1.0.3/gh-release-plugin/config-and-run"},next:{title:"How to Use",permalink:"/docs/1.0.3/scala-plugin/how-to-use"}},c={},p=[{value:"With GitHub Actions",id:"with-github-actions",level:2},{value:"A Single Project",id:"a-single-project",level:2},{value:"<code>build.sbt</code>",id:"buildsbt",level:3},{value:"GitHub Actions Config",id:"github-actions-config",level:3},{value:"A Project with Multiple Sub-projects",id:"a-project-with-multiple-sub-projects",level:2}],u={toc:p};function m(e){let{components:t,...n}=e;return(0,i.kt)("wrapper",(0,a.Z)({},u,n,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("h2",{id:"with-github-actions"},"With GitHub Actions"),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"PROJECT_ROOT/project/plugins.sbt")),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'addSbtPlugin("io.kevinlee" % "sbt-devoops" % "1.0.3")\n')),(0,i.kt)("h2",{id:"a-single-project"},"A Single Project"),(0,i.kt)("p",null,"A single project in the root."),(0,i.kt)("h3",{id:"buildsbt"},(0,i.kt)("inlineCode",{parentName:"h3"},"build.sbt")),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"PROJECT_ROOT/build.sbt")),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'ThisBuild / organization := "com.example"\nThisBuild / scalaVersion := "2.13.3"\nThisBuild / version := "0.1.0"\n\nlazy val root = (project in file("."))\n  .enablePlugins(DevOopsGitReleasePlugin)\n  .settings(\n    name := "test-project",\n    libraryDependencies += "some" %% "lib" % "1.0.0"\n  )\n')),(0,i.kt)("p",null,"Make sure you have changelog in the ",(0,i.kt)("inlineCode",{parentName:"p"},"PROJECT_ROOT/changelogs")," folder."),(0,i.kt)("p",null,"e.g.)\nIf the project version in ",(0,i.kt)("inlineCode",{parentName:"p"},"build.sbt")," is ",(0,i.kt)("inlineCode",{parentName:"p"},"0.1.0"),","),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"PROJECT_ROOT/changelogs/0.1.0.md")," "),(0,i.kt)("h3",{id:"github-actions-config"},"GitHub Actions Config"),(0,i.kt)("p",null,"Before setting up GitHub Actions, make sure you have a GitHub\n",(0,i.kt)("a",{parentName:"p",href:"https://github.com/settings/tokens"},"personal access token"),"\nwith at least the ",(0,i.kt)("inlineCode",{parentName:"p"},"publis_repo")," scope.  "),(0,i.kt)("ul",{className:"contains-task-list"},(0,i.kt)("li",{parentName:"ul",className:"task-list-item"},(0,i.kt)("input",{parentName:"li",type:"checkbox",checked:!0,disabled:!0})," ",(0,i.kt)("inlineCode",{parentName:"li"},"public_repo     Access public repositories"),(0,i.kt)("img",{alt:"Person Access Token Scope",src:(0,r.Z)("img/github-personal-access-token.png")}))),(0,i.kt)("p",null,"and add the access token to the project's ",(0,i.kt)("inlineCode",{parentName:"p"},"Secrets"),"."),(0,i.kt)("p",null,"e.g.)"),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},"Go to ",(0,i.kt)("a",{parentName:"li",href:"https://github.com/YOUR_USERNAME/YOUR_PROJECT/settings/secrets"},"https://github.com/YOUR_USERNAME/YOUR_PROJECT/settings/secrets")),(0,i.kt)("li",{parentName:"ul"},"Click the ",(0,i.kt)("inlineCode",{parentName:"li"},"New secret")," button"),(0,i.kt)("li",{parentName:"ul"},"In the ",(0,i.kt)("inlineCode",{parentName:"li"},"Name"),", put the access token name you want (e.g. ",(0,i.kt)("inlineCode",{parentName:"li"},"RELEASE_GITHUB_TOKEN"),")"),(0,i.kt)("li",{parentName:"ul"},"Add the token to the ",(0,i.kt)("inlineCode",{parentName:"li"},"Value"),".")),(0,i.kt)("p",null,"To release whenever tag is created and pushed to the remote repo,\nadd the GitHub Actions config yaml file like the following one to\n",(0,i.kt)("inlineCode",{parentName:"p"},"PROJECT_ROOT/.github/workflows"),"."),(0,i.kt)("p",null,"e.g. ",(0,i.kt)("inlineCode",{parentName:"p"},"release.yml")),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-yaml"},"name: Release\n\non:\n  push:\n    tags:\n      - '*'\n\njobs:\n  build:\n\n    runs-on: ubuntu-latest\n\n    strategy:\n      matrix:\n        scala:\n          - { version: \"2.12.11\", binary-version: \"2.12\", java-version: \"8\" }\n\n    steps:\n    - uses: actions/checkout@v2\n    - uses: actions/setup-java@v1\n      with:\n        java-version: ${{ matrix.scala.java-version }}\n\n\n    - name: Cache Coursier\n      uses: actions/cache@v1\n      with:\n        path: ~/.cache/coursier\n        key: ${{ runner.os }}-coursier-scala-${{ matrix.scala.binary-version }}-${{ hashFiles('**/*.sbt') }}-${{ hashFiles('**/build.properties') }}\n        restore-keys: |\n          ${{ runner.os }}-coursier-scala-${{ matrix.scala.binary-version }}-\n  \n    - name: Cache Ivy\n      uses: actions/cache@v1\n      with:\n        path: ~/.ivy2/cache\n        key: ${{ runner.os }}-ivy-scala-${{ matrix.scala.binary-version }}-${{ hashFiles('**/*.sbt') }}-${{ hashFiles('**/build.properties') }}\n        restore-keys: |\n          ${{ runner.os }}-ivy-scala-${{ matrix.scala.binary-version }}-\n\n\n    - name: sbt GitHub Release\n      env:\n        GITHUB_TOKEN: ${{ secrets.RELEASE_GITHUB_TOKEN }}\n      run: |\n        echo \"Run] sbt GitHub release\"\n        echo 'sbt -J-Xmx2048m ++${{ matrix.scala.version }}! clean test packagedArtifacts'\n        sbt -J-Xmx2048m \\\n          ++${{ matrix.scala.version }}! \\\n          clean \\\n          test \\\n          packagedArtifacts \\\n          gitHubRelease\n\n")),(0,i.kt)("p",null,"If you want to manually run it, you need run at least the following two tasks."),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-bash"},"sbt packagedArtifacts gitHubRelease\n")),(0,i.kt)("h2",{id:"a-project-with-multiple-sub-projects"},"A Project with Multiple Sub-projects"),(0,i.kt)("admonition",{title:"NOTE:",type:"caution"},(0,i.kt)("p",{parentName:"admonition"},"To be added...")))}m.isMDXComponent=!0}}]);