(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[643],{3905:function(e,n,t){"use strict";t.d(n,{Zo:function(){return u},kt:function(){return d}});var o=t(7294);function r(e,n,t){return n in e?Object.defineProperty(e,n,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[n]=t,e}function i(e,n){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);n&&(o=o.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),t.push.apply(t,o)}return t}function a(e){for(var n=1;n<arguments.length;n++){var t=null!=arguments[n]?arguments[n]:{};n%2?i(Object(t),!0).forEach((function(n){r(e,n,t[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):i(Object(t)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(t,n))}))}return e}function l(e,n){if(null==e)return{};var t,o,r=function(e,n){if(null==e)return{};var t,o,r={},i=Object.keys(e);for(o=0;o<i.length;o++)t=i[o],n.indexOf(t)>=0||(r[t]=e[t]);return r}(e,n);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(o=0;o<i.length;o++)t=i[o],n.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(r[t]=e[t])}return r}var c=o.createContext({}),s=function(e){var n=o.useContext(c),t=n;return e&&(t="function"==typeof e?e(n):a(a({},n),e)),t},u=function(e){var n=s(e.components);return o.createElement(c.Provider,{value:n},e.children)},p={inlineCode:"code",wrapper:function(e){var n=e.children;return o.createElement(o.Fragment,{},n)}},f=o.forwardRef((function(e,n){var t=e.components,r=e.mdxType,i=e.originalType,c=e.parentName,u=l(e,["components","mdxType","originalType","parentName"]),f=s(t),d=r,g=f["".concat(c,".").concat(d)]||f[d]||p[d]||i;return t?o.createElement(g,a(a({ref:n},u),{},{components:t})):o.createElement(g,a({ref:n},u))}));function d(e,n){var t=arguments,r=n&&n.mdxType;if("string"==typeof e||r){var i=t.length,a=new Array(i);a[0]=f;var l={};for(var c in n)hasOwnProperty.call(n,c)&&(l[c]=n[c]);l.originalType=e,l.mdxType="string"==typeof e?e:r,a[1]=l;for(var s=2;s<i;s++)a[s]=t[s];return o.createElement.apply(null,a)}return o.createElement.apply(null,t)}f.displayName="MDXCreateElement"},3940:function(e,n,t){"use strict";t.r(n),t.d(n,{frontMatter:function(){return l},contentTitle:function(){return c},metadata:function(){return s},toc:function(){return u},default:function(){return f}});var o=t(2122),r=t(9756),i=(t(7294),t(3905)),a=["components"],l={id:"how-to-use",title:"DevOopsScalaPlugin - How to Use",sidebar_label:"How to Use"},c=void 0,s={unversionedId:"scala-plugin/how-to-use",id:"version-2.5.0/scala-plugin/how-to-use",isDocsHomePage:!1,title:"DevOopsScalaPlugin - How to Use",description:"DevOopsScalaPlugin",source:"@site/versioned_docs/version-2.5.0/scala-plugin/how-to-use.md",sourceDirName:"scala-plugin",slug:"/scala-plugin/how-to-use",permalink:"/docs/2.5.0/scala-plugin/how-to-use",version:"2.5.0",frontMatter:{id:"how-to-use",title:"DevOopsScalaPlugin - How to Use",sidebar_label:"How to Use"},sidebar:"version-2.5.0/docs",previous:{title:"Examples",permalink:"/docs/2.5.0/gh-release-plugin/examples"},next:{title:"How to Use",permalink:"/docs/2.5.0/java-plugin/how-to-use"}},u=[{value:"DevOopsScalaPlugin",id:"devoopsscalaplugin",children:[]}],p={toc:u};function f(e){var n=e.components,t=(0,r.Z)(e,a);return(0,i.kt)("wrapper",(0,o.Z)({},p,t,{components:n,mdxType:"MDXLayout"}),(0,i.kt)("h2",{id:"devoopsscalaplugin"},"DevOopsScalaPlugin"),(0,i.kt)("p",null,"This plugin is for a Scala project and is enabled by default.\nIt has quite a few ",(0,i.kt)("inlineCode",{parentName:"p"},"scalacOptions")," added. If you'd like to see what those options are, simply run "),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-sbt"},"show scalacOptions\n")),(0,i.kt)("p",null,"e.g.)"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre"},"[info] * -language:higherKinds\n[info] * -encoding\n[info] * UTF-8\n[info] * -deprecation\n[info] * -unchecked\n[info] * -feature\n[info] * -Xfatal-warnings\n[info] * -Wdead-code\n[info] * -Wvalue-discard\n[info] * -Xlint:adapted-args\n[info] * -Xlint:inaccessible\n[info] * -Xlint:nullary-override\n[info] * -Wnumeric-widen\n[info] * -Wunused:imports\n[info] * -Xlint:nullary-unit\n[info] * -Xlint:infer-any\n[info] * -Xlint:missing-interpolator\n[info] * -Xlint:doc-detached\n[info] * -Xlint:private-shadow\n[info] * -Xlint:type-parameter-shadow\n[info] * -Xlint:poly-implicit-overload\n[info] * -Xlint:option-implicit\n[info] * -Xlint:delayedinit-select\n[info] * -Xlint:package-object-classes\n[info] * -Xlint:stars-align\n[info] * -Xlint:constant\n")))}f.isMDXComponent=!0}}]);