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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.UUID;

/**
 * This class was created to serve as a information container. It is used to add and retrieve information using the XMLHandler API.
 * It can be used directly but the programmer can use the Data API to manipulate it because the XMLHandler deals with Data(s) and
 * not directly with AreaInfo.
 *
 */

public class AreaInfo {

    private UUID fileGroupID;
    private UUID areaId;
    private String shape;
    private String begin;
    private String end;
    private String coords;
    private TreeMap<String,String> a;
    
    /**
     * Constructor with no arguments. Using this, you do not need to create
     * an UUID for this class.
     * 
     */
    
    public AreaInfo(){
        this.shape = "";
        this.begin = "";
        this.end = "";
        this.coords = "";
        this.a = new TreeMap<String,String>();
        this.areaId = UUID.randomUUID();
    }
    
    /**
     * Constructor receiving two arguments. It is used when the programmer already generated the UUID's to the area and file group.
     * 
     * @param areaID
     *          Area unique identifier.
     * @param fileGrpID
     *          FileGroup unique identifier.
     */
    
    public AreaInfo(UUID areaID, UUID fileGrpID){
        this.areaId = areaID;
        this.fileGroupID = fileGrpID;
        this.a = new TreeMap<String,String>();
        this.shape = "";
        this.begin = "";
        this.end = "";
        this.coords = "";
    }

    /**
     * Get a representation of arbitrary attributes.
     *
     * @return
     *      TreeMap<String,String> : it is a representation of attributes in the form : <Name,Value>.
     */
    
    public TreeMap<String, String> getA() {
        return a;
    }

    /**
     * Get the Area unique identification.
     *
     * @return
     *      UUID : area Universal Unique IDentification.
     */

    public UUID getAreaId() {
        return areaId;
    }

    /**
     * It parses a string that is the representation in the XML and returns a Point2D.
     *
     * @return
     *      Point2D : representation in a known format of an XML attribute(eg.: BEGIN="x:1.0 y:1.0").
     *
     * @see #getBeginString() 
     */

    public Point2D getBegin() {
        String[] list = this.begin.split(" ");
       Point2D.Float p = new Point2D.Float((float)Float.valueOf(list[0].split(":")[1]),(float)Float.valueOf(list[1].split(":")[1]));
    return p;
    }

    /**
     * Returns a textual representation of the "BEGIN" attribute.
     *
     * @return
     *      String : Returns exactly the representation in the XML Document of the "BEGIN" attribute. (eg.: BEGIN="x:1.0 y:1.0").
     *
     * @see #getBegin() 
     */
    
    public String getBeginString(){
        return this.begin;
    }
    
    /**
     * Returns a textual representation of the "END" attribute.
     *
     * @return
     *      String : Returns exactly the representation in the XML Document of the "END" attribute. (eg.: END="x:10.0 y:10.0").
     *
     * @see #getEnd()
     */
    
    public String getEndString(){
        return this.end;
    }
    
    /**
     * Returns a textual representation of the "COORDS" attribute.
     *
     * @return
     *      String : Returns exactly the representation in the XML Document of the "COORDS" attribute.
     *              (eg.: COORDS="x:13.0 y:510.0;x:13.0 y:704.0;x:134.0 y:704.0;x:134.0 y:510.0;")
     *
     * @see #getCoords()
     */
    
    public String getCoordsString(){
        return this.coords;
    }

    /**
     * This method receives a textual representation of the "COORDS" attribute.
     *
     * @param s
     *      Textual representation of the "COORDS" attribute.
     */

    public void setCoords(String s){
        this.coords = s;
    }
    
    /**
     * Returns a representation of the "COORDS" attribute.
     * 
     * @return
     *      ArrayList<Point2D.Float>  : This ArrayList contains the points represented in the "COORDS".
     *      
     */
    
    public ArrayList<Point2D.Float> getCoords() {
        ArrayList<Point2D.Float> list = new ArrayList<Point2D.Float>();
        String[] strList = this.coords.split(";");
        for(int i=0; i < strList.length ;i++){
            String[] s = strList[i].split(" ");
            list.add((new Point2D.Float((float)Float.valueOf(s[0].split(":")[1]),(float)Float.valueOf(s[1].split(":")[1]))));
            //i++;
        }
    return list;
    }
    
    /**
     * It parses a string that is the representation in the XML of the "END" attribute and returns a Point2D.
     *
     * @return
     *      Point2D : representation in a known format of an XML attribute(eg.: END="x:1.0 y:1.0").
     *
     * @see #getEndString()
     */

    public Point2D getEnd() {
        String[] list = this.end.split(" ");
       Point2D.Float p = new Point2D.Float((float)Float.valueOf(list[0].split(":")[1]),(float)Float.valueOf(list[1].split(":")[1]));
    return p;
    }

     /**
     * Get the file group unique identification.
     *
     * @return
     *      UUID : file group Universal Unique IDentification.
     */

    public UUID getFileGroupID() {
        return fileGroupID;
    }

    /**
     * Returns a textual representiona of the "SHAPE" attribute.
     *
     * @return
     *      String : Textual represention of the "SHAPE" attribute. (eg.: SHAPE="RECTANGLE").
     */

    public String getShape() {
        return shape;
    }

    /**
     * This method receives a set of attributes that will be added to a <xc:area> section.
     *
     * @param a
     *      TreeMap<String,String> : set of arbitrary attributes.
     *      
     */
    public void setA(TreeMap<String, String> a) {
        this.a = a;
    }

    /**
     *This method receives an object (UUID) identifying an area.
     *
     * @param areaId
     *      UUID : representation of an area unique identification.
     */

    public void setAreaId(UUID areaId) {
        this.areaId = areaId;
    }

    /**
     * This method receives an object(Point2D.Float) representing a point ("BEGIN" attribute).
     * 
     * @param p
     *      Point2D.Float : representation of the "BEGIN" attribute.
     *
     * @see #setBegin(java.lang.String)
     */

    public void setBegin(Point2D.Float p) {
        StringBuilder str = new StringBuilder();
            str.append("x:" + p.x);
            str.append(" ");
            str.append("y:" + p.y);
    this.begin = str.toString();
    }

    /**
     * This method receives a String representing the "BEGIN" attribute.
     *
     * @param s
     *      String : representation of the "BEGIN" attribute.
     *
     * @see #setBegin(Point2D.Float)
     */
    
    public void setBegin(String s){
        this.begin = s;
    }
    
    /**
     * This method receives a String representing the "END" attribute.
     *
     * @param s
     *      String : representation of the "END" attribute.
     *
     * @see #setEnd(Point2D.Float)
     */
    
    public void setEnd(String s){
        this.end = s;
    }

    /**
     * This method receives a list of points(represeting the points in the "COORDS" attribute).
     *
     * @param list
     *      ArrayList<Point2D.Float> : representation of the points in the "COORDS" attribute.
     */

    public void setCoords(ArrayList<Point2D.Float> list) {
        StringBuilder str = new StringBuilder();
        for(Point2D.Float p : list){
            str.append("x:" + p.getX());
            str.append(" ");
            str.append("y:" + p.getY());
            str.append(";");
        }
        this.coords = str.toString();
    }
    
    /**
     * This method receives an object(Point2D.Float) representing a point ("END" attribute).
     * 
     * @param p
     *      Point2D.Float : representation of the "END" attribute.
     *
     * @see #setEnd(Point2D.Float)
     */

    public void setEnd(Point2D.Float p) {
        StringBuilder str = new StringBuilder();
            str.append("x:" + p.x);
            str.append(" ");
            str.append("y:" + p.y);
    this.end = str.toString();
    }
    
    /**
     *This method receives an object (UUID) identifying a file group.
     *
     * @param fileGroupID
     *      UUID : representation of a file group by an unique identification.
     */

    public void setFileGroupID(UUID fileGroupID) {
        this.fileGroupID = fileGroupID;
    }
    
    /**
     * This method receives a String representing the "SHAPE" attribute.
     *
     * @param shape
     *      String : representation of the "SHAPE" attribute.
     *
     */

    public void setShape(String shape) {
        this.shape = shape;
    }
    
    /**
     * This methods is intended to be used whenever you want to add your own 
     * parameters to an area.
     * 
     * @param name
     *          Parameter's name(it must be unique);
     * @param value
     *          Parameter's value;
     */
    
    public void addAreaAttribute(String name,String value){
        this.a.put(name, value);
    }

    /**
     * This method returns an attribute value given its name.
     *
     * @param name
     *      Parameter name.
     * @return
     *      String : attribute value.
     */

    public String getAttribute(String name){
        String res = "";
        res = this.a.get(name);
    return res;
    }

    /**
     * This method returns the structure that contains all attributes added in the form <name,value>.
     *
     * @return
     *      TreeMap<String,String> : structure that maps an attribute value given its name.
     * @see #getA() 
     */
    
    public TreeMap<String,String> getAllAttributes(){
        return this.a;
    }
    
    
}
