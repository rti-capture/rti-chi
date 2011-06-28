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
 * This class is supposed to be totally generic and can represent a Node
 * in the XML file.
 * 
 * @author matheus
 *
 */
public class Info {
	private String tagname;
	TreeMap<String,String> attributes;
	public String value;
	
	/**
	 * This constructor receives the tag name.
	 * 
	 * @param tagname
	 */
	public Info(String tagname){
		this.tagname = tagname;
		attributes = new TreeMap<String,String>();
	}
	
	public void addAttribute(String name,String value){
		this.attributes.put(name, value);
		
	}
	
	public void addContent(String o){
		this.value = o;
	}

	public String getTagname() {
		return tagname;
	}

	public void setTagname(String tagname) {
		this.tagname = tagname;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String getAttribute(String name){
		return this.attributes.get(name);
	}
	
	

}
