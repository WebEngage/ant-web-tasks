## Introduction ##
  1. [What is property injection?](#What_is_property_injection?.md)
  1. [Attributes and Nested Elements](#Attributes_and_Nested_Elements.md)
  1. [Inject Samples](#Inject_Samples.md)

## What is property injection? ##
Simply put, an attempt to resolve ANT build time properties names in specified files and replace these with their corresponding values.
<br />
For an example, lets say you have a file
WEB-INF/some-application-property.xml which has this content -
```
<bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource">
  <property name="driverClassName">
    <value>com.mysql.jdbc.Driver</value>
  </property>
  <property name="url">
    <value>jdbc:mysql://${MYSQL.HOST}:${MYSQL.PORT}/${MYSQL.SCHEMA}</value>
  </property>
  <property name="username">
    <value>${MYSQL.USER}</value>
  </property>
  <property name="password">
    <value>${MYSQL.PASSWORD}</value>
  </property>
</bean>
```
${MYSQL.USER} and the likes are nothing but fancy placeholders. If there is a build property called "MYSQL.USER" defined in Ant, then this token will get replaced with the corresponding value.
<br />
While performing your WAR operation, you can instruct `<powerWar>` to `inject` the real values in this file as underneath
```
<taskdef name="powerWar" classname="com.avlesh.antwebtasks.war.PowerWAR" classpath="/path/to/ant-web-tasks.jar"/>
<target name="my-war-target">
  <powerWar destfile="distribution/myapp.war" webxml="web/WEB-INF/web.xml" compress="true">
    <!-- 
     Bare minimum usage of the injector. 
     For other possible attributes, go here
    -->
    <inject verbose="on">
      <fileset dir="web>
        <include name="WEB-INF/some-application-property.xml"/>
      </fileset>
    </inject>

    <classes dir="${build.dir}/classes"/>
    <lib dir="${lib.dir}">
      <include name="*.jar" />
    </lib>
    <fileset dir="web">
      <exclude name="**/web.xml" />
    </fileset>
  </powerWar>
</target>
```
The best part with PowerWAR's inject is that it does not alter the state of your file; meaning your file does not get modified. Since PowerWAR is an extension of the War task, which essentially extends an archive task, PowerWAR has access to the archiving operation. When the WAR task tries to archive your file, PowerWAR kick in tries to find a replaceable property in the file. If it finds one, the file's InputStream is modified (after replacing the property with its corresponding value) and sent to the archive operation.

## Attributes and Nested Elements ##
<table cellpadding='10' cellspacing='0' border='1' width='800'>
<blockquote><tr>
<blockquote><td width='100' align='left' valign='top'><b>Attribute/ Nested Element</b></td>
<td width='500' align='left' valign='top'><b>Description</b></td>
<td width='200' align='left' valign='top'><b>Remarks</b></td>
</blockquote></tr>
<tr>
<blockquote><td width='100' align='left' valign='top'><code>fileset</code></td>
<td width='500' align='left' valign='top'><code>&lt;fileset&gt;</code> is a nested inner element for <code>inject</code> inside the <code>powerWar</code> task. Using this, you can specify group of files to which injection should be applied. <code>&lt;fileset&gt;</code> is a core Ant data-type. Find more details on using fileset here - <a href='http://ant.apache.org/manual/CoreTypes/fileset.html'>http://ant.apache.org/manual/CoreTypes/fileset.html</a>
<br /><br />Here's an example of applying injection to all ".xml" and ".properties" file inside the "web" directory minus the "web.xml" file.<br>
<pre><code>&lt;inject verbose="on"&gt;<br>
  &lt;fileset dir="web"&gt;<br>
    &lt;include name="**/*.xml"/&gt;<br>
    &lt;include name="**/*.properties"/&gt;<br>
    &lt;exclude name="**/web.xml" /&gt;<br>
  &lt;/fileset&gt;<br>
&lt;/inject&gt;<br>
</code></pre>
</blockquote></td>
<blockquote><td align='left' valign='top'>Default: <code>none</code>, applies the inject directive to all the files in project's base directory to be added to the war archive.<br /><br />Note: You can specify multiple <code>&lt;fileset&gt;</code> based selectors for a single <code>inject</code> element.<br>
</td>
</blockquote></tr>
<tr>
<blockquote><td width='100' align='left' valign='top'><code>patternPrefix</code></td>
<td width='500' align='left' valign='top'>The injector replaces all ${BUILD.PROPERTY.NAME} style properties by default. However, you may choose to have different patterns for these property names in your files.<br /><br />For example, if you choose to keep the tokens in your file like this -<br>
<pre><code>&lt;bean id="foo"&gt;<br>
  &lt;property name="dbHost"&gt;<br>
    &lt;value&gt;@__MYSQL.HOST__@&lt;/value&gt;<br>
  &lt;/property&gt;<br>
&lt;/bean&gt;<br>
</code></pre>
your <code>patternPrefix</code> attribute should be specified as "<code>@__</code>"</td>
</blockquote><td width='200' align='left' valign='top'>Default: ${</td>
</tr>
<tr>
<blockquote><td width='100' align='left' valign='top'><code>patternSuffix</code></td>
<td width='500' align='left' valign='top'>Look for the description above. In the sample provided, <code>patternSuffix</code> should be specified as "<code>__@</code>"</td>
</blockquote><td width='200' align='left' valign='top'>Default: }</td>
</tr>
<tr>
<blockquote><td width='100' align='left' valign='top'><code>injectionPropertyFile</code></td>
<td width='500' align='left' valign='top'>The injector, by default, replaces all the properties which can be resolved via Ant's property map. Ant prepares a global list of all properties including System properties, properties declared by you during the build process and properties read from your build property files (imported using <code>&lt;property file="/my/build/file.properties"/&gt;</code>). The good news is that you can use any of these in your files.<br>
<br /><br />However, if this is not desired, and you wish to have a very separate and distinct list of properties to use, put them in a java properties files (key/value pairs) and pass the file name in this attribute.</td>
</blockquote><td width='200' align='left' valign='top'>Default: none<br /><br />If a specified file is not found (or could not be read), a <code>BuildException</code> is thrown.</td>
</tr>
<tr>
<blockquote><td width='100' align='left' valign='top'><code>modifyOriginal</code></td>
<td width='500' align='left' valign='top'>Boolean attribute. If set to true will modify the original file.</td>
</blockquote><td width='200' align='left' valign='top'>Default: <code>false</code></td>
</tr>
<tr>
<blockquote><td width='100' align='left' valign='top'><code>verbose</code></td>
<td width='500' align='left' valign='top'>Boolean attribute. If set to true spits out detailed information generated by the injector.</td>
</blockquote><td width='200' align='left' valign='top'>Default: <code>true</code></td>
</tr>
</table></blockquote>

## Inject Samples ##
<b>Inject any build property in any file</b>
<br />This is the most minimal (and also the most time consuming) usage of the `<inject>` directive inside `<powerWar>` task.
```
<taskdef name="powerWar" classname="com.avlesh.antwebtasks.war.PowerWAR" classpath="/path/to/ant-web-tasks.jar"/>
<target name="my-war-target">
  <powerWar destfile="distribution/myapp.war" webxml="web/WEB-INF/web.xml" compress="true">
    <!-- 
       Replaces all ${BUILD.PROPERTY.NAME} occurrences in the files
       that are being archived by the underlying "war" target. 
       
       Note: In this usage, the original files are not modified. Only the file's
       InputStream is changed.
    -->
    <inject verbose="on"/>

    <classes dir="${build.dir}/classes"/>
    <lib dir="${lib.dir}">
      <include name="*.jar" />
    </lib>
    <fileset dir="web">
      <exclude name="**/web.xml" />
    </fileset>
  </powerWar>
</target>
```
<br />
<b>Inject any build property in a group of files</b>
<br />This a more efficient usage of `<inject>`. You can use Ant's core data-type `<fileset>` to specify the file groups. More on `<fileset>` [here](http://ant.apache.org/manual/CoreTypes/fileset.html).
```
<taskdef name="powerWar" classname="com.avlesh.antwebtasks.war.PowerWAR" classpath="/path/to/ant-web-tasks.jar"/>
<target name="my-war-target">
  <powerWar destfile="distribution/myapp.war" webxml="web/WEB-INF/web.xml" compress="true">
    <!-- 
       Replaces all ${BUILD.PROPERTY.NAME} inside all ".xml" and ".properties"
       files in the "web" directory minus the "web.xml" file. 
       
       Note:
       1. You can specify multiple <fileset> elements inside a single inject. 
       2. In this usage, the original files are not modified. Only the file's
          InputStream is changed.
    -->
    <inject verbose="on">
      <fileset dir="web">
        <include name="**/*.xml"/>
        <include name="**/*.properties"/>
        <exclude name="**/web.xml" />
      </fileset>
    </inject>

    <classes dir="${build.dir}/classes"/>
    <lib dir="${lib.dir}">
      <include name="*.jar" />
    </lib>
    <fileset dir="web">
      <exclude name="**/web.xml" />
    </fileset>
  </powerWar>
</target>
```
<br />
<b>Inject pre-defined properties in a group of files</b>
<br />The injector, by default, replaces all the properties which can be resolved via Ant's property map. Ant prepares a global list of all properties including System properties, properties declared by you during the build process and properties read from your build property files (imported using `<property file="/my/build/file.properties"/>`). The good news is that you can use any of these in your files.
<br /><br />However, if this is not desired, and you wish to have a very separate and distinct list of properties to use, put them in a java properties files (key/value pairs) and pass the file name in this attribute.
<br />
```
<taskdef name="powerWar" classname="com.avlesh.antwebtasks.war.PowerWAR" classpath="/path/to/ant-web-tasks.jar"/>
<target name="my-war-target">
  <powerWar destfile="distribution/myapp.war" webxml="web/WEB-INF/web.xml" compress="true">
    <!-- 
       Replaces all ${BUILD.PROPERTY.NAME} 
       only specified in the "inject.properties" file. 
    -->
    <inject verbose="on" injectionPropertyFile="inject.properties">
      <fileset dir="web">
        <include name="**/*.xml"/>
        <include name="**/*.properties"/>
        <exclude name="**/web.xml" />
      </fileset>
    </inject>

    <classes dir="${build.dir}/classes"/>
    <lib dir="${lib.dir}">
      <include name="*.jar" />
    </lib>
    <fileset dir="web">
      <exclude name="**/web.xml" />
    </fileset>
  </powerWar>
</target>
```
<br />
<b>Inject any build property and modify the original file too</b>
<br />Beware! Be sure that you need this. When using `<inject>` inside the `powerWar` task, you might not need to turn on this attribute.
```
<taskdef name="powerWar" classname="com.avlesh.antwebtasks.war.PowerWAR" classpath="/path/to/ant-web-tasks.jar"/>
<target name="my-war-target">
  <powerWar destfile="distribution/myapp.war" webxml="web/WEB-INF/web.xml" compress="true">
    <!-- In this usage, the original files are modified. -->
    <inject verbose="on" modifyOriginal="true">
      <fileset dir="web">
        <include name="**/*.xml"/>
        <include name="**/*.properties"/>
        <exclude name="**/web.xml" />
      </fileset>
    </inject>

    <classes dir="${build.dir}/classes"/>
    <lib dir="${lib.dir}">
      <include name="*.jar" />
    </lib>
    <fileset dir="web">
      <exclude name="**/web.xml" />
    </fileset>
  </powerWar>
</target>
```
<b>Inject any build property present in files in a format other than ${PROPERTY.NAME}</b>
<br />It is quite possible that your application files might not have the property names in the ${BUILD.PROPERTY.NAME} format. In this case, you can use the `patternPrefix` and `patternSuffix` attributes inside `<inject>` directive as follows
```
<taskdef name="powerWar" classname="com.avlesh.antwebtasks.war.PowerWAR" classpath="/path/to/ant-web-tasks.jar"/>
<target name="my-war-target">
  <powerWar destfile="distribution/myapp.war" webxml="web/WEB-INF/web.xml" compress="true">
    <!--
      Replaces all occurrences of "@__BUILD.PROPERTY.NAME__@" in the files
      to be archived by the underlying "war" task.
    -->
    <inject patternPrefix="@__" patternSuffix="__@"/>

    <classes dir="${build.dir}/classes"/>
    <lib dir="${lib.dir}">
      <include name="*.jar" />
    </lib>
    <fileset dir="web">
      <exclude name="**/web.xml" />
    </fileset>
  </powerWar>
</target>
```