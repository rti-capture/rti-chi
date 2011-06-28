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


/**
 * This class is intended to be used to add and retrieve "processes" in the XML document. A process is defined by:
 *    <br>  1. Identification;
 *    <br>  2. Status;
 *    <br>  3. Type;
 *    <br>  4. Description;
 *    <br>  5. Component Identification;
 *    <br>  6. Sequence number;
 *    <br>  7. Stage(defines the input/output of a process).
 *
 */

public class Process {
	String id;
	String status;
	String type;
	String desc;
	String componentID;
        String sequenceNumber;
	StageInfo stageI;

        /**
         * Constructor taking no arguments.
         *
         */

	public Process(){
		this.id = "";
		this.status = "";
		this.type = "";
		this.desc = "";
		this.componentID = "";
                this.sequenceNumber = "";
		this.stageI = new StageInfo();
	}
	
	/**
	 * Constructor taking all parameter except the StageInfo object.
         *
	 * @param id
         *      String : Process ID;
	 * @param status
         *      String : Process status;
	 * @param type
         *      String : Process type;
	 * @param desc
         *      String : Process description;
	 * @param ci
         *      String : Component Identification;
         * @param sn
         *      String : Sequence Number;
	 */
	
	public Process(String id, String status, String type,String desc,String ci,String sn) {
		super();
		this.id = id;
		this.status = status;
		this.type = type;
		this.desc = desc;
		this.componentID = ci;
                this.sequenceNumber = sn;
		this.stageI = new StageInfo();
	}
	
	/**
	 * Constructor taking all parameters including the StageInfo object.
	 * @param id 
         *      String : Process ID;
	 * @param status 
         *      String : Process status;
	 * @param type 
         *      String : Process type;
	 * @param desc
         *      String : Process description;
         * @param sn
         *      String : Sequence number;
	 * @param ci
         *      String : Component Identification;
         * * @param si 
         *      StageInfo : StageInfo object;
         *
         * @see StageInfo
	 */
	
	public Process(String id, String status, String type,String desc,String ci,String sn,StageInfo si) {
		super();
		this.id = id;
		this.status = status;
		this.type = type;
		this.desc = desc;
		this.stageI = si;
                this.sequenceNumber = sn;
		this.componentID = ci;
	}

        /**
         * This method returns the component identification.
         *
         * @return
         *      String : Component Identification.
         */
	
	public String getComponentID() {
		return componentID;
	}
        
        /**
         * This method sets the component identification.
         * 
         * @param componentID
         *      String : Component Identification.
         */

	public void setComponentID(String componentID) {
		this.componentID = componentID;
	}
        
        /**
         * This method returns the process description.
         *
         * @return
         *      String : Process description.
         */

	public String getDesc() {
		return desc;
	}
        
        /**
         * This method sets the process description.
         * 
         * @param desc
         *      String : Process description.
         */

	public void setDesc(String desc) {
		this.desc = desc;
	}

        /**
         * This method returns the process identification.
         *
         * @return
         *      STring : Process identification.
         */
	
	public String getId() {
		return id;
	}

        /**
         * This method sets the process identification.
         *
         * @param id
         *      String : Process identification.
         */

	public void setId(String id) {
		this.id = id;
	}

        /**
         * This method returns the process status.
         *
         * @return
         *      String : Process status.
         */

	public String getStatus() {
		return status;
	}

        /**
         * This method sets the process status.
         *
         * @param status
         *      String : Process status.
         */

	public void setStatus(String status) {
		this.status = status;
	}

        /**
         * This method returns the process type.
         *
         * @return
         *      String : Process type.
         */

	public String getType() {
		return type;
	}

        /**
         * This method returns the process sequence number.
         *
         * @return
         *      String : Process sequence number.
         */

        public String getSequenceNumber(){
            return this.sequenceNumber;
        }
        
        /**
         * This method sets the process type.
         * 
         * @param type
         *      String : Process type.
         */

	public void setType(String type) {
		this.type = type;
	}

        /**
         * This method returns the StageInfo associated with the current process.
         *
         * @return
         *      StageInfo : StageInfo associated with the current process.
         *
         * @see StageInfo
         */

	public StageInfo getStageI() {
		return stageI;
	}

        /**
         * This method sets the process StageInfo.
         *
         * @param stageI
         *      StageInfo : StageInfo that will be attached to this process.
         *
         * @see StageInfo
         */

	public void setStageI(StageInfo stageI) {
		this.stageI = stageI;
	}

        /**
         * This method adds an input reference to this process.
         *
         * @param name
         *      String : Input reference name.
         * @param value
         *      String : Input reference value.
         */
	
	public void addInput(String name,String value){
		this.stageI.addInputRef(name,value);
	}
        
        /**
         * This method adds an output reference to this process.
         *
         * @param name
         *      String : Output reference name.
         * @param value
         *      String : Output reference value.
         */
	
	public void addOutput(String name,String value){
		this.stageI.addOutputRef(name,value);
	}

        /**
         * This method sets the process sequence number.
         *
         * @param s
         *      String : Process sequence number.
         */
        
        public void setSequenceNumber(String s){
            this.sequenceNumber = s;
        }
		
}
