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

public class CacheBuster {
  protected Project project;
  protected Task caller;
  protected File versionFile;
  protected boolean createVersionFileIfNotExists = true;
  protected boolean autoIncrementVersionNumber = false;
  protected String versionPropertyKey;
  protected boolean verbose = true;
  protected boolean modifyOriginal = false;
  protected File cacheBusterPreferencesFile = new File(".cache-buster.pref");
  protected List<CacheBusterRule> rules = new ArrayList<CacheBusterRule>();
  protected List<FileSet> fileSets = new ArrayList<FileSet>();

  private List<String> filesToApplyCacheBustingRules = new ArrayList<String>();
  private Map<String, CacheBusterPreference> cacheBusterPreferences = new HashMap<String, CacheBusterPreference>();
  private String versionData = null;

  protected static final String VERSION_FILE_TXT = "%\\{version-file-txt\\}";

  public CacheBuster(Project project){
    this.project = project;
  }

  protected final void init(){
    if(getVersionFile() == null){
      throw new BuildException("\"versionFile\" is a required attribute in the <cacheBuster> tag. " +
          "Your cache-busting rules cannot be applied without this file");
    }else if(!getVersionFile().exists() && !isCreateVersionFileIfNotExists()){
      throw new BuildException("The \"versionFile\" " + versionFile.getPath() + " does not exist." +
      " Set \"createVersionFileIfNotExists\" attribute to true or create the file manually.");
    }

    String versionText = getVersionText();
    if(isVerbose()){
      caller.log("Appending the version text \"" + versionText + "\" to all your cache-busting rules");
    }
    
    List<CacheBusterRule> rules = getRules();
    for(CacheBusterRule rule : rules){
      //If the rule is of "checkForLastModified" kind and the file is not yet modified,
      //use a previous version number. This is also a fix for the issue
      //http://code.google.com/p/ant-web-tasks/issues/detail?id=4
      String versionTextForThisRule = versionText;

      if(rule.isEmpty()){
        if(rule.isCheckFileLastModifiedTime()){
          throw new BuildException("A <rule> tag should have a valid \"file\" attribute or a nested \"fileset\", " +
              "if \"checkFileLastModifiedTime\" is set to true in the <cacheBuster>.");
        }else{
          throw new BuildException("A <rule> tag, inside cache-buster, without " +
              "\"from\" or \"to\" attributes is considered invalid.");
        }
      }

      //if this is a file specific rule
      if(rule.getFile() != null){
        File ruleFile = rule.getFile();
        if(rule.isCheckFileLastModifiedTime()){
          CacheBusterPreference preference =
              new CacheBusterPreference(ruleFile.getPath(), ruleFile.lastModified(), versionText);
          if(getCacheBusterPreferencesFile().exists()){
            cacheBusterPreferences = CacheBusterPreference.getPreferencesFromFile(getCacheBusterPreferencesFile());
            //add a new kid on the block
            if(!cacheBusterPreferences.containsKey(ruleFile.getPath())){
              cacheBusterPreferences.put(ruleFile.getPath(), preference);
            }
          }else{
            cacheBusterPreferences.put(ruleFile.getPath(), preference);
          }
        }else{
          caller.log("You have specified \"file\" attribute for the rule \"" + rule.getFrom().pattern() + "\". " +
              "This attribute can only be used in conjunction with the \"checkFileLastModifiedTime\" " +
              "attribute of the <cacheBuster> which is not enabled in your case.");  
        }

        //set the version text to current one, if and existing file is modified
        if(cacheBusterPreferences.get(ruleFile.getPath()).getLastModifiedTime() < ruleFile.lastModified()){
          cacheBusterPreferences.get(ruleFile.getPath()).setVersion(versionText);
        }

        versionTextForThisRule = cacheBusterPreferences.get(rule.getFile().getPath()).getVersion();
      }

      String replaceFormat = rule.getTo().replaceAll(VERSION_FILE_TXT, versionTextForThisRule);
      rule.setTo(replaceFormat);
    }

    for(FileSet fileSet : this.fileSets){
      File baseDirForThisFileSet = fileSet.getDir(this.project);
      String[] includedFilesInThisFileSet = fileSet.getDirectoryScanner(this.project).getIncludedFiles();
      for(String fileInThisFileSet : includedFilesInThisFileSet){
        this.filesToApplyCacheBustingRules.add(new File(baseDirForThisFileSet, fileInThisFileSet).getPath());
      }
    }
  }

  public InputStream doCacheBusting(InputStream in, String filePath) throws IOException{
    CacheBustingResponse response = performCacheBusting(in, filePath);
    return response.finalStream;
  }

  public File doCacheBusting(File file) throws IOException {
    InputStream in = new FileInputStream(file);
    CacheBustingResponse response = performCacheBusting(in, file.getPath());
    in.close();
    if(response.isModified){
      String modifiedFileContent = response.finalContent;
      FileWriter writer = new FileWriter(file);
      writer.write(modifiedFileContent);
      writer.close();
      file = new File(file.getPath());
    }
    return file;
  }

  private CacheBustingResponse performCacheBusting(InputStream in, String filePath) throws IOException {
    boolean streamModified = false;
    String fileContent = WebAntUtil.getContentFromStream(in);
    for(CacheBusterRule rule : rules){
      Matcher urlPatternMatcher = rule.from.matcher(fileContent);
      //if a url pattern is found in the file, proceed further
      if(urlPatternMatcher.find()){
        if(isVerbose()){
          caller.log("Match found in " + filePath + "; replacing all: " + rule.from.pattern());
        }
        fileContent = urlPatternMatcher.replaceAll(rule.to);
        streamModified = true;
      }
    }

    in = new ByteArrayInputStream(fileContent.getBytes());
    return new CacheBustingResponse(in, fileContent, streamModified);
  }

  public boolean shouldCacheBust(File file){
    return this.filesToApplyCacheBustingRules.isEmpty() || this.filesToApplyCacheBustingRules.contains(file.getPath());
  }

  protected String getVersionText(){
    if(versionData == null){
      File versionFile = getVersionFile();
      if(!versionFile.exists()){
        try{
          versionFile.createNewFile();
          FileWriter fileWriter = new FileWriter(versionFile);
          fileWriter.write(String.valueOf(0));
          fileWriter.flush();
          fileWriter.close();
          versionFile = new File(versionFile.getPath());
        }catch(Exception ex){
          throw new BuildException("Error creating version file. Skipping ...");
        }
      }

      if(getVersionPropertyKey() != null){
        Properties properties = new Properties();
        try{
          properties.load(new FileInputStream(getVersionFile()));
        }catch(Exception ex){
          throw new BuildException("Error reading your version property file: " + getVersionFile().getPath());
        }
        versionData = properties.getProperty(getVersionPropertyKey());
        if(versionData == null || "".equals(versionData.trim())){
          throw new BuildException("Your version file: " + getVersionFile().getPath() + " does not " +
              "contain any version data against the property: " + getVersionPropertyKey() + ". " +
              "Please check your version file. Skipping all your cache busting rules ...");
        }
      }else{
        int lastVersionNumber = 0;
        boolean isNumericString = true;
        try{
          FileReader fr = new FileReader(versionFile);
          String currentFileContent = WebAntUtil.readFully(fr);
          fr.close();
          lastVersionNumber = Integer.parseInt(currentFileContent);
        }catch(Exception ex){
          //nothing to worry about
          isNumericString = false;
        }

        try{
          if(isAutoIncrementVersionNumber()){
            if(isNumericString){
              lastVersionNumber++;
              FileWriter fileWriter = new FileWriter(versionFile);
              fileWriter.write(String.valueOf(lastVersionNumber));
              fileWriter.flush();
              fileWriter.close();
              versionFile = new File(versionFile.getPath());
            }
          }

          FileReader fr = new FileReader(versionFile);
          versionData = WebAntUtil.readFully(fr);
          fr.close();
        }catch(IOException ex){
          throw new BuildException("Error writing to your version file: " + getVersionFile().getAbsolutePath());
        }

        if(versionData == null || "".equals(versionData.trim())){
          throw new BuildException("Your version file: " + getVersionFile().getAbsolutePath() + " does not " +
              "contain any version data. Please check your version file. Skipping all your cache busting rules ...");
        }
      }

      versionData = versionData.trim();
    }
    return versionData;
  }

  public void addFileset(FileSet fileSet){
    this.fileSets.add(fileSet);
  }

  public FileSet createFileset() {
    FileSet fileSet = new FileSet();
    this.fileSets.add(fileSet);

    return fileSet;
  }

  public CacheBusterRule createRule() {
    return new CacheBusterRule();
  }

  public void addRule(CacheBusterRule rule){
    this.rules.add(rule);
  }

  public File getVersionFile() {
    return versionFile;
  }

  public void setVersionFile(File versionFile) {
    this.versionFile = versionFile;
  }

  public boolean isVerbose() {
    return verbose;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public List<CacheBusterRule> getRules() {
    return rules;
  }

  public void setRules(List<CacheBusterRule> rules) {
    this.rules = rules;
  }

  public String getVersionPropertyKey() {
    return versionPropertyKey;
  }

  public void setVersionPropertyKey(String versionPropertyKey) {
    this.versionPropertyKey = versionPropertyKey;
  }

  public boolean isModifyOriginal() {
    return modifyOriginal;
  }

  public void setModifyOriginal(boolean modifyOriginal) {
    this.modifyOriginal = modifyOriginal;
  }

  public File getCacheBusterPreferencesFile() {
    return cacheBusterPreferencesFile;
  }

  public void setCacheBusterPreferencesFile(File cacheBusterPreferencesFile) {
    this.cacheBusterPreferencesFile = cacheBusterPreferencesFile;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public Task getCaller() {
    return caller;
  }

  public void setCaller(Task caller) {
    this.caller = caller;
  }

  public boolean isCreateVersionFileIfNotExists() {
    return createVersionFileIfNotExists;
  }

  public void setCreateVersionFileIfNotExists(boolean createVersionFileIfNotExists) {
    this.createVersionFileIfNotExists = createVersionFileIfNotExists;
  }

  public boolean isAutoIncrementVersionNumber() {
    return autoIncrementVersionNumber;
  }

  public void setAutoIncrementVersionNumber(boolean autoIncrementVersionNumber) {
    this.autoIncrementVersionNumber = autoIncrementVersionNumber;
  }

  public List<FileSet> getFileSets() {
    return fileSets;
  }

  public void setFileSets(List<FileSet> fileSets) {
    this.fileSets = fileSets;
  }

  public List<String> getFilesToApplyCacheBustingRules() {
    return filesToApplyCacheBustingRules;
  }

  public void setFilesToApplyCacheBustingRules(List<String> filesToApplyCacheBustingRules) {
    this.filesToApplyCacheBustingRules = filesToApplyCacheBustingRules;
  }

  public Map<String, CacheBusterPreference> getCacheBusterPreferences() {
    return cacheBusterPreferences;
  }

  public void setCacheBusterPreferences(Map<String, CacheBusterPreference> cacheBusterPreferences) {
    this.cacheBusterPreferences = cacheBusterPreferences;
  }

  private class CacheBustingResponse{
    protected InputStream finalStream;
    protected boolean isModified;
    protected String finalContent;

    private CacheBustingResponse(InputStream finalStream, String finalContent, boolean modified) {
      this.finalStream = finalStream;
      this.isModified = modified;
      this.finalContent = finalContent;
    }
  }
}