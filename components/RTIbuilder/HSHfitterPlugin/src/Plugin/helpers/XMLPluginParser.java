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

package Plugin.helpers;

//import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//import org.xml.sax.InputSource;

/**
 *
 * @author matheus
 */
public class XMLPluginParser {

    Document d;

    public XMLPluginParser(String filepath) throws Exception{
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        File fXmlFile = new File(filepath);
        d = dBuilder.parse(fXmlFile);
    }

    /*
    public XMLPluginParser(String filepath) throws Exception{
        DOMParser dp = new DOMParser();
        try{
            InputSource source = new InputSource(filepath);
            dp.parse(source);
            d = dp.getDocument();

        }catch(Exception e){
            System.out.println("Cannot Retrieve information about the plugin");
            e.printStackTrace();
            throw new Exception(e);
        }
    }*/

    public String getFitterPath(){
        NodeList nl = d.getElementsByTagName("properties");
        String s = "";
        for(int i=0; i< nl.getLength(); i++){
            if(nl.item(i).getNodeType() == Node.ELEMENT_NODE){
                Element e = (Element)nl.item(i);
                s = e.getAttribute("FITTER_PATH");
            }
        }
        return s;
    }

    public String getCropFolder(){
        NodeList nl = d.getElementsByTagName("properties");
        String s = "";
        for(int i=0; i< nl.getLength(); i++){
            if(nl.item(i).getNodeType() == Node.ELEMENT_NODE){
                Element e = (Element)nl.item(i);
                s = e.getAttribute("CROP_PATH");
            }
        }
        return s;
    }

    public void setFitterPath(String path) throws Exception{
        NodeList nl = d.getElementsByTagName("properties");
        for(int i=0; i< nl.getLength(); i++){
            if(nl.item(i).getNodeType() == Node.ELEMENT_NODE){
                Element e = (Element)nl.item(i);
                e.setAttribute("FITTER_PATH", path);
            }
        }
        try{
            this.writeXML();
        }catch(Exception e){
            throw new Exception(e);
        }
        

    }

    private void writeXML() throws Exception {
		try {
			// Now create a TransformerFactory and use it to create a
			// Transformer
			// object to transform our DOM document into a stream of XML text.
			// No arguments to newTransformer() means no XSLT stylesheet
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			// Create the Source and Result objects for the transformation
			DOMSource source = new DOMSource(this.d); // DOM document
			// StreamResult result = new StreamResult(System.out); // to XML
			// text
			StreamResult result2 = new StreamResult(new File("Plugins/PluginHSHfitter.xml"));

			// Finally, do the transformation
			transformer.transform(source, result2);

		} catch (Exception e) {
            e.printStackTrace();
			throw new Exception(e);
		}
	}


}
