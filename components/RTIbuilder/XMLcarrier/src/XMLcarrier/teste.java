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

import XMLcarrier.Exceptions.UUIDNotFound;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.UUID;


public class teste{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
            
        XMLHandler a = new XMLHandler("may.xml");
        
        RawInfo spheres = new RawInfo("Spheres");
	        Info sphere1 = new Info("Sphere");
	        sphere1.addAttribute("ID", "1");
	        sphere1.addAttribute("X", "1.0");
	        sphere1.addAttribute("Y", "2.0");
	        sphere1.addAttribute("R", "3.0");
	        sphere1.addAttribute("QQATRIBUTO", "VALOR");
	        
	        Info sphere2 = new Info("Sphere");
	        sphere2.addAttribute("ID", "2");
	        sphere2.addAttribute("X", "22.0");
	        sphere2.addAttribute("Y", "222.0");
	        sphere2.addAttribute("R", "3.0");
	        sphere2.addAttribute("QQATRIBUTO", "VALOR");
	        
	    spheres.addInnerTag(sphere1);
	    spheres.addInnerTag(sphere2);
	    
	    RawInfo lp = new RawInfo("LightPositions");
	    	Info um = new Info("LP");
	    	um.addAttribute("QQvalor2", "VALOR1_2");
	    	Info um2 = new Info("LP");
	    	um2.addAttribute("QQvalor2", "VALOR2_2");
	    	Info um3 = new Info("LP");
	    	um3.addAttribute("QQvalor2", "VALOR3_2");
	    	
	    	lp.addInnerTag(um);
	    	lp.addInnerTag(um2);
	    	lp.addInnerTag(um3);
	    	
        
        try{
        	//a.loadXML();
            a.createXML();
            a.setHeaderInfo(new HeaderInfo());
            a.setTimestamp(0);
            System.out.println("Timestamp inicial " + a.getTimestamp());
            a.incTimestamp();
            System.out.println("Incrementei timestamp " + a.getTimestamp());
            a.incTimestamp();
            System.out.println("Incrementei timestamp " + a.getTimestamp());
            a.incTimestamp();
            System.out.println("Incrementei timestamp " + a.getTimestamp());
            a.writeXML();
            //ArrayList<RawInfo> list = new ArrayList<RawInfo>();
            //list.add(lp);
            //a.modifyComputedInfo(list);
            //a.createXML();
        	//a.addComputedInfo(spheres);
        	//a.addComputedInfo(lp);
        	
        	//RawInfo ri = a.getComputedInfo("LightPositions");
        	//System.out.println(ri.getMainTagname());
        	//ArrayList<Info> list = ri.getAllInnerInformation();
        	
        	//Info b = list.get(0);
        	//list.remove(1);
        	//b.addAttribute("teste", "teste");
        	
        	//a.modifyComputedInfo(ri);
        	
        	//System.out.println(ri.getAllInnerInformation().get(0).getAttribute("QQATRIBUTO"));

        	a.writeXML();
        }catch(Exception e){
        	e.printStackTrace();
        }
        
            
        }	
}

