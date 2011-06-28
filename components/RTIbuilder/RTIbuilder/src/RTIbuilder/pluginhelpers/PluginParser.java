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
import java.net.URL;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.SAXException;

public class PluginParser {

    public static ArrayList<Plugin> getPlugins() {
        ArrayList<Plugin> plugins = new ArrayList<Plugin>();
        File f = new File("Plugins");
        String PluginName = "";
        String FilePath = "";
        String PluginInterfaceClass = "";
        String version = "";
        UUID uuid = null;
        if (f.isDirectory()) {

            File plugin_descriptors[] = f.listFiles();

            for (File file : plugin_descriptors) {
                if (file.getName().endsWith(".xml")) {
                    DOMParser dp = new DOMParser();
                    try {
                        InputSource source = new InputSource(file.getPath());
                        dp.parse(source);
                        Document d = dp.getDocument();

                        NodeList nodelist = d.getElementsByTagName("name");
                        if (nodelist.getLength() == 1) {
                            PluginName = nodelist.item(0).getTextContent();
                        }
                        nodelist = d.getElementsByTagName("jarfile");
                        if (nodelist.getLength() == 1) {
                            FilePath = nodelist.item(0).getTextContent();
                        }
                        nodelist = d.getElementsByTagName("version");
                        if (nodelist.getLength() == 1) {
                            version = nodelist.item(0).getTextContent();
                        }
                        nodelist = d.getElementsByTagName("UI");
                        if (nodelist.getLength() == 1) {
                            PluginInterfaceClass = nodelist.item(0).getTextContent();
                        }
                        nodelist = d.getElementsByTagName("uuid");
                        if (nodelist.getLength() == 1) {
                            uuid = UUID.fromString(nodelist.item(0).getTextContent());
                        }
                        URL urls[] = {};
                        JarFileLoader jfl = new JarFileLoader(urls);
                        jfl.addFile((new File("").getAbsolutePath()) + File.separator + FilePath);

                        System.out.println((new File("").getAbsolutePath()) + File.separator + FilePath);

                        Class c = jfl.loadClass(PluginInterfaceClass);

                        System.out.println("Class loaded.... :: Name: " + PluginName + " Class: " + PluginInterfaceClass);

                        plugins.add(new Plugin(c, PluginName, FilePath, uuid, version));


                    } catch (InstantiationException ex) {
                        Logger.getLogger(PluginParser.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(PluginParser.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(PluginParser.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SAXException ex) {
                        Logger.getLogger(PluginParser.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(PluginParser.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }

        }

        return plugins;

    }

    public static Plugin getPlugin(String XmlPath) {
        Plugin plugin = null;
        String PluginName = "";
        String FilePath = "";
        String PluginInterfaceClass = "";
        String version = "";
        File file = new File(XmlPath);
        UUID uuid = null;
        DOMParser dp = new DOMParser();
        try {
            InputSource source = new InputSource(file.getPath());
            dp.parse(source);
            Document d = dp.getDocument();

            NodeList nodelist = d.getElementsByTagName("name");
            if (nodelist.getLength() == 1) {
                PluginName = nodelist.item(0).getTextContent();
            }
            nodelist = d.getElementsByTagName("jarfile");
            if (nodelist.getLength() == 1) {
                FilePath = nodelist.item(0).getTextContent();
            }
            nodelist = d.getElementsByTagName("UI");
            if (nodelist.getLength() == 1) {
                PluginInterfaceClass = nodelist.item(0).getTextContent();
            }
            nodelist = d.getElementsByTagName("version");
            if (nodelist.getLength() == 1) {
                version = nodelist.item(0).getTextContent();
            }
            nodelist = d.getElementsByTagName("uuid");
            if (nodelist.getLength() == 1) {
                uuid = UUID.fromString(nodelist.item(0).getTextContent());
            }
            URL urls[] = {};
            JarFileLoader jfl = new JarFileLoader(urls);
            jfl.addFile((new File("").getAbsolutePath()) + File.separator + FilePath);

            System.out.println((new File("").getAbsolutePath()) + File.separator + FilePath);
            System.out.println(PluginInterfaceClass);

            Class c = jfl.loadClass(PluginInterfaceClass);

            System.out.println("Class loaded....");
            plugin = new Plugin(c, PluginName, FilePath, uuid, version);

        } catch (InstantiationException ex) {
            Logger.getLogger(PluginParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(PluginParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PluginParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(PluginParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PluginParser.class.getName()).log(Level.SEVERE, null, ex);
        }

        return plugin;

    }
}
