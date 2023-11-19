"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[220],{3905:(e,n,t)=>{t.d(n,{Zo:()=>p,kt:()=>g});var o=t(7294);function r(e,n,t){return n in e?Object.defineProperty(e,n,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[n]=t,e}function i(e,n){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);n&&(o=o.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),t.push.apply(t,o)}return t}function a(e){for(var n=1;n<arguments.length;n++){var t=null!=arguments[n]?arguments[n]:{};n%2?i(Object(t),!0).forEach((function(n){r(e,n,t[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):i(Object(t)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(t,n))}))}return e}function l(e,n){if(null==e)return{};var t,o,r=function(e,n){if(null==e)return{};var t,o,r={},i=Object.keys(e);for(o=0;o<i.length;o++)t=i[o],n.indexOf(t)>=0||(r[t]=e[t]);return r}(e,n);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(o=0;o<i.length;o++)t=i[o],n.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(r[t]=e[t])}return r}var s=o.createContext({}),c=function(e){var n=o.useContext(s),t=n;return e&&(t="function"==typeof e?e(n):a(a({},n),e)),t},p=function(e){var n=c(e.components);return o.createElement(s.Provider,{value:n},e.children)},u="mdxType",f={inlineCode:"code",wrapper:function(e){var n=e.children;return o.createElement(o.Fragment,{},n)}},d=o.forwardRef((function(e,n){var t=e.components,r=e.mdxType,i=e.originalType,s=e.parentName,p=l(e,["components","mdxType","originalType","parentName"]),u=c(t),d=r,g=u["".concat(s,".").concat(d)]||u[d]||f[d]||i;return t?o.createElement(g,a(a({ref:n},p),{},{components:t})):o.createElement(g,a({ref:n},p))}));function g(e,n){var t=arguments,r=n&&n.mdxType;if("string"==typeof e||r){var i=t.length,a=new Array(i);a[0]=d;var l={};for(var s in n)hasOwnProperty.call(n,s)&&(l[s]=n[s]);l.originalType=e,l[u]="string"==typeof e?e:r,a[1]=l;for(var c=2;c<i;c++)a[c]=t[c];return o.createElement.apply(null,a)}return o.createElement.apply(null,t)}d.displayName="MDXCreateElement"},2009:(e,n,t)=>{t.r(n),t.d(n,{assets:()=>s,contentTitle:()=>a,default:()=>u,frontMatter:()=>i,metadata:()=>l,toc:()=>c});var o=t(7462),r=(t(7294),t(3905));const i={id:"how-to-use",title:"DevOopsScalaPlugin - How to Use",sidebar_label:"How to Use"},a=void 0,l={unversionedId:"scala-plugin/how-to-use",id:"version-2.24.1/scala-plugin/how-to-use",title:"DevOopsScalaPlugin - How to Use",description:"DevOopsScalaPlugin",source:"@site/versioned_docs/version-2.24.1/scala-plugin/how-to-use.md",sourceDirName:"scala-plugin",slug:"/scala-plugin/how-to-use",permalink:"/docs/2.24.1/scala-plugin/how-to-use",draft:!1,tags:[],version:"2.24.1",frontMatter:{id:"how-to-use",title:"DevOopsScalaPlugin - How to Use",sidebar_label:"How to Use"},sidebar:"version-2.24.1/docs",previous:{title:"Examples",permalink:"/docs/2.24.1/gh-release-plugin/examples"},next:{title:"How to Use",permalink:"/docs/2.24.1/java-plugin/how-to-use"}},s={},c=[{value:"DevOopsScalaPlugin",id:"devoopsscalaplugin",level:2}],p={toc:c};function u(e){let{components:n,...t}=e;return(0,r.kt)("wrapper",(0,o.Z)({},p,t,{components:n,mdxType:"MDXLayout"}),(0,r.kt)("h2",{id:"devoopsscalaplugin"},"DevOopsScalaPlugin"),(0,r.kt)("p",null,"This plugin is for a Scala project and is enabled by default.\nIt has quite a few ",(0,r.kt)("inlineCode",{parentName:"p"},"scalacOptions")," added. If you'd like to see what those options are, simply run "),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-sbt"},"show scalacOptions\n")),(0,r.kt)("p",null,"e.g.)"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre"},"[info] * -language:higherKinds\n[info] * -encoding\n[info] * UTF-8\n[info] * -deprecation\n[info] * -unchecked\n[info] * -feature\n[info] * -Xfatal-warnings\n[info] * -Wdead-code\n[info] * -Wvalue-discard\n[info] * -Xlint:adapted-args\n[info] * -Xlint:inaccessible\n[info] * -Xlint:nullary-override\n[info] * -Wnumeric-widen\n[info] * -Wunused:imports\n[info] * -Xlint:nullary-unit\n[info] * -Xlint:infer-any\n[info] * -Xlint:missing-interpolator\n[info] * -Xlint:doc-detached\n[info] * -Xlint:private-shadow\n[info] * -Xlint:type-parameter-shadow\n[info] * -Xlint:poly-implicit-overload\n[info] * -Xlint:option-implicit\n[info] * -Xlint:delayedinit-select\n[info] * -Xlint:package-object-classes\n[info] * -Xlint:stars-align\n[info] * -Xlint:constant\n")))}u.isMDXComponent=!0}}]);