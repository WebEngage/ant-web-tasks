<table cellpadding='0' cellspacing='0' border='0'>
<blockquote><tr>
<blockquote><td width='200' valign='top'><b>Quick links</b>
</blockquote><ol><li><a href='http://code.google.com/p/ant-web-tasks/wiki/PowerWar'>PowerWAR</a>
</li><li><a href='http://code.google.com/p/ant-web-tasks/wiki/PropertyInjection'>PropertyInjection</a>
</li><li><a href='http://code.google.com/p/ant-web-tasks/wiki/CacheBusting'>CacheBusting</a>
</li><li><a href='http://code.google.com/p/ant-web-tasks/downloads/list'>Download</a>
</li><li><a href='#Coming_Soon.md'>Coming Soon</a>
</li></ol><blockquote></td>
<td valign='top'>
<h1>AntWebTasks</h1>
AntWebTasks is a collection of powerful Ant tasks and extensions to address some of the most common problems in web application deployment. More specifically, AntWebTasks adds a lot of power to the existing WAR task thereby making it capable of modifying the InputStream of files before archiving it into the application's war. This capability, is in turn used to implement "property injection" and "cache-busting" during the build process.</td>
</blockquote></tr>
</table>
A few things that you can use this Ant contribution for:<br>
<ol><li><b>Property "injection"</b>: Like any other web application, you too have a lot of files that contain parametrized tokens, for lets say database related properties such as db-host, db-user, db-password etc. It is natural not to hard code these values in those application files. So far so good. This is where the problem begins. You would now do some ugly looking token replacement in these files as a part of our build process. Moreover, you have to make sure to do this before the war task is performed and once it is done, you don't have to forget to revert these files back to their original state. Huh!!! Enough! Not any more ... <br /><br /><table cellpadding='0' cellspacing='0' border='0'><tr><td><b><a href='#Quick_Samples_(Property_Injection).md'>Quick Samples</a></b></td><td width='20'></td><td><b><a href='http://code.google.com/p/ant-web-tasks/wiki/PropertyInjection'>Detailed Guide</a></b></td></tr></table><br />
</li><li><b>Cache busting</b>: HTTP Cache comes as a huge boon for performance. Clients (Browsers) typically store a copy of the HTTP Response and serve it from their caches unless instructed otherwise. In typical scenarios, this works very well. However, as webmasters and developers you have always faced one huge challenge - How to force clients to reload the Javacript/CSS files that were just modified and deployed in your web-application? <a href='http://public.yahoo.com/bfrance/radwin/talks/http-caching.htm'>HTTP Cache-Busting</a> is a well known technique and AntWebTasks offers a simple, yet powerful, implementation of the idea. Needless to mention, this solution is integrated with your build process and hence you need zero code changes in your application.<br /><br /><table cellpadding='0' cellspacing='0' border='0'><tr><td><b><a href='#Quick_Samples_(Cache_Busting).md'>Quick Samples</a></b></td><td width='20'></td><td><b><a href='http://code.google.com/p/ant-web-tasks/wiki/CacheBusting'>Detailed Guide</a></b></td></tr></table>
<hr />
<br />
<h1>Quick Samples (Property Injection)</h1>
If you are learn by example kind, here are some quick samples on how to bring it all together and get AntWebTasks up and running. First, <a href='http://code.google.com/p/ant-web-tasks/downloads/list'>download</a> the latest binary. There are no third-party dependencies other than the core Ant classes, so no need to worry.<br>
<br /><br />
Define a <code>&lt;powerWar&gt;</code> task and change the reference(s) of <code>&lt;war&gt;</code> task to <code>&lt;powerWar&gt;</code> task in your <code>build.xml</code> file as follows:<br>
<pre><code>&lt;taskdef name="powerWar" classname="com.avlesh.antwebtasks.war.PowerWAR" classpath="/path/to/ant-web-tasks.jar"/&gt;<br>
&lt;target name="my-war-target"&gt;<br>
  &lt;powerWar destfile="distribution/myapp.war" webxml="web/WEB-INF/web.xml" compress="true"&gt;<br>
    &lt;classes dir="${build.dir}/classes"/&gt;<br>
    &lt;lib dir="${lib.dir}"&gt;<br>
      &lt;include name="*.jar" /&gt;<br>
    &lt;/lib&gt;<br>
    &lt;fileset dir="web"&gt;<br>
      &lt;exclude name="**/web.xml" /&gt;<br>
    &lt;/fileset&gt;<br>
  &lt;/powerWar&gt;<br>
&lt;/target&gt;<br>
</code></pre>
Bingo! If all of this works without glitches, then you are good to go. Doing <code>ant my-war-target</code> should work as usual for you, meaning it should create a "myapp.war" as it was doing earlier.<br>
<br /><br /><b>Inject any build property in any file</b>
<br />This is the most minimal (and also the most time consuming) usage of the <code>&lt;inject&gt;</code> directive inside <code>&lt;powerWar&gt;</code> task.<br>
<pre><code>&lt;taskdef name="powerWar" classname="com.avlesh.antwebtasks.war.PowerWAR" classpath="/path/to/ant-web-tasks.jar"/&gt;<br>
&lt;target name="my-war-target"&gt;<br>
  &lt;powerWar destfile="distribution/myapp.war" webxml="web/WEB-INF/web.xml" compress="true"&gt;<br>
    &lt;!-- <br>
       Replaces all ${BUILD.PROPERTY.NAME} occurrences in the files<br>
       that are being archived by the underlying "war" target. <br>
       <br>
       Note: In this usage, the original files are not modified. Only the file's<br>
       InputStream is changed.<br>
    --&gt;<br>
    &lt;inject verbose="on"/&gt;<br>
<br>
    &lt;classes dir="${build.dir}/classes"/&gt;<br>
    &lt;lib dir="${lib.dir}"&gt;<br>
      &lt;include name="*.jar" /&gt;<br>
    &lt;/lib&gt;<br>
    &lt;fileset dir="web"&gt;<br>
      &lt;exclude name="**/web.xml" /&gt;<br>
    &lt;/fileset&gt;<br>
  &lt;/powerWar&gt;<br>
&lt;/target&gt;<br>
</code></pre>
<br />
<b>Inject any build property in a group of files</b>
<br />This a more efficient usage of <code>&lt;inject&gt;</code>. You can use Ant's core data-type <code>&lt;fileset&gt;</code> to specify the file groups. More on <code>&lt;fileset&gt;</code> <a href='http://ant.apache.org/manual/CoreTypes/fileset.html'>here</a>.<br>
<pre><code>&lt;taskdef name="powerWar" classname="com.avlesh.antwebtasks.war.PowerWAR" classpath="/path/to/ant-web-tasks.jar"/&gt;<br>
&lt;target name="my-war-target"&gt;<br>
  &lt;powerWar destfile="distribution/myapp.war" webxml="web/WEB-INF/web.xml" compress="true"&gt;<br>
    &lt;!-- <br>
       Replaces all ${BUILD.PROPERTY.NAME} inside all ".xml" and ".properties"<br>
       files in the "web" directory minus the "web.xml" file. <br>
       <br>
       Note:<br>
       1. You can specify multiple &lt;fileset&gt; elements inside a single inject. <br>
       2. In this usage, the original files are not modified. Only the file's<br>
          InputStream is changed.<br>
    --&gt;<br>
    &lt;inject verbose="on"&gt;<br>
      &lt;fileset dir="web"&gt;<br>
        &lt;include name="**/*.xml"/&gt;<br>
        &lt;include name="**/*.properties"/&gt;<br>
        &lt;exclude name="**/web.xml" /&gt;<br>
      &lt;/fileset&gt;<br>
    &lt;/inject&gt;<br>
<br>
    &lt;classes dir="${build.dir}/classes"/&gt;<br>
    &lt;lib dir="${lib.dir}"&gt;<br>
      &lt;include name="*.jar" /&gt;<br>
    &lt;/lib&gt;<br>
    &lt;fileset dir="web"&gt;<br>
      &lt;exclude name="**/web.xml" /&gt;<br>
    &lt;/fileset&gt;<br>
  &lt;/powerWar&gt;<br>
&lt;/target&gt;<br>
</code></pre>
<br />
<b>Inject pre-defined properties in a group of files</b>
<br />The injector, by default, replaces all the properties which can be resolved via Ant's property map. Ant prepares a global list of all properties including System properties, properties declared by you during the build process and properties read from your build property files (imported using <code>&lt;property file="/my/build/file.properties"/&gt;</code>). The good news is that you can use any of these in your files.<br>
<br /><br />However, if this is not desired, and you wish to have a very separate and distinct list of properties to use, put them in a java properties files (key/value pairs) and pass the file name in this attribute.<br>
<br />
<pre><code>&lt;taskdef name="powerWar" classname="com.avlesh.antwebtasks.war.PowerWAR" classpath="/path/to/ant-web-tasks.jar"/&gt;<br>
&lt;target name="my-war-target"&gt;<br>
  &lt;powerWar destfile="distribution/myapp.war" webxml="web/WEB-INF/web.xml" compress="true"&gt;<br>
    &lt;!-- <br>
       Replaces all ${BUILD.PROPERTY.NAME} <br>
       only specified in the "inject.properties" file. <br>
    --&gt;<br>
    &lt;inject verbose="on" injectionPropertyFile="inject.properties"&gt;<br>
      &lt;fileset dir="web"&gt;<br>
        &lt;include name="**/*.xml"/&gt;<br>
        &lt;include name="**/*.properties"/&gt;<br>
        &lt;exclude name="**/web.xml" /&gt;<br>
      &lt;/fileset&gt;<br>
    &lt;/inject&gt;<br>
<br>
    &lt;classes dir="${build.dir}/classes"/&gt;<br>
    &lt;lib dir="${lib.dir}"&gt;<br>
      &lt;include name="*.jar" /&gt;<br>
    &lt;/lib&gt;<br>
    &lt;fileset dir="web"&gt;<br>
      &lt;exclude name="**/web.xml" /&gt;<br>
    &lt;/fileset&gt;<br>
  &lt;/powerWar&gt;<br>
&lt;/target&gt;<br>
</code></pre>
<br />
More <code>&lt;inject&gt;</code> <b><a href='http://code.google.com/p/ant-web-tasks/wiki/PropertyInjection'>attributes and samples</a></b>
<hr />
<br />
<h1>Quick Samples (Cache Busting)</h1>
First, take a look at the section <a href='#Quick_Samples_(Property_Injection).md'>above</a> on how to use AntWebTask with your build process.<br>
<br /><br />
Define a <code>&lt;powerWar&gt;</code> task and change the reference(s) of <code>&lt;war&gt;</code> task to <code>&lt;powerWar&gt;</code> task in your <code>build.xml</code> file as follows:<br>
<pre><code>&lt;taskdef name="powerWar" classname="com.avlesh.antwebtasks.war.PowerWAR" classpath="/path/to/ant-web-tasks.jar"/&gt;<br>
&lt;target name="my-war-target"&gt;<br>
  &lt;powerWar destfile="distribution/myapp.war" webxml="web/WEB-INF/web.xml" compress="true"&gt;<br>
    &lt;classes dir="${build.dir}/classes"/&gt;<br>
    &lt;lib dir="${lib.dir}"&gt;<br>
      &lt;include name="*.jar" /&gt;<br>
    &lt;/lib&gt;<br>
    &lt;fileset dir="web"&gt;<br>
      &lt;exclude name="**/web.xml" /&gt;<br>
    &lt;/fileset&gt;<br>
  &lt;/powerWar&gt;<br>
&lt;/target&gt;<br>
</code></pre>
<br /><br /><b>Apply cache-busting to all Javascript links in all JSP's</b>
<br />Scenario: Your jsp's reference a lot of Javascript files via the <code>&lt;script&gt;</code>'s <code>src</code> attribute. The attribute values are URL's matching the pattern <code>/static-content/js/(.*).js</code>. The easiest way to bust cache is to change this URL everytime you deploy a new build. As the client (browser) gets to see a new URL for the javascript files, it will be forced to send a new request. After receiving the response, unless instructed otherwise, browser would cache the contents of the file and keep serving it from its cache (unless expired) until you change the URL again in the next build. Look at the sample usage below -<br>
<pre><code>&lt;taskdef name="powerWar" classname="com.avlesh.antwebtasks.war.PowerWAR" classpath="/path/to/ant-web-tasks.jar"/&gt;<br>
&lt;target name="my-war-target"&gt;<br>
  &lt;powerWar destfile="distribution/myapp.war" webxml="web/WEB-INF/web.xml" compress="true"&gt;<br>
<br>
    &lt;!--<br>
       Most minimal usage of the cacheBuster feature in the PowerWAR task.<br>
       <br>
       The attribute "versionFile" is mandatory. cacheBuster picks up the<br>
       content of this file and uses this as "revision number". The <br>
       replacement attribute, %{version-file-txt}, is populated with this<br>
       value and can be used anywhere in the "to" attribute of the "rule" <br>
    --&gt;<br>
    &lt;cacheBuster verbose="on" versionFile="someFile.txt"&gt;<br>
<br>
      &lt;!--<br>
        Fileset(s) to apply all the rules on. This is the file group in <br>
        which pattern's matching a rule's "from" are found and replaced<br>
        by their corresponding "to's"<br>
      <br>
        The example below is an instruction to find the URL patterns in<br>
        all ".jsp" files inside the "web" directory.<br>
      --&gt;<br>
      &lt;fileset dir="web"&gt;<br>
        &lt;include name="**/*.jsp"/&gt;<br>
      &lt;/fileset&gt;<br>
      <br>
      &lt;!-- <br>
        Cache busting rule's to apply to the files above. In the example <br>
        below all a URL "/static-content/js/my-file.js" would be deployed<br>
        as "/static-content/js/my-file.js?version=5" on the 5th build.<br>
      --&gt;<br>
      &lt;rule from="/static-content/js/(.*).js" <br>
            to="/static-content/js/$1.js?version=%{version-file-txt}"/&gt;<br>
    &lt;/cacheBuster&gt;<br>
    <br>
    ...<br>
  &lt;/powerWar&gt;<br>
&lt;/target&gt;<br>
</code></pre>
More <code>&lt;cacheBuster&gt;</code> <b><a href='http://code.google.com/p/ant-web-tasks/wiki/CacheBusting'>attributes and samples</a></b>
<hr />
<br />
<h1>Coming Soon</h1>
In the coming versions, AntWebTasks would be adding a lot more. An indicative list underneath:<br>
</li></ol></blockquote>  1. JS-Minifier - This one will let you minify and obfuscate your Javascript files during build time.
  1. SpaceEater - Apply this to your "view" files, like jsp's, to remove all the unnecessary spaces, new lines, tabs etc as a part of the build process.