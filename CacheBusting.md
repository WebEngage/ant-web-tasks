## Introduction ##
  1. [What is cache busting?](#What_is_cache_busting?.md)
  1. [Cache Busting Attributes](#Cache_Busting_Attributes.md)
  1. [Cache Busting Nested Elements](#Cache_Busting_Nested_Elements.md)
  1. [Cache Busting Samples](#Cache_Busting_Samples.md)

## What is cache busting? ##
Cache in this context is always HTTP Cache. HTTP Cache comes as a huge boon for performance. Clients (Browsers) typically store a copy of the HTTP Response and serve it from their caches unless instructed otherwise. In typical scenarios, this works very well. However, you run into a problem when you want to invalidate this cache. There are very few ways to achieve this. Cache Busting is one of the solutions to this problem in which a random string is appended to the HTTP Request URL, thereby "faking" an altogether new URL. The browser is forced to make a request as the URL itself has changed.

The PowerWAR task of this Ant contribution lets you achieve this from the comfort of your build.xml file. Underneath is a simple example -
```
<taskdef name="powerWar" classname="com.avlesh.antwebtasks.war.PowerWAR" classpath="/path/to/ant-web-tasks.jar"/>
<target name="my-war-target">
  <powerWar destfile="distribution/myapp.war" webxml="web/WEB-INF/web.xml" compress="true">

    <cacheBuster verbose="on" versionFile="version.txt">
      <fileset dir="web"/>
      <rule from="/mysite/css/(.*).css" 
              to="/mysite/css/(.*).css?random=%{version-file-txt}"/>
    </cacheBuster>
    
    ...
  </powerWar>
</target>
```

### Cache Busting Attributes ###
<table cellpadding='10' cellspacing='0' border='1' width='800'>
<blockquote><tr>
<blockquote><td width='100' align='left' valign='top'><b>Attribute</b></td>
<td width='500' align='left' valign='top'><b>Description</b></td>
<td width='200' align='left' valign='top'><b>Remarks</b></td>
</blockquote></tr>
<tr>
<blockquote><td width='100' align='left' valign='top'><code>versionFile</code></td>
<td width='500' align='left' valign='top'>A file which is supposed to store the version (or revision or build number) for the project. The <code>cacheBuster</code> simply picks up this value and populates the replacement variable <code> %{version-file-txt}. </code>
<br /><br />
This file can be a java properties file too. Look at <code>versionPropertyKey</code> for details.</td>
<td align='left' valign='top'>Default: <code>none</code><br />Mandatory.</td>
</blockquote></tr>
<tr>
<blockquote><td width='100' align='left' valign='top'><code>createVersionFileIfNotExists</code></td>
<td width='500' align='left' valign='top'>When set to <code>true</code>, creates the <code>versionFile</code> if it does not exist. Starting version number in the file is set to 1.</td>
<td align='left' valign='top'>Default: <code>true</code></td>
</blockquote></tr>
<tr>
<blockquote><td width='100' align='left' valign='top'><code>autoIncrementVersionNumber</code></td>
<td width='500' align='left' valign='top'>You may want to use this attribute, when you are not managing the version file yourself. When set to <code>true</code>, the <code>&lt;cacheBuster&gt;</code> increments the value of version number in this file everytime it is called.</td>
<td align='left' valign='top'>Default: <code>false</code></td>
</blockquote></tr>
<tr>
<blockquote><td width='100' align='left' valign='top'><code>versionPropertyKey</code></td>
<td width='500' align='left' valign='top'>If you were managing the version file yourself, chances are that your <code>versionFile</code> is a java properties file. Use this attribute to pass in the key name of the property against which the version number is stored.<br /><br />
Eaxmple:<br>
<pre><code>&lt;!-- Your java properties style version file --&gt;<br>
build.date=Mon Aug 31 00:31:33 IST 2009<br>
build.version=444<br>
build.user=avlesh<br>
<br>
&lt;!-- Your corresponding &lt;cacheBuster&gt; usage --&gt;<br>
&lt;cacheBuster versionFile="my-file.properties" <br>
             versionPropertyKey="build.version"&gt;<br>
  &lt;fileset dir="web"/&gt;<br>
  ...<br>
&lt;/cacheBuster&gt;<br>
</code></pre>
</td>
<td align='left' valign='top'>Default: <code>none</code></td>
</blockquote></tr>
<tr>
<blockquote><td width='100' align='left' valign='top'><code>verbose</code></td>
<td width='500' align='left' valign='top'>See plenty of debug and info level messages on the Ant output stream.</td>
<td align='left' valign='top'>Default: <code>true</code></td>
</blockquote></tr>
<tr>
<blockquote><td width='100' align='left' valign='top'><code>cacheBusterPreferencesFile</code></td>
<td width='500' align='left' valign='top'>When you want to use a file's last modified timestamp to apply cache busting rules, the <code>&lt;cacheBuster&gt;</code> tries to maintain the state such files in every run. This attribute is not required otherwise.</td>
<td align='left' valign='top'>Default: <code>.cache-buster.pref</code><br /><br />This file is saved in the project's base directory.</td>
</blockquote></tr>
<tr>
<blockquote><td width='100' align='left' valign='top'><code>modifyOriginal</code></td>
<td width='500' align='left' valign='top'>When set to <code>true</code>, modifies the original content of the fileset (or the file group) on which cache busting rules are applied.<br /><br />
WARNING: You may seldom want to use this inside the <code>&lt;powerWar&gt;</code> task.</td>
<td align='left' valign='top'>Default: <code>false</code></td>
</blockquote></tr>
</table>
<br />
<hr />
<h3>Cache Busting Nested Elements</h3>
<table cellpadding='10' cellspacing='0' border='1' width='800'>
<tr>
<blockquote><td width='100' align='left' valign='top'><b>Nested Element</b></td>
<td width='500' align='left' valign='top'><b>Description</b></td>
<td width='200' align='left' valign='top'><b>Remarks</b></td>
</blockquote></tr>
<tr>
<blockquote><td width='100' align='left' valign='top'><code>fileset</code></td>
<td width='500' align='left' valign='top'>A group of files to which you want to apply the cache busting rules to. <br />You can specify many fileset elements for a <code>&lt;cacheBuster&gt;</code></td>
<td width='200' align='left' valign='top'>Default: <code>none</code><br /><br />In this case all the files being archived by the underlying "war" target go through cache busting.</td>
</blockquote></tr>
<tr>
<blockquote><td width='100' align='left' valign='top'><code>rule</code></td>
<td width='500' align='left' valign='top'>A single cache busting rule. You can specify more than one rule.<br />Rule attributes:<br />
<code>from (Required)</code>: The URL patterns which need to be replace in your <code>cacheBuster </code> fileset(s).<br />
<code>to (Required)</code>: The URL which the <code>from</code> needs to be converted to.<br />
<code>file (Optional)</code>: The file for which the rule is being applied. If specified, an additional file lastmodied time check is done. If the file is not modified between versions, the <code>to</code> pattern conversion takes the last version number (since the file has not been modified) into account.<br /><br />
Examples:<br />
<pre><code>&lt;rule from="/css/(.*).css" <br>
      to="/css/$1.css?version=%{version-file-txt}"/&gt;<br>
<br>
&lt;rule from="/js/massive.js" <br>
      to="/js/massive.js-%{version-file-txt}.js" <br>
      file="web/static-files/js/massive.js"/&gt;<br>
</code></pre>
</td>
<td width='200' align='left' valign='top'>Default: <code>none</code>.</td>
</blockquote></tr>
</table>
<br />
<hr />
<h3>Cache Busting Samples</h3>
<b>Apply cache-busting to all Javascript links in all JSP's</b>
<br />Scenario: Your jsp's reference a lot of Javascript files via the <code>&lt;script&gt;</code>'s <code>src</code> attribute. The attribute values are URL's matching the pattern <code>/static-content/js/(.*).js</code>. The easiest way to bust cache is to change this URL everytime you deploy a new build. As the client (browser) gets to see a new URL for the javascript files, it will be forced to send a new request. After receiving the response, unless instructed otherwise, browser would cache the contents of the file and keep serving it from its cache (unless expired) until you change the URL again in the next build. Look at the sample usage below -<br>
<pre><code>&lt;taskdef name="powerWar" classname="com.avlesh.antwebtasks.war.PowerWAR" classpath="/path/to/ant-web-tasks.jar"/&gt;<br>
&lt;target name="my-war-target"&gt;<br>
  &lt;powerWar destfile="distribution/myapp.war" webxml="web/WEB-INF/web.xml" compress="true"&gt;<br>
<br>
    &lt;!--<br>
       Most minimal usage of the cacheBuster feature in the PowerWAR task.<br>
       <br>
       The attribute "versionFile" is mandatory. cacheBuster picks up the<br>
       content of this file and uses this as "revision number". The <br>
       replacement attribute, %{version-file-txt}, is populated with this<br>
       value and can be used anywhere in the "to" attribute of the "rule" <br>
    --&gt;<br>
    &lt;cacheBuster verbose="on" versionFile="someFile.txt"&gt;<br>
<br>
      &lt;!--<br>
        Fileset(s) to apply all the rules on. This is the file group in <br>
        which pattern's matching a rule's "from" are found and replaced<br>
        by their corresponding "to's"<br>
      <br>
        The example below is an instruction to find the URL patterns in<br>
        all ".jsp" files inside the "web" directory.<br>
      --&gt;<br>
      &lt;fileset dir="web"&gt;<br>
        &lt;include name="**/*.jsp"/&gt;<br>
      &lt;/fileset&gt;<br>
      <br>
      &lt;!-- <br>
        Cache busting rule's to apply to the files above. In the example <br>
        below all a URL "/static-content/js/my-file.js" would be deployed<br>
        as "/static-content/js/my-file.js?version=5" on the 5th build.<br>
      --&gt;<br>
      &lt;rule from="/static-content/js/(.*).js" <br>
            to="/static-content/js/$1.js?version=%{version-file-txt}"/&gt;<br>
    &lt;/cacheBuster&gt;<br>
    <br>
    ...<br>
  &lt;/powerWar&gt;<br>
&lt;/target&gt;<br>
</code></pre>
<br /><b>Apply cache-busting to all Javascript and CSS links in all JSP's</b>
<pre><code>&lt;taskdef name="powerWar" classname="com.avlesh.antwebtasks.war.PowerWAR" classpath="/path/to/ant-web-tasks.jar"/&gt;<br>
&lt;target name="my-war-target"&gt;<br>
  &lt;powerWar destfile="distribution/myapp.war" webxml="web/WEB-INF/web.xml" compress="true"&gt;<br>
    &lt;cacheBuster verbose="on" versionFile="someFile.txt"&gt;<br>
      &lt;fileset dir="web"&gt;<br>
        &lt;include name="**/*.jsp"/&gt;<br>
      &lt;/fileset&gt;<br>
      &lt;rule from="/static-content/js/(.*).js" <br>
            to="/static-content/js/$1.js?version=%{version-file-txt}"/&gt;<br>
      &lt;rule from="/css/(.*).css" <br>
            to="/css/$1.js?version=%{version-file-txt}"/&gt;<br>
    &lt;/cacheBuster&gt;<br>
    <br>
    ...<br>
  &lt;/powerWar&gt;<br>
&lt;/target&gt;<br>
</code></pre>
<br /><b>Apply cache-busting all Javascript only when modified, CSS always</b>
<pre><code>&lt;taskdef name="powerWar" classname="com.avlesh.antwebtasks.war.PowerWAR" classpath="/path/to/ant-web-tasks.jar"/&gt;<br>
&lt;target name="my-war-target"&gt;<br>
  &lt;powerWar destfile="distribution/myapp.war" webxml="web/WEB-INF/web.xml" compress="true"&gt;<br>
    &lt;cacheBuster verbose="on" versionFile="someFile.txt"&gt;<br>
      &lt;fileset dir="web"&gt;<br>
        &lt;include name="**/*.jsp"/&gt;<br>
      &lt;/fileset&gt;<br>
      &lt;rule from="/static-content/js/myFile.js" <br>
            to="/static-content/js/myFile.js?version=%{version-file-txt}"<br>
            file="web/static-files/js/my-file.js"/&gt;<br>
      &lt;rule from="/css/(.*).css" <br>
            to="/css/$1.js?version=%{version-file-txt}"/&gt;<br>
    &lt;/cacheBuster&gt;<br>
    <br>
    ...<br>
  &lt;/powerWar&gt;<br>
&lt;/target&gt;<br>
</code></pre>
More elaborate examples coming soon ...