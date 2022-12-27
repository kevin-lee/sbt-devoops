"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[866],{3905:(e,t,n)=>{n.d(t,{Zo:()=>u,kt:()=>f});var a=n(7294);function r(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function o(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function i(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?o(Object(n),!0).forEach((function(t){r(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):o(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,a,r=function(e,t){if(null==e)return{};var n,a,r={},o=Object.keys(e);for(a=0;a<o.length;a++)n=o[a],t.indexOf(n)>=0||(r[n]=e[n]);return r}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(a=0;a<o.length;a++)n=o[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}var s=a.createContext({}),p=function(e){var t=a.useContext(s),n=t;return e&&(n="function"==typeof e?e(t):i(i({},t),e)),n},u=function(e){var t=p(e.components);return a.createElement(s.Provider,{value:t},e.children)},c="mdxType",d={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},v=a.forwardRef((function(e,t){var n=e.components,r=e.mdxType,o=e.originalType,s=e.parentName,u=l(e,["components","mdxType","originalType","parentName"]),c=p(n),v=r,f=c["".concat(s,".").concat(v)]||c[v]||d[v]||o;return n?a.createElement(f,i(i({ref:t},u),{},{components:n})):a.createElement(f,i({ref:t},u))}));function f(e,t){var n=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var o=n.length,i=new Array(o);i[0]=v;var l={};for(var s in t)hasOwnProperty.call(t,s)&&(l[s]=t[s]);l.originalType=e,l[c]="string"==typeof e?e:r,i[1]=l;for(var p=2;p<o;p++)i[p]=n[p];return a.createElement.apply(null,i)}return a.createElement.apply(null,n)}v.displayName="MDXCreateElement"},9123:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>s,contentTitle:()=>i,default:()=>c,frontMatter:()=>o,metadata:()=>l,toc:()=>p});var a=n(7462),r=(n(7294),n(3905));const o={id:"how-to-use",title:"DevOopsJavaPlugin - How to Use",sidebar_label:"How to Use"},i=void 0,l={unversionedId:"java-plugin/how-to-use",id:"java-plugin/how-to-use",title:"DevOopsJavaPlugin - How to Use",description:"DevOopsJavaPlugin",source:"@site/docs/java-plugin/how-to-use.md",sourceDirName:"java-plugin",slug:"/java-plugin/how-to-use",permalink:"/docs/java-plugin/how-to-use",draft:!1,tags:[],version:"current",frontMatter:{id:"how-to-use",title:"DevOopsJavaPlugin - How to Use",sidebar_label:"How to Use"},sidebar:"docsSidebar",previous:{title:"How to Use",permalink:"/docs/scala-plugin/how-to-use"}},s={},p=[{value:"DevOopsJavaPlugin",id:"devoopsjavaplugin",level:2}],u={toc:p};function c(e){let{components:t,...n}=e;return(0,r.kt)("wrapper",(0,a.Z)({},u,n,{components:t,mdxType:"MDXLayout"}),(0,r.kt)("h2",{id:"devoopsjavaplugin"},"DevOopsJavaPlugin"),(0,r.kt)("p",null,"To use sbt-devoops for a Java project, add the following line to ",(0,r.kt)("inlineCode",{parentName:"p"},"build.sbt")),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-sbt"},"enablePlugins(DevOopsJavaPlugin)\n")),(0,r.kt)("p",null,"It has"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("inlineCode",{parentName:"li"},"javaVersion")," setting"),(0,r.kt)("li",{parentName:"ul"},"set ",(0,r.kt)("inlineCode",{parentName:"li"},"crossPaths")," and ",(0,r.kt)("inlineCode",{parentName:"li"},"autoScalaLibrary")," to false"),(0,r.kt)("li",{parentName:"ul"},"default ",(0,r.kt)("inlineCode",{parentName:"li"},"javacOptions"),": ",(0,r.kt)("inlineCode",{parentName:"li"},"-source")," and ",(0,r.kt)("inlineCode",{parentName:"li"},"-target")," set to the ",(0,r.kt)("inlineCode",{parentName:"li"},"javaVersion")," above, ",(0,r.kt)("inlineCode",{parentName:"li"},"-Xlint:unchecked"),", and set ",(0,r.kt)("inlineCode",{parentName:"li"},"-encoding")," to ",(0,r.kt)("inlineCode",{parentName:"li"},"UTF-8")," ")),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'  override lazy val projectSettings: Seq[Setting[_]] = Seq(\n    javaVersion := "1.8", // 1.8 is default if not specified otherwise.\n    crossPaths := false,\n    autoScalaLibrary := false,\n    javacOptions ++= Seq(\n      "-source", javaVersion.value,\n      "-target", javaVersion.value,\n      "-Xlint:unchecked",\n      "-encoding", "UTF-8"\n    )\n  )\n')))}c.isMDXComponent=!0}}]);