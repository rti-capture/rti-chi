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

package ModuleInterfaces;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author rcosta
 */
public class UserConfig {

    ConcurrentHashMap<String,String> configTable;

    File cfgFile;

    public UserConfig()
    {
        configTable = new ConcurrentHashMap();
        cfgFile = null;
    }

    public UserConfig(File configFile)
    {
        initializeUserConfig(configFile);
    }

    public UserConfig(String configFilePath)
    {
        cfgFile = new File(configFilePath);

        initializeUserConfig(cfgFile);
    }

    private void initializeUserConfig(File configFile)
    {
        configTable = new ConcurrentHashMap();

        if (configFile.isFile() && configFile.canRead())
        {
            // Read file
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                
                Document doc = db.parse(cfgFile);
                doc.getDocumentElement().normalize();

                //System.out.println("Root element " + doc.getDocumentElement().getNodeName());
                Element rootNode = doc.getDocumentElement();

                NodeList nodeLst = rootNode.getChildNodes();

                for (int i = 0; i < nodeLst.getLength(); i++)
                {
                    Node node = nodeLst.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE)
                    {
                        Element entry = (Element) node;
                        NamedNodeMap attributes = entry.getAttributes();

                        String keyS="";
                        String valueS="";

                        for (int attC=0; attC < attributes.getLength(); attC++)
                        {
                            Node item = attributes.item(attC);

                            if (item.getNodeType() == Node.ATTRIBUTE_NODE)
                            {
                                //System.out.println("Attr element " + item.getNodeName());
                                Attr attrib = (Attr) item;
                                if (attrib.getName().equals("Key"))
                                    keyS = attrib.getValue();
                                else
                                    valueS = attrib.getValue();


                                //this.silentWriteKey(attrib.getName(), attrib.getValue());
                            }
                            else
                            {
                                //System.out.println("Something else... " + item.getNodeName());
                            }
                        }
                        this.silentWriteKey(keyS, valueS);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
        else if(!cfgFile.exists())
        {
            try {
                // Write file stub
                cfgFile.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(UserConfig.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.writeToStorage();
        }
        else
        {
            // Some other error...?
            System.out.println("Unhandled UserConfig error upon initialization, likely a bug.");
        }
    }

    /*
     * Get value for key
     */
    public String readEntry(String key)
    {
        return configTable.get(key);
    }

    /*
     * Avoid using this unless you know what you are doing.
     * 
     * Sets value for key but does not store data to hard drive
     */
    public void silentWriteKey(String key, String data)
    {
        configTable.put(key, data);
    }

    /*
     * Sets value for key and writes to hard drive
     */
    public boolean writeEntry(String key, String data)
    {
        configTable.put(key, data);

        return writeToStorage();
    }

    public boolean writeToStorage()
    {
        if (this.cfgFile == null || !this.cfgFile.canWrite()) return false;

        try {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // Root element
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("Config");
        doc.appendChild(rootElement);

        for( Entry<String,String> entry : configTable.entrySet() )
        {
            Element entryNode = doc.createElement("Entry");
            Attr key = doc.createAttribute("Key");
            key.setValue(entry.getKey());
            Attr value = doc.createAttribute("Value");
            value.setValue(entry.getValue());

            entryNode.setAttributeNode(key);
            entryNode.setAttributeNode(value);

            rootElement.appendChild(entryNode);
        }

        //write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(cfgFile);
        transformer.transform(source, result);

        System.out.println("Wrote out configuration.");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            return false;
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
            return false;
        }

        return true;
    }

}
