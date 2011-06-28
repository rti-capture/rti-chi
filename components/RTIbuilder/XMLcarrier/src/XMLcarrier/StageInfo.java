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

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * This class contains all the information about a process stage. In the current version of the LPTracker/XMLCarrier, only two
 * attributes are used(input/output references).
 * This doesnt mean that the other information are not usefull. It is just not used yet.
 *
 */

public class StageInfo {
	String userInfo;
	String dateS;
	String dateF;
	String host;
	String macAddr;
	String id;
	String version;
        TreeMap<String,String> inputRef;
	TreeMap<String,String> outputRef;

        /**
         * Empty constructor.
         *
         */

	public StageInfo(){
		this.inputRef = new TreeMap<String,String>();
		this.outputRef = new TreeMap<String,String>();
	}

        /**
         * Constructor taking all parameters except the input/output objects.
         *
         * @param dateF
         *      String : Stage - Date finished.
         * @param dateS
         *      String : Stage - Date started.
         * @param host
         *      String : Host where the stage was executed.
         * @param macAddr
         *      String : Mac Address of the active interface where the stage was executed.
         * @param userInfo
         *      String : User information.
         * @param id
         *      String : Identification of the stage.
         * @param version
         *      String : version of the software that generated the stage information.
         */
	
	public StageInfo(String dateF, String dateS, String host, String macAddr,
			String userInfo,String id,String version) {
		this.dateF = dateF;
		this.dateS = dateS;
		this.host = host;
		this.macAddr = macAddr;
		this.userInfo = userInfo;
		this.id = id;
		this.version = version;
		this.inputRef= new TreeMap<String,String>();
		this.outputRef= new TreeMap<String,String>();
	}

        /**
         * This method returns the version of the software used.
         *
         * @return
         *      String : Version of the software used.
         */

	public String getVersion() {
		return version;
	}

        /**
         * This method sets the software's version.
         *
         * @param version
         *      String : software's version.
         */

	public void setVersion(String version) {
		this.version = version;
	}

        /**
         * This method returns the stage's identification.
         *
         * @return
         *      String : Stage's identification.
         */

	public String getId() {
		return id;
	}

        /**
         * This method sets the stage's identification.
         *
         * @param id
         *      String : Stage's identification.
         */

	public void setId(String id) {
		this.id = id;
	}

        /**
         * This method returns the user information.
         *
         * @return
         *      String : user information.
         */

	public String getUserInfo() {
		return userInfo;
	}

        /**
         * This method sets the user information.
         *
         * @param userInfo
         *      String : User information.
         */

	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}
	public String getDateS() {
		return dateS;
	}
	public void setDateS(String dateS) {
		this.dateS = dateS;
	}
	public String getDateF() {
		return dateF;
	}
	public void setDateF(String dateF) {
		this.dateF = dateF;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getMacAddr() {
		return macAddr;
	}
	public void setMacAddr(String macAddr) {
		this.macAddr = macAddr;
	}
	
        /**
         * This method adds an input reference to this stage in the form <name,value>.
         *
         * @param k
         *      Input reference name;
         * @param v
         *      Input refence value;
         */
        
	public void addInputRef(String k,String v){
		this.inputRef.put(k, v);
	}
	
	/**
         * This method adds an output reference to this stage in the form <name,value>.
         *
         * @param k
         *      Output reference name;
         * @param v
         *      Output refence value;
         */
        
	public void addOutputRef(String k,String v){
		this.outputRef.put(k, v);
	}
        
        /**
         * This method returns a list of input references in the form <name(key),value>.
         * 
         * @return
         *      TreeMap<String,String> : list of input references in the form <name(key),value>.
         */

	public TreeMap<String,String> getInputRef() {
		return inputRef;
	}
        
        /**
         * This method adds a list of input references in the form <name(key)>,value>.
         * 
         * @param inputRef
         *      TreeMap<String,String> : list of input references in the form <name(key),value>.
         */

	public void setInputRef(TreeMap<String,String> inputRef) {
		this.inputRef = inputRef;
	}
        
        /**
         * This method returns a list of output references in the form <name(key),value>.
         * 
         * @return
         *      TreeMap<String,String> : list of output references in the form <name(key),value>.
         */

	public TreeMap<String,String> getOutputRef() {
		return outputRef;
	}
        
        /**
         * This method adds a list of output references in the form <name(key)>,value>.
         * 
         * @param outputRef
         *      TreeMap<String,String> : list of output references in the form <name(key),value>.
         */

	public void setOutputRef(TreeMap<String,String> outputRef) {
		this.outputRef = outputRef;
	}
	
	
}
