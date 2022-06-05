"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[162],{3905:function(e,t,n){n.d(t,{Zo:function(){return c},kt:function(){return g}});var r=n(7294);function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function o(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function i(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?o(Object(n),!0).forEach((function(t){a(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):o(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,r,a=function(e,t){if(null==e)return{};var n,r,a={},o=Object.keys(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||(a[n]=e[n]);return a}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(a[n]=e[n])}return a}var l=r.createContext({}),p=function(e){var t=r.useContext(l),n=t;return e&&(n="function"==typeof e?e(t):i(i({},t),e)),n},c=function(e){var t=p(e.components);return r.createElement(l.Provider,{value:t},e.children)},u={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},d=r.forwardRef((function(e,t){var n=e.components,a=e.mdxType,o=e.originalType,l=e.parentName,c=s(e,["components","mdxType","originalType","parentName"]),d=p(n),g=a,v=d["".concat(l,".").concat(g)]||d[g]||u[g]||o;return n?r.createElement(v,i(i({ref:t},c),{},{components:n})):r.createElement(v,i({ref:t},c))}));function g(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var o=n.length,i=new Array(o);i[0]=d;var s={};for(var l in t)hasOwnProperty.call(t,l)&&(s[l]=t[l]);s.originalType=e,s.mdxType="string"==typeof e?e:a,i[1]=s;for(var p=2;p<o;p++)i[p]=n[p];return r.createElement.apply(null,i)}return r.createElement.apply(null,n)}d.displayName="MDXCreateElement"},9390:function(e,t,n){n.r(t),n.d(t,{assets:function(){return c},contentTitle:function(){return l},default:function(){return g},frontMatter:function(){return s},metadata:function(){return p},toc:function(){return u}});var r=n(7462),a=n(3366),o=(n(7294),n(3905)),i=(n(4996),["components"]),s={sidebar_position:1,id:"getting-started",title:"Getting Started",sidebar_label:"Getting Started",slug:"/"},l=void 0,p={unversionedId:"getting-started",id:"getting-started",title:"Getting Started",description:"sbt DevOops",source:"@site/docs/getting-started.md",sourceDirName:".",slug:"/",permalink:"/docs/",draft:!1,tags:[],version:"current",sidebarPosition:1,frontMatter:{sidebar_position:1,id:"getting-started",title:"Getting Started",sidebar_label:"Getting Started",slug:"/"},sidebar:"docsSidebar",next:{title:"Config and Run",permalink:"/docs/gh-release-plugin/config-and-run"}},c={},u=[{value:"<img src='/img/sbt-devoops-logo-64x64.png' /> sbt DevOops",id:"-sbt-devoops",level:3},{value:"Get sbt-devoops",id:"get-sbt-devoops",level:2},{value:"DevOopsGitHubReleasePlugin",id:"devoopsgithubreleaseplugin",level:3}],d={toc:u};function g(e){var t=e.components,n=(0,a.Z)(e,i);return(0,o.kt)("wrapper",(0,r.Z)({},d,n,{components:t,mdxType:"MDXLayout"}),(0,o.kt)("h3",{id:"-sbt-devoops"},(0,o.kt)("img",{src:"/img/sbt-devoops-logo-64x64.png"})," sbt DevOops"),(0,o.kt)("p",null,(0,o.kt)("a",{parentName:"p",href:"https://github.com/Kevin-Lee/sbt-devoops/actions?workflow=Build+All"},(0,o.kt)("img",{parentName:"a",src:"https://github.com/Kevin-Lee/sbt-devoops/workflows/Build%20All/badge.svg",alt:"Build Status"})),"\n",(0,o.kt)("a",{parentName:"p",href:"https://github.com/Kevin-Lee/sbt-devoops/actions?workflow=Release"},(0,o.kt)("img",{parentName:"a",src:"https://github.com/Kevin-Lee/sbt-devoops/workflows/Release/badge.svg",alt:"Release Status"}))),(0,o.kt)("p",null,(0,o.kt)("a",{parentName:"p",href:"https://search.maven.org/artifact/io.kevinlee/sbt-devoops"},(0,o.kt)("img",{parentName:"a",src:"https://maven-badges.herokuapp.com/maven-central/io.kevinlee/sbt-devoops/badge.svg",alt:"Maven Central"})),"\n",(0,o.kt)("a",{parentName:"p",href:"https://index.scala-lang.org/kevin-lee/sbt-devoops/sbt-devoops"},(0,o.kt)("img",{parentName:"a",src:"https://index.scala-lang.org/kevin-lee/sbt-devoops/sbt-devoops/latest.svg",alt:"Latest version"}))),(0,o.kt)("p",null,"SBT Plugin to help release artifacts and changelogs"),(0,o.kt)("h2",{id:"get-sbt-devoops"},"Get sbt-devoops"),(0,o.kt)("div",{className:"admonition admonition-tip alert alert--success"},(0,o.kt)("div",{parentName:"div",className:"admonition-heading"},(0,o.kt)("h5",{parentName:"div"},(0,o.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,o.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"12",height:"16",viewBox:"0 0 12 16"},(0,o.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M6.5 0C3.48 0 1 2.19 1 5c0 .92.55 2.25 1 3 1.34 2.25 1.78 2.78 2 4v1h5v-1c.22-1.22.66-1.75 2-4 .45-.75 1-2.08 1-3 0-2.81-2.48-5-5.5-5zm3.64 7.48c-.25.44-.47.8-.67 1.11-.86 1.41-1.25 2.06-1.45 3.23-.02.05-.02.11-.02.17H5c0-.06 0-.13-.02-.17-.2-1.17-.59-1.83-1.45-3.23-.2-.31-.42-.67-.67-1.11C2.44 6.78 2 5.65 2 5c0-2.2 2.02-4 4.5-4 1.22 0 2.36.42 3.22 1.19C10.55 2.94 11 3.94 11 5c0 .66-.44 1.78-.86 2.48zM4 14h5c-.23 1.14-1.3 2-2.5 2s-2.27-.86-2.5-2z"}))),"Note")),(0,o.kt)("div",{parentName:"div",className:"admonition-content"},(0,o.kt)("p",{parentName:"div"},"Documentation for ",(0,o.kt)("inlineCode",{parentName:"p"},"2.16.0")," is a work in progress."))),(0,o.kt)("p",null,"In the ",(0,o.kt)("inlineCode",{parentName:"p"},"project/plugins.sbt"),", add the following line,"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'addSbtPlugin("io.kevinlee" % "sbt-devoops" % "2.16.0")\n')),(0,o.kt)("h3",{id:"devoopsgithubreleaseplugin"},"DevOopsGitHubReleasePlugin"),(0,o.kt)("p",null,"To use ",(0,o.kt)("inlineCode",{parentName:"p"},"DevOopsGitHubReleasePlugin"),", add the following line to ",(0,o.kt)("inlineCode",{parentName:"p"},"build.sbt"),"."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},"enablePlugins(DevOopsGitHubReleasePlugin)\n")),(0,o.kt)("p",null,"For more about how to set up and use, please check out the next pages ",(0,o.kt)("a",{parentName:"p",href:"gh-release-plugin/config-and-run"},"DevOopsGitHubReleasePlugin - Config and Run"),"."))}g.isMDXComponent=!0}}]);