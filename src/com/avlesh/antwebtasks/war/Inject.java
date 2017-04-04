/**
 * Copyright 2009 Avlesh Singh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.avlesh.antwebtasks.war;

import com.avlesh.antwebtasks.util.WebAntUtil;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Inject {
    protected Task caller;
    protected List<FileSet> fileSets = new ArrayList<FileSet>();
    protected boolean verbose = true;
    protected String patternPrefix = "${";
    protected String patternSuffix = "}";
    protected Project project;
    protected File injectionPropertyFile;
    protected boolean modifyOriginal = false;
    private List<String> filesToInject = new ArrayList<String>();
    private Map tokens;

    private Pattern pattern;


    public Inject(Project project) {
        this.project = project;
    }

    public void addFileset(FileSet fileSet) {
        this.fileSets.add(fileSet);
    }

    public FileSet createFileset() {
        FileSet fileSet = new FileSet();
        this.fileSets.add(fileSet);

        return fileSet;
    }

    public void init() {
        StringBuilder builder = new StringBuilder();
        builder.append(Pattern.quote(this.patternPrefix)).append("(");
        boolean first = true;
        if (this.injectionPropertyFile != null) {

            try {
                Properties properties = new Properties();
                properties.load(new FileInputStream(this.injectionPropertyFile));
                tokens = properties;

                for (Object propertyName : properties.keySet()) {
                    String propertyNameStr = (String) propertyName;
                    if (first) {
                        builder.append(Pattern.quote(propertyNameStr));
                        first = false;
                    } else {
                        builder.append("|").append(Pattern.quote(propertyNameStr));
                    }
                }

            } catch (Exception ex) {
                throw new BuildException(ex);
            }
        } else {
            Hashtable propertyMap = this.project.getProperties();
            tokens = propertyMap;
            for (Object propertyName : propertyMap.keySet()) {
                if (first) {
                    builder.append(Pattern.quote(propertyName.toString()));
                    first = false;
                } else {
                    builder.append("|").append(Pattern.quote(propertyName.toString()));
                }
            }
        }

        builder.append(")").append(Pattern.quote(this.patternSuffix));
        pattern = Pattern.compile(builder.toString());
        for (FileSet fileSet : this.fileSets) {
            File baseDirForThisFileSet = fileSet.getDir(this.project);
            String[] includedFilesInThisFileSet = fileSet.getDirectoryScanner(this.project).getIncludedFiles();
            for (String fileInThisFileSet : includedFilesInThisFileSet) {
                this.filesToInject.add(new File(baseDirForThisFileSet, fileInThisFileSet).getPath());
            }
        }
    }

    public boolean shouldInject(File file) {
        return this.filesToInject.isEmpty() || this.filesToInject.contains(file.getPath());
    }

    public InputStream doInjection(InputStream in, String filePath) throws IOException {
        InjectionResponse response = performInjection(in, filePath);
        return response.finalStream;
    }

    public File doInjection(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        InjectionResponse response = performInjection(in, file.getPath());
        in.close();
        if (response.isModified) {
            String modifiedFileContent = response.finalContent;
            FileWriter writer = new FileWriter(file);
            writer.write(modifiedFileContent);
            writer.close();
            file = new File(file.getPath());
        }
        return file;
    }

    private InjectionResponse performInjection(InputStream in, String filePath) throws IOException {
        boolean isModified = false;
        String fileContent = WebAntUtil.getContentFromStream(in);

        StringBuffer sb = new StringBuffer(fileContent.length());
        Matcher matcher = pattern.matcher(fileContent);
        while (matcher.find()) {
            String group1 = matcher.group(1);
            matcher.appendReplacement(sb, Matcher.quoteReplacement((String) tokens.get(group1)));
            isModified = true;
            if (this.verbose) {
                caller.log("Replacing " + this.patternPrefix + group1 + this.patternSuffix + " in " + filePath);
            }
        }
        matcher.appendTail(sb);
        fileContent = sb.toString();
        return new InjectionResponse(new ByteArrayInputStream(fileContent.getBytes()), fileContent, isModified);
    }

    private class InjectionResponse {
        protected InputStream finalStream;
        protected boolean isModified;
        protected String finalContent;

        private InjectionResponse(InputStream finalStream, String finalContent, boolean modified) {
            this.finalStream = finalStream;
            this.isModified = modified;
            this.finalContent = finalContent;
        }
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public String getPatternPrefix() {
        return patternPrefix;
    }

    public void setPatternPrefix(String patternPrefix) {
        this.patternPrefix = patternPrefix;
    }

    public String getPatternSuffix() {
        return patternSuffix;
    }

    public void setPatternSuffix(String patternSuffix) {
        this.patternSuffix = patternSuffix;
    }

    public List<FileSet> getFileSets() {
        return fileSets;
    }

    public void setFileSets(List<FileSet> fileSets) {
        this.fileSets = fileSets;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public File getInjectionPropertyFile() {
        return injectionPropertyFile;
    }

    public void setInjectionPropertyFile(File injectionPropertyFile) {
        this.injectionPropertyFile = injectionPropertyFile;
    }

    public boolean isModifyOriginal() {
        return modifyOriginal;
    }

    public void setModifyOriginal(boolean modifyOriginal) {
        this.modifyOriginal = modifyOriginal;
    }

    public Task getCaller() {
        return caller;
    }

    public void setCaller(Task caller) {
        this.caller = caller;
    }

    public List<String> getFilesToInject() {
        return filesToInject;
    }

    public void setFilesToInject(List<String> filesToInject) {
        this.filesToInject = filesToInject;
    }

    public static void main(String[] args) {
        System.out.println(Pattern.compile(Pattern.quote("${") + "(java.ext.dirs|WEBENGAGE_JS_ERROR_LOGGER_BASE|ftp.nonProxyHosts|HOW_IT_WORKS_NOTIFICATION_ID|WKSERVICE.HOST|lib.dir|java.vm.specification.version|FEEDBACK_MAILBOX|java.specification.vendor|WEBENGAGE_API_SECRET|java.vm.name|WEBENGAGE_SNS_SUBSCRIPTION_ARN|user.dir|STATIC_CONTENT_VERSION|DRUPAL_API_SECRET|java.awt.graphicsenv|MYSQL.HOST|FEEDBACK_APP_HOST|java.vendor.url|CDNZ_DOMAIN|S3_STATIC_BUCKET_ZIP|sun.boot.class.path|java.vendor|PG_SECRET_WORD|API_DEPLOYMENT_DIR|ADMIN.USERNAME|ant.java.version|CDN_STATIC_DOMAIN|java.home|SENTIMENT_SERVICE_HOST|QUARTZ.CLASSPATH|AWS.SECRET_KEY|WEBENGAGE_CONFIG_JS_FILE|build.dir|EVENT_API_KEY|WORDPRESS_API_KEY|JOOMLA_API_SECRET|java.version|NEW_DEMO_WEBENGAGE_LICENSE_CODE|SURVEY_RESPONSE_API_KEY|MYSQL.RULE_SCHEMA|AWS.S3_ENABLED|CDN_DOMAIN|HOME_PAGE_SURVEY_ID|java.runtime.name|MYSQL.DRIVER|DEMO_SYSTEM_NOTIFICATION_ID|ant.project.default-target|GEO_IP_URL|AWS.SES_ENABLED|scripts.dir|API_WEBENGAGE_HOST|java.class.version|ant.core.lib|OPENCART_API_KEY|WEBENGAGE_JS_LOGGER_BASE|ANTIVIRUS.PORT|EVENT_API_SECRET|api-docs-js.dir|user.name|PRESTASHOP_API_SECRET|EXEC_SPHINX|dist.dir|MIGRATE_TO_WEBENGAGE_API_KEY|WKSERVICE.HOST.USERNAME|DSIM_API_KEY|WEBENGAGE_SNS_TOPIC_ARN|basedir|PG_SELLER_ID|LOGS.FILENAME|java.vm.vendor|DASHBOARD_APP_HOST|SHOPIFY_API_SECRET|WEBENGAGE_PARENT_FOLDER_V3|SHOPIFY_API_KEY|MYSQL.PASSWORD|DRUPAL_API_KEY|sun.java.command|MIGRATE_TO_WEBENGAGE_API_SECRET|WEBENGAGE_PARENT_FOLDER_V3_ZIP|MYSQL.GEO_SCHEMA|sun.jnu.encoding|PG_TESTING|tool.less|AWS.ACCESS_KEY|MONGO.CONVERSION.STATS.HOST|PDF_SERVICE_HOST2|OPENCART_API_SECRET|AWS_SNS_ENABLED|os.name|socksNonProxyHosts|sun.java.launcher|MAGENTO_API_KEY|css.dir|WEBHOOK_JOB_MINUTES_BEFORE|TAG_CLOUD_HOST|DOCS_DEPLOYMENT_DIR|MYSQL.SCHEMA|ant.project.invoked-targets|java.library.path|NOTIFICATION_WIDGET_SCRIPT_FILE_NAME|MONGO.DB|WKSERVICE.PORT|sql.dir|WEBAPP_DEPLOYMENT_DIR|MAGENTO_API_SECRET|MONGO.FIELD_STATS.DB|PRESTASHOP_API_KEY|EXEC_PANDOC|java.vm.version|MYSQL.PORT|line.separator|MONGO.CONVERSION.STATS.DB|ant.file.webengage|sun.io.unicode.encoding|ant.home|STATIC_DEPLOYMENT_DIR|less.dir|API_CREATE_WIDGET_HOST|GTM.TAG|file.separator|sun.boot.library.path|java-src.dir|MONGO.HOST|ant.project.name|WKSERVICE.HOST.REALM|LOGS.DIR|os.arch|project.name|S3_STATIC_BUCKET|PDF_SERVICE_HOST|AWS.CLOUDFRONT.DISTRIBUTION_ID|MONGO.EXE.DIR|SHOPIFY_CLIENT_ID|java.vendor.url.bug|APPLICATION_DOMAIN|conf-src.dir|user.country.format|WEBENGAGE_CONFIG_HOST_V3|os.version|ant.file.type|FB.API_ID|WEBENGAGE_PARENT_FOLDER|java.runtime.version|EMAIL.HOST|WE_TRACKER_BASE_URL|API_WEBENGAGE_PORT|SNAPSHOT.IMAGE.SERVICE_HOST|MAILGUN_BASIC_AUTH_BASE64|DEMO_APPLICATION_DOMAIN|EXEC_PYTHON|DSIM_API_SECRET|WEBENGAGE_GZIP_FLAG_SETTER_URL|TAGCLOUD_SERVICE_AUTH|sun.cpu.isalist|file.encoding|build.only|DEMO_DISCOUNT_NOTIFICATION_ID|sun.cpu.endian|DEMO_WEBENGAGE_LICENSE_CODE|user.timezone|build-only-lib.dir|java.endorsed.dirs|ADMIN.PASSWORD|MYSQL.USERNAME|java.specification.name|sun.management.compiler|BASE_DIR|WEBENGAGE_LICENSE_CODE|path.separator|project.version|file.encoding.pkg|user.home|SENTIMENT_SERVICE_AUTH|java.vm.info|EXEC_S3CMD|web.dir|WORDPRESS_API_SECRET|HUBSPOT_API_SECRET|static.dir|API_WEBENGAGE_SCHEME|sun.arch.data.model|WE_TRACKER_BASIC_AUTH_BASE64|MEMCACHED.SERVERS|java.class.path|MONGO.CONVERSION.STATS.PORT|ant.library.dir|ANTIVIRUS.HOST|HUBSPOT_API_KEY|WEBENGAGE_CONVERSION_LOGGER_BASE|ant.version|WE_TRACKER_BASE_PORT|tool.rhino|JOOMLA_API_KEY|java.specification.version|MYSQL.USER_SCHEMA|java.vm.specification.name|WEBENGAGE_API_KEY|HOME_PAGE_NOTIFICATION_ID|ant.file.type.webengage|DEMO_LEAD_GEN_SURVEY_ID|GEN_NOTIFICATION_WIDGET_SCRIPT_FILE_NAME|gen.dir|java.vm.specification.vendor|dist-api-docs.dir|WEBENGAGE_CONFIG_HOST|user.language|ant.file|DEMO_CUST_INSIGHT_SURVEY_ID|java.awt.printerjob|PG_BASIC_AUTH_BASE64|SHOPIFY_CLIENT_KEY|FEEDBACK_TAB_UPLOAD_FOLDER|WIDGET_DOMAIN|build-api-docs.dir|WIDGET_SSL_DOMAIN|awt.toolkit|HOW_IT_WORKS_SURVEY_ID|sun.os.patch.level|MYSQL.COMMERCE_SCHEMA|SURVEY_APP_HOST|java.io.tmpdir|NOTIFICATION_APP_HOST|gopherProxySet|http.nonProxyHosts|WKSERVICE.HOST.PASSWORD|user.country|MONGO.PORT)" + Pattern.quote("}")));
    }
}