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

import java.util.UUID;

/**
 * This class is intended to be used to add and retrieve "parameters" in the XML document.
 * A parameter can appear in a data section in the XML document. An example is given below:
 * 
 * <xc:dataSection>
 *      <xc:Data ...>
 *          <xc:param NAME="" TYPE="">VALUE</xc:param>
 *      </xc:Data>
 * </xc:dataSection>
 */

public class Parameter {
	
	private String type = null;
	private String name = null;
	private Object value = null;

        /**
         * Constructor receiving all information about a parameter(defined in the XML document).
         *
         * @param type
         *      String : parameter type. Here's a list of valid types :
         *              1. Integer;
         *              2. Double;
         *              3. Float;
         *              4. String;
         *              5. UUID;
         *              6. Boolean.
         * @param name
         *      String : parameter's name.
         * @param value
         *      String : parameter's value.
         */
	
	public Parameter(String type, String name, String value) 
	{
		this.type = type;
		this.name = name;
		//IFSS
		if(type.equalsIgnoreCase("Integer"))
			this.value = (Object) Integer.valueOf(value);
		else if(type.equalsIgnoreCase("Double"))
			this.value = (Object) Double.valueOf(value);
		else if(type.equalsIgnoreCase("Float"))
			this.value = (Object) Float.valueOf(value);
		else if(type.equalsIgnoreCase("String"))
			this.value = (Object) String.valueOf(value);
		else if(type.equalsIgnoreCase("UUID"))
			this.value = (Object) UUID.fromString(value);
		else if(type.equalsIgnoreCase("Boolean"))
			this.value = (Object) Boolean.valueOf(value);
	}
        
        /**
         * Constructor receiving a name and an object that contains the value and type of the parameter.
         * 
         * @param name
         *      String : parameter's name.
         * @param value
         *      Object : a java Object.
         */
	
	public Parameter(String name, Object value) 
	{
		this.type = value.getClass().getSimpleName();
		this.name = name;
		this.value = value;
	}
        
        /**
         * Return the parameter's name.
         * 
         * @return
         *      String : parameter's name.
         */

	public String getName() {
		return name;
	}
        
        /**
         * Set the parameter's name.
         * 
         * @param name
         *      String : parameter's name.
         */

	public void setName(String name) {
		this.name = name;
	}
        
        /**
         * Return the parameter's type.
         * 
         * @return
         *      String : parameter's type.
         */

	public String getType() {
		return type;
	}
        
        /**
         * Set the parameter's type.
         * 
         * @param type
         *      String : parameter type. Here's a list of valid type:
         *              1. Integer;
         *              2. Double;
         *              3. Float;
         *              4. String;
         *              5. UUID;
         *              6. Boolean.
         */

	public void setType(String type) {
		this.type = type;
	}

        /**
         * Return the parameter's value.
         *
         * @return
         *      Object : java Object.
         */

	public Object getValue() {
		return value;
	}

        /**
         * Set the parameter's value.
         *
         * @param value
         *      Object : parameter's value.
         */

	public void setValue(Object value) {
		this.value = value;
	}

        /**
         * This method returns the parameter's value but as a String.
         *
         * @return
         *      String : parameter's value.
         */

	public String getValueString() {
		//return value.getClass().toString();
            return value.toString();
	}

}
