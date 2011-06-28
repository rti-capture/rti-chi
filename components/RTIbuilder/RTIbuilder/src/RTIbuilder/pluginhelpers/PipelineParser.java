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
package RTIbuilder.pluginhelpers;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import java.io.File;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class PipelineParser {

    public static ArrayList<Pipeline> getPipelines() {
        ArrayList<Pipeline> pipelines = new ArrayList<Pipeline>();
        File f = new File("Pipelines");
        String PipelineName = "";
        if (f.isDirectory()) {

            File pipeline_descriptors[] = f.listFiles();

            for (File file : pipeline_descriptors) {
                if (file.getName().endsWith(".xml")) {
                    DOMParser dp = new DOMParser();
                    try {
                        Pipeline pipe = new Pipeline();
                        InputSource source = new InputSource(file.getPath());
                        dp.parse(source);
                        Document d = dp.getDocument();

                        NodeList nodelist = d.getElementsByTagName("name");
                        if (nodelist.getLength() == 1) {
                            PipelineName = nodelist.item(0).getTextContent();
                            PipelineName = PipelineName == null ? "" : PipelineName;
                        }
                        pipe.setName(PipelineName);
                        nodelist = d.getElementsByTagName("plugins");
                        if (nodelist.getLength() == 1) {
                            Element plugins = (Element) nodelist.item(0);
                            NodeList nl = plugins.getChildNodes();
                            for (int i = 0; i < nl.getLength(); i++) {
                                if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                                    Element ee = (Element) nl.item(i);
                                    Integer order = Integer.parseInt(ee.getAttribute("order"));
                                    UUID uuid = UUID.fromString(ee.getAttribute("uuid"));
                                    String PathType = ee.getAttribute("path");// map.getNamedItem("type").getNodeValue();
                                    String FilePath = ee.getAttribute("type");//map.getNamedItem("path").getNodeValue();
                                    Plugin p = new Plugin(PipelineName, FilePath, uuid);
                                    pipe.addPlugin(order, p);
                                }
                            }
                        }

                        pipelines.add(pipe);

                    } catch (SAXException ex) {
                        Logger.getLogger(PluginParser.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(PluginParser.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }

        }

        return pipelines;
    }

    public static Pipeline getPipeline(String url) {
        File file = new File(url);
        String PipelineName = "";
        Pipeline pipe = null;

        if (file.getName().endsWith(".xml")) {
            DOMParser dp = new DOMParser();
            try {
                pipe = new Pipeline();
                InputSource source = new InputSource(file.getPath());
                dp.parse(source);
                Document d = dp.getDocument();

                NodeList nodelist = d.getElementsByTagName("name");
                if (nodelist.getLength() == 1) {
                    PipelineName = nodelist.item(0).getTextContent();
                    PipelineName = PipelineName == null ? "" : PipelineName;
                }
                pipe.setName(PipelineName);
                nodelist = d.getElementsByTagName("plugins");
                if (nodelist.getLength() == 1) {
                    Element plugins = (Element) nodelist.item(0);
                    NodeList nl = plugins.getChildNodes();
                    for (int i = 0; i < nl.getLength(); i++) {
                        if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            Element ee = (Element) nl.item(i);
                            Integer order = Integer.parseInt(ee.getAttribute("order"));
                            UUID uuid = UUID.fromString(ee.getAttribute("uuid"));
                            String PathType = ee.getAttribute("path");// map.getNamedItem("type").getNodeValue();
                            String FilePath = ee.getAttribute("type");//map.getNamedItem("path").getNodeValue();
                            Plugin p = new Plugin(PipelineName, FilePath, uuid);
                            pipe.addPlugin(order, p);
                        }
                    }
                }
            } catch (SAXException ex) {
                Logger.getLogger(PluginParser.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PluginParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }



        return pipe;

    }
}
