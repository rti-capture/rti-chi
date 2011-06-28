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
import java.util.UUID;

/**
 * This class contains all the information needed to add an retrieve data subsections in the XML document.
 * Note that a data section can only contain one of the following items: a list of areas or a list of parameters.
 *
 */

public class Data {
	
	private UUID id = null;
	private String name;
        private TreeMap<String, String> treeM;
	private ArrayList<Parameter> params = null;
        private ArrayList<AreaInfo> areas = null;
        private String description;

        /**
         * Constructor with no parameters. If this constructor is used, the programmer do not need to
         * generate and UUID for this data.
         *
         */

	public Data(){
		this.name = "";
                this.description = "";
		this.params = new ArrayList<Parameter>();
                this.treeM = new TreeMap<String,String>();
                this.areas = new ArrayList<AreaInfo>();
                this.id = UUID.randomUUID();
	}

        /**
         * Constructor receiving an UUID that represents this data unique identification.
         *
         *
         * @param id
         *      UUID: data unique identification.
         */
	
	public Data(UUID id) 
	{
		this.id = id;
		this.params = new ArrayList<Parameter>();
		this.name = "";
                this.description = "";
                this.treeM = new TreeMap<String,String>();
                this.areas = new ArrayList<AreaInfo>();
	}

        /**
         * Constructor receiving the data's UUID and the data's name.
         *
         * @param id
         *      UUID: data's unique identification.
         * @param s
         *      String: data's name.
         */
	
	public Data(UUID id,String s) 
	{
		this.id = id;
		this.params = new ArrayList<Parameter>();
		this.name = s;
                this.description = "";
                this.treeM = new TreeMap<String,String>();
                this.areas = new ArrayList<AreaInfo>();
	}

        /**
         * Returns the data's unique identification.
         *
         * @return
         *      UUID : data's unique identification.
         */
	
	public UUID getId() {
		return id;
	}

        /**
         * This methods returns the data's name.
         *
         * @return
         *      String : data's name.
         */
	
	public String getName(){
		return this.name;
	}

        /**
         * This method receives an UUID that will identify the data univocally.
         *
         * @param id
         *      UUID: data's unique identification.
         */

	public void setId(UUID id) {
		this.id = id;
	}

        /**
         * This method receives a String that will be set to the data's name.
         *
         * @param s
         *      String : data's name.
         */
	
	public void setName(String s){
		this.name = s;
	}

        /**
         * This method returns a list of parameters.
         * 
         * @return
         *      ArrayList<Parameter> : parameter's list.
         * @see Parameter
         */
	
	public ArrayList<Parameter> getParams() {
		return params;
	}

        /**
         * This method receives a list of parameters that will be added to a data.
         *
         * @param params
         *      ArrayList<Parameter> : parameter's list.
         *
         */

	public void setParams(ArrayList<Parameter> params) {
		this.params = params;
	}
	
        /**
         * This method receives a Parameter that will be added to a data section in the XML document.
         * 
         * @param p
         *      Parameter to be added.
         *
         * @see Parameter
         */

	public void addParameter(Parameter p)
	{
		this.params.add(p);
	}
	
        /**
         * This method receives a Parameter name and returns a new Parameter Object.
         * 
         * @param s
         *      String : Parameter's name.
         * @return
         *      Parameter Object.
         * @see Parameter
         */

	public Parameter getParameter(String s){
		Parameter p = null;
		for(int i=0;i<this.params.size();i++){
			if(this.params.get(i).getName().equals(s)){
				p = new Parameter(s,this.params.get(i).getValue());
			}
		}
	return p;
	}
        
        /**
         * This method adds an attribute to the <xc:data ...> tag.
         * 
         * @param id
         *          Attribute's name(it must be unique);
         * @param val
         *          Attribute's value;
         */
        
        public void addAttribute(String name,String val){
            this.treeM.put(name, val);
        }

        /**
         * This method returns all attributes added to the <xc:data ...> tag.
         *
         * @return
         *      TreeMap<String,String> : this object represent a list of attributes in the form <name,value>.
         */
        public TreeMap<String,String> getAllAttribs(){
            return this.treeM;
        }

        /**
         * This method adds an AreaInfo to the data section.
         *
         * @param i
         *      AreaInfo : AreaInfo to be added.
         *
         * @see AreaInfo
         */
        
        public void addAreaInfo(AreaInfo i){
            this.areas.add(i);
        }

        /**
         * This method returns all Area(s)Info objects added.
         *
         * @return
         *      ArrayList<AreaInfo> : AreaInfo's list.
         */

        public ArrayList<AreaInfo> getAreas() {
            return areas;
        }

        /**
         * This method receives a list of AreaInfo to be added to the data section.
         *
         * @param areas
         *      ArrayList<AreaInfo> : AreaInfo's list to be added.
         */

        public void setAreas(ArrayList<AreaInfo> areas) {
            this.areas = areas;
        }

        /**
         * This method returns the data's description.
         *
         * @return
         *      String : Data's description.
         */

        public String getDescription() {
            return description;
        }

        /**
         * This method sets the data description.
         *
         * @param description
         *      String: data's description.
         */

        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * This method returns a list of attributes added to the <xc:data ...> tag in the form <name,value>.
         *
         * @return
         *      TreeMap<String,String> : list of parameters in the form <name,value>.
         */

        public TreeMap<String, String> getTreeM() {
            return treeM;
        }

        /**
         * This method receives a list of attributes in the form <name,value> that will be added to the <xc:data ...> tag.
         *
         * @param treeM
         *      TreeMap<String,String> : list of attributes in the form <name,value>.
         */

        public void setTreeM(TreeMap<String, String> treeM) {
            this.treeM = treeM;
        }

        /**
         * This method returns an AreaInfo object given an UUID.
         *
         * @param id
         *      UUID: AreaInfo unique identification.
         * @return
         *      AreaInfo object whose UUID equals the id passed as an argument.
         */

        public AreaInfo getAreaByID(UUID id){
            AreaInfo a = null;
            for(AreaInfo ai : this.getAreas()){
                if(ai.getAreaId().toString().equals(id.toString())) a = ai;
            }
        return a;
        }
        
	
}
