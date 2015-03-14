PowerWAR, as the name says :), is a powerful extension of Ant's [WAR task](http://ant.apache.org/manual/CoreTasks/war.html). Apart from everything that the original WAR task lets you do, PowerWAR adds capability to perform property-injection and cache-busting.
<br /><br />
To begin with, first download the latest binary, and change your WAR task's usage as below
```
<taskdef name="powerWar" classname="com.avlesh.antwebtasks.war.PowerWAR" classpath="/path/to/ant-web-tasks.jar"/>
<target name="my-war-target">
  <powerWar destfile="distribution/myapp.war" webxml="web/WEB-INF/web.xml" compress="true">
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
As you might have noticed, other than the task name nothing has really changed. Hmmm ... we are yet to discover the functionlities that `<powerWar>` has to offer in the subsequent sections. Lets move on.

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
The best part with PowerWAR's inject is that it does not alter the state of your file; meaning your file does not get modified. Since PowerWAR is an extension of the War task, which essentially extends an archive task, PowerWAR has access to the archiving operation. When the WAR task tries to archive your file, PowerWAR kicks in and tries to find a replaceable property in the file. If it finds one, the file's InputStream is modified (after replacing the property with its corresponding value) before being sent for the archive operation. <b><a href='PropertyInjection.md'>Injection attributes and samples</a></b>