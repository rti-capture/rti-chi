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
 * This class was created to be generic and to support all "raw" data generated
 * by the application.
 * 
 * @author matheus
 *
 */

public class RawInfo {
	private String MainTagname;
	ArrayList<Info> list;
	TreeMap<String,String> attributes;
	

	/**
	 * Constructor receiving the Tagname.
	 * 
	 * @param maintagName : The name of the main Tag. Can be seen as the
	 * name of a table. Ex. : <xc:LightPositions>
	 *
	 */
	
	public RawInfo(String maintagName){
		this.MainTagname = maintagName;
		list = new ArrayList<Info>();
		attributes = new TreeMap<String,String>();
	}
	
	/**
	 * This method adds an attribute to the main tag. Ex. : <xc:LightPositions GENERATEDBY=".."> 
	 * 
	 * @param name
	 * @param value
	 */
	
	public void addAttribute(String name,String value){
		this.attributes.put(name, value);
	}
	
	/**
	 * This method returns the value of an attribute given its name. 
	 * NULL is returned if the name is not found.
	 * 
	 * @param name
	 * @return
	 */
	
	public String getAttribute(String name){
		return this.attributes.get(name);
	}

	public String getMainTagname() {
		return MainTagname;
	}

	public void setMainTagname(String mainTagname) {
		MainTagname = mainTagname;
	}
	
	public void addInnerTag(Info i){
		this.list.add(i);
	}
	
	/**
	 * This method returns all "Info" contained in this class.
	 * 
	 * @return ArrayList<Info>
	 */
	
	public ArrayList<Info> getAllInnerInformation(){
		return this.list;
	}
	
	public void setNewInnerTags(ArrayList<Info> l){
		this.list = l;
	}
	
}

