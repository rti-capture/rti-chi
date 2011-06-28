/*
 *  RTIbuilder
 *  Copyright (C) 2008-11  Universidade do Minho and Cultural Heritage Imaging
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 3 as published
 *  by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package XMLcarrier;

import java.util.TreeMap;

/**
 * This class contains all the information that will be written in the XML's Header section.
 *
 */

public class HeaderInfo {
    
    private String uuid;
    private String projectName;
    private String description;
    private String author;
    private String creationDate;
    private String lastModDate;
    private String userInfo;
    private String host;
    private String timestamp;
    private String memoryAvailable;
    private String processorInfo;
    private String operatingSystem;
    private String macAddress;
    
    private TreeMap<String,String> map;

    private static final String ORIGINAL_CAPTURES = "OriginalCaptures";
    private static final String JPEG_EXPORTS = "JpegExports";
    private static final String FINISHED_FILES = "FinishedFiles";
    private static final String ASSEMBLY_FILES = "AssemblyFiles";
    private static final String CROPPED_DIR = "croppedDir";

    /**
     * Consctructor with no arguments. All information will be added using the set/add methods.
     *
     */
    
    public HeaderInfo(){
    	uuid = "";
    	projectName = "";
    	description = "";
    	author="";
        creationDate="";
        lastModDate = "";
        userInfo = "";
        host = "";
        memoryAvailable = "";
        processorInfo = "";
        operatingSystem = "";
        macAddress = "";
        timestamp = "0";
        map = new TreeMap<String,String>();
        
    }
    
    /**
     * Constructor receiving all the information required to the Header(no additional information).
     * 
     * @param uuid
     *      UUID : Header's unique identification.
     * @param projectName
     *      String: project name.
     * @param desc
     *      String: project description.
     * @param author
     *      String: project author.
     * @param creationDate
     *      String: project's creation date.
     * @param lastModDate
     *      String: project's last modification date.
     */

	public HeaderInfo(String uuid,String projectName,String desc,
            String author,String creationDate,String lastModDate) {
            this.author = author;
            this.creationDate = creationDate;
            this.description = desc;
            this.projectName = projectName;
            this.uuid = uuid;
            this.lastModDate = lastModDate;
            userInfo = "";
            host = "";
            memoryAvailable = "";
            processorInfo = "";
            operatingSystem = "";
            macAddress = "";
            timestamp = "0";
	}
        
        /**
         * Returns the Header's unique identification.
         * 
         * @return
         *      String : Header's unique identification.
         */

	public String getUuid() {
		return uuid;
	}
        
        /**
         * This method sets the project UUID.
         * 
         * @param uuid
         *      String: Project's unique identification.
         */

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
        
        /**
         * This method returns the project name.
         * 
         * @return
         *      String : Project name.
         */

	public String getProjectName() {
		return projectName;
	}
        
        /**
         * This method sets the project name.
         * 
         * @param projectName
         *      String : project name.
         */

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
        
        /**
         * This method returns the project description.
         * 
         * @return
         *      String : project description.
         */

	public String getDescription() {
		return description;
	}
        
        /**
         * This method sets the project description.
         * 
         * @param description
         *      String : project description.
         */

	public void setDescription(String description) {
		this.description = description;
	}
        
        /**
         * This method returns the project's author.
         * 
         * @return
         *      String : project's author.
         */

	public String getAuthor() {
		return author;
	}
        
        /**
         * This method sets the project author.
         * 
         * @param author
         *      String: project's author.
         */

	public void setAuthor(String author) {
		this.author = author;
	}
        
        /**
         * Returns the project's creation date.
         * 
         * @return
         *      String: project's creation date.
         */

	public String getCreationDate() {
		return this.creationDate;
	}
        
        /**
         * This method sets the project's creation date.
         * 
         * @param date
         *      String: project's creation date.
         */

	public void setCreationDate(String date) {
		this.creationDate = date;
	}
        
        /**
         * Returns the project's last modification date.
         * 
         * @return
         *      String: project's creation date.
         */
        
        public String getLastMod(){
            return this.lastModDate;
        }
        
        /**
         * This method sets the project's last modification date.
         * 
         * @param author
         *      String: project's last modification date.
         */
        
        public void setLastModDate(String date){
            this.lastModDate = date;
        }
        
        public void setTimestamp(String s){
            this.timestamp = s;
        }

        public void setTimestamp(int i){
            this.timestamp = String.valueOf(i);
        }

        public int getTimestamp(){
            return Integer.valueOf(this.timestamp).intValue();
        }
        
        public void incTimeStamp(){
            int v = Integer.valueOf(this.timestamp).intValue();
            this.timestamp = String.valueOf(++v);
        }

        /**
         * This method adds an especific attribute, given it's name and value.
         *
         * @param name
         *          Attribute name. Must be unique!
         * @param value
         *          Attribute value;
         */
        
        public void addParameter(String name,String value){
            this.map.put(name, value);
        }
        
        /**
         * This methods returns an attribute value given it's name.
         * 
         * @param name
         *      Attribute name.
         * @return
         *      Attribute value.
         */
        
        public String getParamterByName(String name){
            String res = "";
            res = this.map.get(name);
        return res;
        }

        /**
         * This method returns all attributes added in the form <name,value>.
         * 
         * @return
         *      TreeMap<String,String> : list of attributes in the form <name,value>.
         */
        
        public TreeMap<String,String> getMap(){
            return this.map;
        }
        
        /**
         * This method returns the project's author host name.
         * 
         * @return
         *      String : project's author host name.
         */

        public String getHost() {
            return host;
        }

        /**
         * This method adds a host name to the header.
         *
         * @param host
         *      String : host name to be added.
         */

        public void setHost(String host) {
            this.host = host;
        }

        /**
         * This method returns the user info(name).
         *
         * @return
         *      String : project's author username.
         */

        public String getUserInfo() {
            return userInfo;
        }

        /**
         * This method returns the project's author username.
         *
         * @param userInfo
         *      String : project's author username.
         */

        public void setUserInfo(String userInfo) {
            this.userInfo = userInfo;
        }

        /**
         * This method returns the project's author mac address of  the active interface at the moment that the
         * application was executed.
         *
         * @return
         *      String : project's author mac address.
         */

        public String getMacAddress() {
            return macAddress;
        }

        /**
         * This method sets the mac address that will be written in the Header section.
         *
         * @param macAddress
         *      String : project's author mac address.
         */

        public void setMacAddress(String macAddress) {
            this.macAddress = macAddress;
        }

        /**
         * This method returns the total amount of memory that was availabe in the JVM(Java Virtual Machine) at the moment that the
         * application was executed.
         *
         * @return
         *      String : JVM total memory.
         */

        public String getMemoryAvailable() {
            return memoryAvailable;
        }

        /**
         * This method receives a string representing the total amount of memory available at the JVM and adds to the header section.
         *
         * @param memoryAvailable
         *      String : JVM total memory.
         */

        public void setMemoryAvailable(String memoryAvailable) {
            this.memoryAvailable = memoryAvailable;
        }

        /**
         * This method returns the operating system used in creation of the XML document.
         *
         * @return
         *      String : operating system used in the creation of the XML document.
         */

        public String getOperatingSystem() {
            return operatingSystem;
        }

        /**
         * This method receives a String representing the operating system that will be added to the header section.
         *
         * @param operatingSystem
         *      String : operating system that will be added to the header section.
         */

        public void setOperatingSystem(String operatingSystem) {
            this.operatingSystem = operatingSystem;
        }

        /**
         * This method returns a String that represents the processor information available at the creation of the XML document.
         *
         * @return
         *      String : processor information.
         */

        public String getProcessorInfo() {
            return processorInfo;
        }

        /**
         * This method sets the processor information that will be added to the additional info subsection of the Header.
         *
         * @param processorInfo
         *      String : processor information.
         */

        public void setProcessorInfo(String processorInfo) {
            this.processorInfo = processorInfo;
        }

        public void addOriginalCaptureDirectory(String folderName){
            this.map.put(ORIGINAL_CAPTURES, folderName);
        }

        public String getOriginalCaptureDirectory(){
            return this.map.get(ORIGINAL_CAPTURES);
        }

        public void addJpegExportsDirectory(String folderName){
            this.map.put(JPEG_EXPORTS, folderName);
        }

        public String getJpegExportsDirectory(){
            return this.map.get(JPEG_EXPORTS);
        }

        public void addFinishedFilesDirectory(String folderName){
            this.map.put(FINISHED_FILES,folderName);
        }

        public String getFinishedFilesDirectory(){
            return this.map.get(FINISHED_FILES);
        }

        public void addAssemblyFilesDirectory(String folderName){
            this.map.put(ASSEMBLY_FILES, folderName);
        }

        public String getAssemblyFilesDirectory(){
            return this.map.get(ASSEMBLY_FILES);
        }

        public void addCroppedDir(String croppedDir){
            this.map.put(CROPPED_DIR, croppedDir);
        }

        public String getCroppedDir(){
            return this.map.get(CROPPED_DIR);
        }
    
}
