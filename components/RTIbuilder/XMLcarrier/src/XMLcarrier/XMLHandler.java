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

import XMLcarrier.Exceptions.*;
import java.io.File;
import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.XMLConstants;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.lang.String;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TreeMap;
import java.util.UUID;
import org.w3c.dom.NamedNodeMap;

/**
 * This class is the most important of the XMLcarrier package. It can be seen as the API between the LPTracker application and the
 * XML Document. It is here that the XML document is parsed, written and modified.
 *
 */
public class XMLHandler {

    SchemaFactory schemaFactory;
    Schema schemaXSD;
    Validator validator;
    DocumentBuilder parser;
    Document document;
    Element xmlCarrier;
    Element header;
    Element fileSec;
    Element processes;
    Element messagePassing;
    Element data;
    Element computedData;
    Element logger;
    private final String XMLCarrierNS = "xc";
    private final String XLINK_ATTRIB_NAME = "xlink";
    private final String XLINK_DEFAULT_TYPE = "simple";
    private final String SCHEMA_DEFAULT_NAME = "lptracker.xsd";
    String xmlPath;

    /**
     * Constructor with no arguments.
     * @deprecated
     *      This constructor was deprecated because we do not want to make assumptions about a default xml's name.
     *
     */
    public XMLHandler() {
        //TODO Define a default XML name to this.xmlPath;
    }

    /**
     * Constructor receiving the xml's document name.
     *
     * @param s
     *      String : XML's name.
     *
     */
    public XMLHandler(String s) {
        this.xmlPath = s;
    }

    private void xmlValidator(String xmlFN, String xsdFN)
            throws XSDCantValidadeXML {
        // build an XSD-aware SchemaFactory
        schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // hook up org.xml.sax.ErrorHandler implementation.
        // schemaFactory.setErrorHandler( myErrorHandler );

        // get the custom xsd schema describing the required format for my XML
        // files.
        try {
            schemaXSD = schemaFactory.newSchema(new File(xsdFN));
        } catch (SAXException E) {
            System.out.println("newSchema : " + E.getMessage());
        }

        // Create a Validator capable of validating XML files according to my
        // custom schema.
        try {
            validator = schemaXSD.newValidator();
        } catch (Exception e) {
            System.out.println("newValidator : " + e.getMessage());
            //e.printStackTrace();
        }

        // Get a parser capable of parsing vanilla XML into a DOM tree
        try {
            parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (FactoryConfigurationError fce) {
            fce.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }

        // parse the XML DOM tree against the stricter XSD schema
        try {
            validator.validate(new DOMSource(document));

        } catch (IOException e) {
            //System.out.print("IOException" + e.getMessage());
            throw new XSDCantValidadeXML(e.getMessage());

        } catch (SAXException se) {
            //System.out.print("SAXException" + se.getMessage());
            throw new XSDCantValidadeXML(se.getMessage());

        } catch (NullPointerException ee) {
            //System.out.println("NullPointerException" + ee.getMessage());
            throw new XSDCantValidadeXML(ee.getMessage());
        }
    }

    /**
     * This method is intended to be used only once. When there is no XML created and when the application wants to start writing
     * its output, this method must be invoked because it creates and writes the XML's "skeleton".
     */
    public void createXML() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            this.document = db.newDocument();

            this.xmlCarrier = document.createElement("XMLCarrier");
            this.xmlCarrier.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + XLINK_ATTRIB_NAME, "http://www.w3.org/1999/xlink");
            this.xmlCarrier.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xc", "http://alba.di.uminho.pt/XMLCarrier");
            this.xmlCarrier.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://alba.di.uminho.pt/XMLCarrier");
            this.xmlCarrier.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            this.xmlCarrier.setAttribute("xsi:schemaLocation", "http://alba.di.uminho.pt/XMLCarrier " + SCHEMA_DEFAULT_NAME);
            document.appendChild(xmlCarrier);

            this.header = document.createElement(XMLCarrierNS + ":Header");
            xmlCarrier.appendChild(this.header);

            this.fileSec = document.createElement(XMLCarrierNS + ":fileSec");
            xmlCarrier.appendChild(this.fileSec);

            this.processes = document.createElement(XMLCarrierNS + ":Processes");
            xmlCarrier.appendChild(this.processes);

            this.messagePassing = document.createElement(XMLCarrierNS + ":dataSec");
            xmlCarrier.appendChild(this.messagePassing);

            this.computedData = document.createElement(XMLCarrierNS + ":computedData");
            xmlCarrier.appendChild(this.computedData);

            this.logger = document.createElement(XMLCarrierNS + ":Log");
            xmlCarrier.appendChild(this.logger);

        } catch (Exception e) {
            System.out.println("Unable to create XML : " + e.getMessage());
        }
    }

    /**
     * This method parses a XML file and loads it's content. It must be used once before start invoking methods that needs the
     * Document's tree.
     * @throws
     */
    public void loadXML()
            throws XMLNotAvailable, XSDCantValidadeXML {
        // Get a parser capable of parsing vanilla XML into a DOM tree
        try {
            parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        } catch (FactoryConfigurationError fce) {
            fce.printStackTrace();

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }


        // parse the XML purely as XML and get a DOM tree representation.
        try {
            document = parser.parse(new File(this.xmlPath));

        } catch (IOException e) {
            e.printStackTrace();
            throw new XSDCantValidadeXML(e.getMessage());

        } catch (SAXException se) {
            se.printStackTrace();
            throw new XSDCantValidadeXML(se.getMessage());

        } catch (NullPointerException n) {
            throw new XMLNotAvailable(n.getMessage());
        }

        /*   try{
        this.xmlValidator(this.xmlPath, SCHEMA_DEFAULT_NAME);
        }catch(XSDCantValidadeXML xsd){
        throw new XSDCantValidadeXML(xsd.getMessage());
        }*/

        this.xmlCarrier = document.getDocumentElement();
        NodeList nl = this.xmlCarrier.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (nl.item(i).getNodeName().equals(XMLCarrierNS + ":Header")) {
                    this.header = (Element) nl.item(i);
                } else if (nl.item(i).getNodeName().equals(XMLCarrierNS + ":fileSec")) {
                    this.fileSec = (Element) nl.item(i);
                } else if (nl.item(i).getNodeName().equals(XMLCarrierNS + ":Processes")) {
                    this.processes = (Element) nl.item(i);
                } else if (nl.item(i).getNodeName().equals(XMLCarrierNS + ":dataSec")) {
                    this.messagePassing = (Element) nl.item(i);
                } else if (nl.item(i).getNodeName().equals(XMLCarrierNS + ":computedData")) {
                    this.computedData = (Element) nl.item(i);
                } else if (nl.item(i).getNodeName().equals(XMLCarrierNS + ":Log")) {
                    this.logger = (Element) nl.item(i);
                }
            }
        }
    }

    /**
     * This method writes the XML document.
     *
     * @throws
     */
    public void writeXML() throws Exception {
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
            DOMSource source = new DOMSource(this.document); // DOM document
            // StreamResult result = new StreamResult(System.out); // to XML
            // text
            StreamResult result2 = new StreamResult(new File(this.xmlPath));

            // Finally, do the transformation
            transformer.transform(source, result2);

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private void writeHeader(HeaderInfo hi) {
        header.setAttribute("UUID", hi.getUuid());

        Element projName = document.createElement(XMLCarrierNS + ":ProjectName");
        projName.setTextContent(hi.getProjectName());
        this.header.appendChild(projName);

        Element desc = document.createElement(XMLCarrierNS + ":Description");
        desc.setTextContent(hi.getDescription());
        this.header.appendChild(desc);

        Element author = document.createElement(XMLCarrierNS + ":Author");
        author.setTextContent(hi.getAuthor());
        this.header.appendChild(author);

        Element date = document.createElement(XMLCarrierNS + ":CreationDate");
        date.setTextContent(hi.getCreationDate());
        this.header.appendChild(date);

        Element lastModDate = document.createElement(XMLCarrierNS + ":LastModificationDate");
        lastModDate.setTextContent(hi.getLastMod());
        this.header.appendChild(lastModDate);

        Element ui = document.createElement(XMLCarrierNS + ":UserInfo");
        ui.setTextContent(generateUserInfo());
        this.header.appendChild(ui);

        Element host = document.createElement(XMLCarrierNS + ":Host");
        host.setTextContent(generateHost());
        this.header.appendChild(host);

        Element timestamp = document.createElement(XMLCarrierNS + ":Timestamp");
        timestamp.setTextContent(String.valueOf(hi.getTimestamp()));
        this.header.appendChild(timestamp);

        Element additionalInfo = document.createElement(XMLCarrierNS + ":AdditionalInfo");
        this.header.appendChild(additionalInfo);

        Element infoOne = document.createElement(XMLCarrierNS + ":Info");
        infoOne.setAttribute("OperatingSystem", hi.getOperatingSystem());

        infoOne.setAttribute("MacAddress", hi.getMacAddress());

        infoOne.setAttribute("Processor", hi.getProcessorInfo());

        infoOne.setAttribute("MemoryAvailable", hi.getMemoryAvailable());
        additionalInfo.appendChild(infoOne);

        for (String str : hi.getMap().keySet()) {
            Element others = document.createElement(XMLCarrierNS + ":Info2");
            others.setAttribute("NAME", str);
            others.setAttribute("VALUE", hi.getParamterByName(str));
            additionalInfo.appendChild(others);
        }

    }

    public int getTimestamp() {
        NodeList nl = this.header.getElementsByTagName(XMLCarrierNS + ":Timestamp");
        int res = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nl.item(i);
                res = Integer.valueOf(e.getTextContent()).intValue();
            }
        }
        return res;
    }

    public void incTimestamp() {
        NodeList nl = this.header.getElementsByTagName(XMLCarrierNS + ":Timestamp");
        int res = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nl.item(i);
                res = Integer.valueOf(e.getTextContent()).intValue();
                e.setTextContent(String.valueOf(++res));
            }
        }
    }

    public void setTimestamp(int a) {
        NodeList nl = this.header.getElementsByTagName(XMLCarrierNS + ":Timestamp");
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nl.item(i);
                e.setTextContent(String.valueOf(a));
            }
        }
    }

    private void removeHeader() {
        NodeList nl = this.header.getChildNodes();
        ArrayList<Node> list = new ArrayList<Node>();
        for (int i = 0; i < nl.getLength(); i++) {
            //if(nl.item(i).getNodeType() == Node.ELEMENT_NODE){
            list.add(nl.item(i));
            //}
        }

        for (Node e : list) {
            e.getParentNode().removeChild(e);
            this.header.normalize();
            this.document.normalize();
        }
        this.header.normalize();
        this.document.normalize();
    }

    private boolean removeFileRefs(String uuid) {
        NodeList nl = this.fileSec.getElementsByTagName(XMLCarrierNS + ":fileGrp");
        Element fileGrp = null;
        for (int a = 0; a < nl.getLength(); a++) {
            if (nl.item(a).getNodeType() == Element.ELEMENT_NODE) {
                Element aux = (Element) nl.item(a);
                if (aux.getAttribute("ID").equals(uuid)) {
                    fileGrp = (Element) nl.item(a);
                }
            }
        }

        if (fileGrp == null) {
            return false;
        }

        ArrayList<Node> list = new ArrayList<Node>();

        NodeList imgs = fileGrp.getChildNodes();
        for (int i = 0; i < imgs.getLength(); i++) {
            //if(nl.item(i).getNodeType() == Node.ELEMENT_NODE){
            list.add(imgs.item(i));
            //}
        }

        for (Node e : list) {
            e.getParentNode().removeChild(e);
            this.fileSec.normalize();
            this.document.normalize();
        }
        this.fileSec.normalize();
        this.document.normalize();
        return true;
    }

    private boolean verifyFileGrpUUID(UUID id) {
        boolean flag = false;
        NodeList nl = this.fileSec.getElementsByTagName(XMLCarrierNS + ":fileGrp");
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nl.item(i);
                if (e.getAttribute("ID").equals(id.toString())) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    public void addFileGroup(FileGroup fgrp) throws DuplicateUUID {
        if (this.verifyFileGrpUUID(fgrp.getId())) {
            throw new DuplicateUUID("Already exists a fileGrp with the following UUID" + fgrp.getId());
        } else {
            Element e = this.document.createElement(XMLCarrierNS + ":fileGrp");
            e.setAttribute("ID", fgrp.getId().toString());
            e.setAttribute("USE", fgrp.getUse());
            this.fileSec.appendChild(e);

            if (fgrp.getList() != null) {
                for (ImageFile imgF : fgrp.getList()) {
                    if (!this.verifyFileInfo(imgF.getUuid())) {
                        Element file = this.document.createElement(XMLCarrierNS + ":file");
                        file.setAttribute("ID", imgF.getUuid());
                        file.setAttribute("MIMETYPE", imgF.getMimetype());
                        e.appendChild(file);
                        Element flocat = this.document.createElement(XMLCarrierNS + ":FLocat");
                        flocat.setAttribute(XLINK_ATTRIB_NAME + ":href", imgF.getUrl());
                        flocat.setAttribute("LOCTYPE", imgF.getLoctype());
                        file.appendChild(flocat);

                    } else {
                        throw new DuplicateUUID("Already exists an Image with the following UUID" + imgF.getUuid());
                    }
                }
            } else if (fgrp.getRefList() != null) {
                for (UUID u : fgrp.getRefList()) {
                    Element ref = this.document.createElement(XMLCarrierNS + ":fptr");
                    ref.setTextContent(u.toString());
                    e.appendChild(ref);
                }
            }
        }
    }

    private boolean verifyFileInfo(String uuid) {
        boolean exists = false;
        NodeList fileList = this.fileSec.getElementsByTagName(XMLCarrierNS + ":file");
        for (int i = 0; i < fileList.getLength(); i++) {
            if (fileList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) fileList.item(i);
                if (e.getAttribute("ID").equals(uuid)) {
                    exists = true;
                }
            }
        }
        return exists;
    }

    /**
     * This method adds a file group to the XML, given an UUID.
     *
     * @param id
     *      File group identification;
     * @param use
     *      String that represents the functionality of the file group.
     *
     * @throws DuplicateUUID
     * @see #appendFileRefToFileGrp(java.util.UUID, java.util.ArrayList)
     * @see #appendFileRefToFileGrp(java.util.UUID, java.util.UUID)
     *
     */
    public void addFileGroup(UUID id, String use)
            throws DuplicateUUID {
        if (this.verifyFileGrpUUID(id)) {
            throw new DuplicateUUID();
        } else {
            Element e = this.document.createElement(XMLCarrierNS + ":fileGrp");
            e.setAttribute("ID", id.toString());
            e.setAttribute("USE", use);
            this.fileSec.appendChild(e);
        }
    }

    public void addFileGroup(UUID id, String use, ArrayList<UUID> refList)
            throws DuplicateUUID {
        if (this.verifyFileGrpUUID(id)) {
            throw new DuplicateUUID();
        } else {
            Element e = this.document.createElement(XMLCarrierNS + ":fileGrp");
            e.setAttribute("ID", id.toString());
            e.setAttribute("USE", use);
            for (UUID u : refList) {
                Element ref = this.document.createElement(XMLCarrierNS + ":fptr");
                ref.setTextContent(u.toString());
                e.appendChild(ref);
            }
            this.fileSec.appendChild(e);
        }
    }

    /**
     * This method appends an ImageFile to an existing fileGroup.
     *
     * @param fileGrpId
     *      FileGroup unique identification.
     * @param file
     *      ImageFile
     * @see #appendFileRefToFileGrp(java.util.UUID, java.util.ArrayList)
     * @throws UUIDNotFound - if no fileGroup with the given UUID was found.
     */
    public void appendFileToFileGrp(UUID fileGrpId, ImageFile file)
            throws UUIDNotFound {

        if (this.verifyFileGrpUUID(fileGrpId)) {
            NodeList nl = this.fileSec.getElementsByTagName(XMLCarrierNS + ":fileGrp");
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) nl.item(i);
                    if (e.getAttribute("ID").equals(fileGrpId.toString())) {
                        Element f = this.document.createElement(XMLCarrierNS + ":file");
                        f.setAttribute("ID", file.getUuid());
                        f.setAttribute("MIMETYPE", file.getMimetype());
                        e.appendChild(f);
                        Element flocat = this.document.createElement(XMLCarrierNS + ":FLocat");
                        flocat.setAttribute(XLINK_ATTRIB_NAME + ":href", file.getUrl());
                        flocat.setAttribute("LOCTYPE", file.getLoctype());
                        f.appendChild(flocat);
                    }
                }
            }
        } else {
            throw new UUIDNotFound("UUID: " + fileGrpId + " does not exists");
        }

    }

    public void appendFileToFileGrp(UUID fileGrpId, ArrayList<ImageFile> list)
            throws UUIDNotFound {

        if (this.verifyFileGrpUUID(fileGrpId)) {
            NodeList nl = this.fileSec.getElementsByTagName(XMLCarrierNS + ":fileGrp");
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) nl.item(i);
                    if (e.getAttribute("ID").equals(fileGrpId.toString())) {
                        for (ImageFile imgF : list) {
                            Element f = this.document.createElement(XMLCarrierNS + ":file");
                            f.setAttribute("ID", imgF.getUuid());
                            f.setAttribute("MIMETYPE", imgF.getMimetype());
                            e.appendChild(f);

                            Element flocat = this.document.createElement(XMLCarrierNS + ":FLocat");
                            flocat.setAttribute(XLINK_ATTRIB_NAME + ":href", imgF.getUrl());
                            flocat.setAttribute("LOCTYPE", imgF.getLoctype());
                            f.appendChild(flocat);
                        }
                    }
                }
            }

        } else {
            throw new UUIDNotFound("UUID: " + fileGrpId + " does not exists");
        }

    }

    /**
     * This method removes all existing ImageFile refs in the given fileGrd and add the new references.
     *
     * @param fileGrpId
     * @param list
     * @throws XMLcarrier.Exceptions.UUIDNotFound
     */
    public void alterFileGrp(UUID fileGrpId, ArrayList<ImageFile> list)
            throws UUIDNotFound {

        if (this.verifyFileGrpUUID(fileGrpId)) {
            this.removeFileRefs(fileGrpId.toString());
            this.appendFileToFileGrp(fileGrpId, list);

        } else {
            throw new UUIDNotFound("UUID: " + fileGrpId + " does not exists");
        }

    }

    /**
     * This method adds a list of processes without the additional information.
     *
     * @param procList
     *            List of process.
     * @see #addProcess(XMLcarrier.Process)
     * @see Process
     */
    public void addProcesses(ArrayList<Process> procList) {
        for (Process p : procList) {
            Element proc = this.document.createElement(XMLCarrierNS + ":process");
            proc.setAttribute("STATUS", p.getStatus());
            proc.setAttribute("TYPE", p.getType());
            proc.setAttribute("SEQ", p.getSequenceNumber());
            proc.setAttribute("COMPID", p.getComponentID());
            proc.setAttribute("ID", p.getId());

            Element desc = this.document.createElement(XMLCarrierNS + ":description");
            desc.setTextContent(p.getDesc());
            proc.appendChild(desc);

            this.writeIOReferences(p, proc);
        }
    }

    /*private boolean removeFileRefs(String uuid) {
        NodeList nl = this.fileSec.getElementsByTagName(XMLCarrierNS + ":fileGrp");
        Element fileGrp = null;
        for (int a = 0; a < nl.getLength(); a++) {
            if (nl.item(a).getNodeType() == Element.ELEMENT_NODE) {
                Element aux = (Element) nl.item(a);
                if (aux.getAttribute("ID").equals(uuid)) {
                    fileGrp = (Element) nl.item(a);
                }
            }
        }

        if (fileGrp == null) {
            return false;
        }

        ArrayList<Node> list = new ArrayList<Node>();

        NodeList imgs = fileGrp.getChildNodes();
        for (int i = 0; i < imgs.getLength(); i++) {
            //if(nl.item(i).getNodeType() == Node.ELEMENT_NODE){
            list.add(imgs.item(i));
            //}
        }

        for (Node e : list) {
            e.getParentNode().removeChild(e);
            this.fileSec.normalize();
            this.document.normalize();
        }
        this.fileSec.normalize();
        this.document.normalize();
        return true;
    }*/    
    
    /**
     * This method SETS the list of processes to another without the additional information.
     *
     * @param procList
     *            List of process.
     * @see #addProcess(XMLcarrier.Process)
     * @see Process
     */
    public void setProcesses(ArrayList<Process> procList) {
        NodeList nl = this.processes.getElementsByTagName(XMLCarrierNS + ":process");

        ArrayList<Node> list = new ArrayList();

        for(int i=0; i < nl.getLength(); i++)
        {
            list.add(nl.item(i));
        }

        for (Node n : list)
        {
            n.getParentNode().removeChild(n);
            //this.processes.normalize();
            //this.document.normalize();
        }
        
        for(Process p : procList)
        {
            //if (p.getType().equals("PIPELINE"))
            //{

            //}
            addProcess(p);
        }
        //processes.

        //this.addProcesses(procList);
        //this.processes.normalize();
        //this.document.normalize();
    }

    /**
     * This method writes a single process with the additional info.
     *
     * @param p
     *            Process to be added.
     * @see Process.
     */
    public void addProcess(Process p) {
        Element proc = this.document.createElement(XMLCarrierNS + ":process");
        proc.setAttribute("STATUS", p.getStatus());
        proc.setAttribute("TYPE", p.getType());
        proc.setAttribute("SEQ", p.getSequenceNumber());
        proc.setAttribute("COMPID", p.getComponentID());
        proc.setAttribute("ID", p.getId());
        this.processes.appendChild(proc);

        Element desc = this.document.createElement(XMLCarrierNS + ":description");
        desc.setTextContent(p.getDesc());
        proc.appendChild(desc);


        Element input = this.document.createElement(XMLCarrierNS + ":Input");
        proc.appendChild(input);

        for (String s : p.getStageI().getInputRef().keySet()) {
            Element ref = this.document.createElement(XMLCarrierNS + ":ref");
            ref.setAttribute("STATUS", "ACTIVE");
            ref.setAttribute("NAME", s);
            ref.setTextContent(p.getStageI().getInputRef().get(s));
            input.appendChild(ref);
        }

        Element output = this.document.createElement(XMLCarrierNS + ":Output");
        proc.appendChild(output);

        for (String s : p.getStageI().getOutputRef().keySet()) {
            Element ref = this.document.createElement(XMLCarrierNS + ":ref");
            ref.setAttribute("STATUS", "ACTIVE");
            ref.setAttribute("NAME", s);
            ref.setTextContent(p.getStageI().getOutputRef().get(s));
            output.appendChild(ref);
        }

    }

    /**
     * This method adds a single input reference to an existing process.
     *
     * @param id
     *            The process identification.
     * @param name
     *          Reference name
     * @param ref
     *            The reference to be added.
     * @throws UUIDNotFound - if there isn't a process with the given identification.
     */
    public void addInputReference(String id, String name, String ref)
            throws UUIDNotFound {

        NodeList nl = this.document.getElementsByTagName(XMLCarrierNS + ":process");
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nl.item(i);
                if (e.getAttribute("ID").equals(id)) {
                    NodeList in = e.getElementsByTagName(XMLCarrierNS + ":Input");
                    if (!(in.getLength() == 0)) {
                        Element inE = (Element) in.item(0);

                        Element reff = this.document.createElement(XMLCarrierNS + ":ref");
                        reff.setAttribute("STATUS", "ACTIVE");
                        reff.setAttribute("NAME", name);
                        reff.setTextContent(ref);
                        inE.appendChild(reff);
                    } else {
                        Element input = this.document.createElement(XMLCarrierNS + ":Input");
                        e.appendChild(input);

                        Element reff = this.document.createElement(XMLCarrierNS + ":ref");
                        reff.setAttribute("STATUS", "ACTIVE");
                        reff.setAttribute("NAME", name);
                        reff.setTextContent(ref);
                        input.appendChild(reff);
                    }
                }
            }
        }
    }

    public void addNewInputRef(String id, TreeMap<String, UUID> map)
            throws UUIDNotFound {

        NodeList nl = this.document.getElementsByTagName(XMLCarrierNS + ":process");
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nl.item(i);
                if (e.getAttribute("ID").equals(id)) {
                    NodeList in = e.getElementsByTagName(XMLCarrierNS + ":Input");
                    if (!(in.getLength() == 0)) {
                        Element inE = (Element) in.item(0);
                        NodeList list = inE.getElementsByTagName(XMLCarrierNS + ":ref");
                        if (list != null && list.getLength() != 0) {
                            for (int ae = 0; ae < list.getLength(); ae++) {
                                if (list.item(ae).getNodeType() == Node.ELEMENT_NODE) {
                                    Element refAux = (Element) list.item(ae);
                                    refAux.setAttribute("STATUS", "INACTIVE");
                                }
                            }
                        }
                        for (String s : map.keySet()) {
                            Element reff = this.document.createElement(XMLCarrierNS + ":ref");
                            reff.setAttribute("STATUS", "ACTIVE");
                            reff.setAttribute("NAME", s);
                            reff.setTextContent(map.get(s).toString());
                            inE.appendChild(reff);
                        }
                    } else {
                        Element input = this.document.createElement(XMLCarrierNS + ":Input");
                        e.appendChild(input);

                        for (String s : map.keySet()) {
                            Element reff = this.document.createElement(XMLCarrierNS + ":ref");
                            reff.setAttribute("STATUS", "ACTIVE");
                            reff.setAttribute("NAME", s);
                            reff.setTextContent(map.get(s).toString());
                            input.appendChild(reff);
                        }
                    }
                }
            }
        }
    }

    public void addNewOutputRef(String id, TreeMap<String, UUID> map)
            throws UUIDNotFound {

        NodeList nl = this.document.getElementsByTagName(XMLCarrierNS + ":process");
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nl.item(i);
                if (e.getAttribute("ID").equals(id)) {
                    NodeList in = e.getElementsByTagName(XMLCarrierNS + ":Output");
                    if (!(in.getLength() == 0)) {
                        Element inE = (Element) in.item(0);
                        NodeList list = inE.getElementsByTagName(XMLCarrierNS + ":ref");
                        if (list != null && list.getLength() != 0) {
                            for (int ae = 0; ae < list.getLength(); ae++) {
                                if (list.item(ae).getNodeType() == Node.ELEMENT_NODE) {
                                    Element refAux = (Element) list.item(ae);
                                    refAux.setAttribute("STATUS", "INACTIVE");
                                }
                            }
                        }
                        for (String s : map.keySet()) {
                            Element reff = this.document.createElement(XMLCarrierNS + ":ref");
                            reff.setAttribute("STATUS", "ACTIVE");
                            reff.setAttribute("NAME", s);
                            reff.setTextContent(map.get(s).toString());
                            inE.appendChild(reff);
                        }
                    } else {
                        Element input = this.document.createElement(XMLCarrierNS + ":Output");
                        e.appendChild(input);

                        for (String s : map.keySet()) {
                            Element reff = this.document.createElement(XMLCarrierNS + ":ref");
                            reff.setAttribute("STATUS", "ACTIVE");
                            reff.setAttribute("NAME", s);
                            reff.setTextContent(map.get(s).toString());
                            input.appendChild(reff);
                        }
                    }
                }
            }
        }
    }

    /**
     * This method adds a single output reference to an existing process.
     *
     * @param id
     *            The process identification.
     * @param name
     *          Reference name
     * @param ref
     *            The reference to be added.
     * @throws UUIDNotFound - if there isn't a process with the given identification.
     */
    public void addOutputReference(String id, String name, String ref) {
        NodeList nl = this.document.getElementsByTagName(XMLCarrierNS + ":process");
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nl.item(i);
                if (e.getAttribute("ID").equals(id)) {
                    NodeList in = e.getElementsByTagName(XMLCarrierNS + ":Output");
                    if (!(in.getLength() == 0)) {
                        Element outE = (Element) in.item(0);

                        Element reff = this.document.createElement(XMLCarrierNS + ":ref");
                        reff.setAttribute("STATUS", "ACTIVE");
                        reff.setAttribute("NAME", name);
                        reff.setTextContent(ref);
                        outE.appendChild(reff);
                    } else {
                        Element output = this.document.createElement(XMLCarrierNS + ":Output");
                        e.appendChild(output);

                        Element reff = this.document.createElement(XMLCarrierNS + ":ref");
                        reff.setAttribute("STATUS", "ACTIVE");
                        reff.setAttribute("NAME", name);
                        reff.setTextContent(ref);
                        output.appendChild(reff);
                    }
                }
            }
        }
    }

    /**
     * This method returns a list of Input references given a component
     * identification.
     *
     * @param id
     *      Component Identification
     * @return
     *      List of Input references in the form <name,value>.
     * @throws UnknownProcessID - if there isn't a process with the given component identification.
     */
    public TreeMap<String, String> getInputReferences(String id)
            throws UnknownProcessID {

        boolean flag = false;
        TreeMap<String, String> list = new TreeMap<String, String>();
        NodeList nl = this.document.getElementsByTagName(XMLCarrierNS + ":process");
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nl.item(i);
                if (e.getAttribute("COMPID").equals(id)) {
                    flag = true;
                    NodeList in = e.getElementsByTagName(XMLCarrierNS + ":Input");
                    if (!(in.getLength() == 0)) {
                        Element inE = (Element) in.item(0);
                        NodeList refs = inE.getElementsByTagName(XMLCarrierNS + ":ref");
                        for (int k = 0; k < refs.getLength(); k++) {
                            if (refs.item(k).getNodeType() == Node.ELEMENT_NODE) {
                                Element ee = (Element) refs.item(k);
                                if (ee.getAttribute("STATUS").equals("ACTIVE")) {
                                    list.put(ee.getAttribute("NAME"), ee.getTextContent());
                                }
                            }
                        }
                    }
                }
            }
        }
        if (flag == true) {
            return list;
        } else {
            throw new UnknownProcessID();
        }
    }

    /**
     * This method returns a list of Output references given a component
     * identification.
     *
     * @param id
     *      Component Identification
     * @return
     *      List of Output references in the form <name,value>.
     * @throws UnknownProcessID - if there isn't a process with the given component identification.
     */
    public TreeMap<String, String> getOutputReferences(String id)
            throws UnknownProcessID {

        boolean flag = false;
        TreeMap<String, String> list = new TreeMap<String, String>();
        NodeList nl = this.document.getElementsByTagName(XMLCarrierNS + ":process");
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nl.item(i);
                if (e.getAttribute("COMPID").equals(id)) {
                    flag = true;
                    NodeList in = e.getElementsByTagName(XMLCarrierNS + ":Output");
                    if (!(in.getLength() == 0)) {
                        Element inE = (Element) in.item(0);
                        NodeList refs = inE.getElementsByTagName(XMLCarrierNS + ":ref");
                        for (int k = 0; k < refs.getLength(); k++) {
                            if (refs.item(k).getNodeType() == Node.ELEMENT_NODE) {
                                Element ee = (Element) refs.item(k);
                                if (ee.getAttribute("STATUS").equals("ACTIVE")) {
                                    list.put(ee.getAttribute("NAME"), ee.getTextContent());
                                }
                            }
                        }
                    }
                }
            }
        }
        if (flag == true) {
            return list;
        } else {
            throw new UnknownProcessID();
        }
    }

    /**
     * This method reads an Element and returns a Process, should it be a valid
     * Process node
     *
     * @param e
     * @return
     */
    public Process readProcessFromElement(Element e)
    // <editor-fold defaultstate="collapsed" desc="Beta code">
    {
        Process p = new Process();

        p.setId(e.getAttribute("ID"));
        String compId = e.getAttribute("COMPID");
        p.setComponentID(compId);

        String seq = e.getAttribute("SEQ");
        p.setSequenceNumber(seq);

        String status = e.getAttribute("STATUS");
        p.setStatus(status);

        String type = e.getAttribute("TYPE");
        p.setType(type);
        Element desc = (Element) e.getElementsByTagName(XMLCarrierNS + ":description").item(0);
        p.setDesc(desc.getTextContent());

        Element in = (Element) e.getElementsByTagName(XMLCarrierNS + ":Input").item(0);
        if (in != null) {
            NodeList nlIn = in.getChildNodes();
            for (int j = 0; j < nlIn.getLength(); j++) {
                if (nlIn.item(j).getNodeType() == Node.ELEMENT_NODE) {
                    Element refIn = (Element) nlIn.item(j);
                    if (refIn.getNodeName().equals(XMLCarrierNS + ":ref")) {
                        p.addInput(refIn.getAttribute("NAME"), refIn.getTextContent());
                    }
                }
            }
        }
        Element out = (Element) e.getElementsByTagName(XMLCarrierNS + ":Output").item(0);
        if (out != null) {
            NodeList nlOut = out.getChildNodes();
            for (int k = 0; k < nlOut.getLength(); k++) {
                if (nlOut.item(k).getNodeType() == Node.ELEMENT_NODE) {
                    Element refOut = (Element) nlOut.item(k);
                    if (refOut.getNodeName().equals(XMLCarrierNS + ":ref")) {
                        p.addOutput(refOut.getAttribute("NAME"), refOut.getTextContent());
                    }
                }
            }
        }

        return p;
    }
    // </editor-fold>

    /**
     * This method returns a list of all Processes in the XMLcarrier
     *
     * @return
     */
    public ArrayList<Process> getAllProcesses()
    {
        ArrayList<Process> list = new ArrayList();

        NodeList nl = this.document.getElementsByTagName(XMLCarrierNS + ":process");

        // For each Node Element...
        for (int i = 0; i < nl.getLength(); i++)
        {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE)
            {
                // ... read a Process from it!
                list.add(readProcessFromElement((Element)nl.item(i)));
            }
        }

        // Then return the list
        return list;
    }

    /**
     * This method returns a process whose identification matches the given id.
     *
     * @param id
     *      Process identification.
     * @return
     *      A new Process.
     * @see Process
     */
    public Process getProcessById(String id) {
        Process p = new Process();

        NodeList nl = this.document.getElementsByTagName(XMLCarrierNS + ":process");
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nl.item(i);
                if (e.getAttribute("ID").equals(id)) {
                    p.setId(id);
                    String compId = e.getAttribute("COMPID");
                    p.setComponentID(compId);

                    String seq = e.getAttribute("SEQ");
                    p.setSequenceNumber(seq);

                    String status = e.getAttribute("STATUS");
                    p.setStatus(status);

                    String type = e.getAttribute("TYPE");
                    p.setType(type);
                    Element desc = (Element) e.getElementsByTagName(XMLCarrierNS + ":description").item(0);
                    p.setDesc(desc.getTextContent());

                    Element in = (Element) e.getElementsByTagName(XMLCarrierNS + ":Input").item(0);
                    if (in != null) {
                        NodeList nlIn = in.getChildNodes();
                        for (int j = 0; j < nlIn.getLength(); j++) {
                            if (nlIn.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                Element refIn = (Element) nlIn.item(j);
                                if (refIn.getNodeName().equals(XMLCarrierNS + ":ref")) {
                                    p.addInput(refIn.getAttribute("NAME"), refIn.getTextContent());
                                }
                            }
                        }
                    }
                    Element out = (Element) e.getElementsByTagName(XMLCarrierNS + ":Output").item(0);
                    if (out != null) {
                        NodeList nlOut = out.getChildNodes();
                        for (int k = 0; k < nlOut.getLength(); k++) {
                            if (nlOut.item(k).getNodeType() == Node.ELEMENT_NODE) {
                                Element refOut = (Element) nlOut.item(k);
                                if (refOut.getNodeName().equals(XMLCarrierNS + ":ref")) {
                                    p.addInput(refOut.getAttribute("NAME"), refOut.getTextContent());
                                }
                            }
                        }
                    }

                }
            }
        }
        return p;
    }

    /**
     * This method sets the process's component ID given it's ID.
     *
     * @param pid
     *      Process Identification.
     * @param id
     *      Process component identification.
     * @see Process
     */
    public void setProcessComponentID(String pid, String id) {
        NodeList nl = this.processes.getElementsByTagName(XMLCarrierNS + ":process");
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element proc = (Element) nl.item(i);
                if (proc.getAttribute("ID").equals(pid)) {
                    proc.setAttribute("COMPID", id);
                }
            }
        }
    }

    /**
     * This method returns a process's component ID given it's unique identifier.
     *
     * @param id
     *      Process's identification.
     * @return
     *      Process's component id.
     */
    public String getProcessCOMPIDByID(String id) {
        String compid = "";
        NodeList nl = this.processes.getElementsByTagName(XMLCarrierNS + ":process");
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nl.item(i);
                if (e.getAttribute("ID").equals(id)) {
                    compid = e.getAttribute("COMPID");
                }
            }
        }
        return compid;
    }

    private void writeProcessHeader(Process p, Element proc, Element place) {
        proc.setAttribute("STATUS", p.getStatus());
        proc.setAttribute("TYPE", p.getType());
        proc.setAttribute("ID", p.getId());
        place.appendChild(proc);
        Element desc = this.document.createElement(XMLCarrierNS + ":description");
        desc.setTextContent(p.getDesc());
        proc.appendChild(desc);
        Element info = this.document.createElement(XMLCarrierNS + ":Info");
        info.setAttribute("VERSION", p.getStageI().getVersion());
        proc.appendChild(info);
        Element userInfo = this.document.createElement(XMLCarrierNS + ":UserInfo");
        userInfo.setTextContent(p.getStageI().getUserInfo());
        info.appendChild(userInfo);
        Element date = this.document.createElement(XMLCarrierNS + ":Date");
        date.setAttribute("STARTED", p.getStageI().getDateS());
        date.setAttribute("DATEFINISHED", p.getStageI().getDateF());
        info.appendChild(date);
        Element host = this.document.createElement(XMLCarrierNS + ":Host");
        host.setTextContent(p.getStageI().getHost());
        info.appendChild(host);
        Element mac = this.document.createElement(XMLCarrierNS + ":MacAddress");
        mac.setTextContent(p.getStageI().getMacAddr());
        info.appendChild(mac);
    }

    private void writeIOReferences(Process p, Element e) {
        Element input = this.document.createElement(XMLCarrierNS + ":Input");
        e.appendChild(input);
        if (!(p.getStageI().getInputRef().size() == 0)) {
            for (String s : p.getStageI().getInputRef().keySet()) {
                Element ref = this.document.createElement(XMLCarrierNS + ":ref");
                ref.setAttribute("NAME", s);
                ref.setTextContent(p.getStageI().getInputRef().get(s));
                input.appendChild(ref);
            }
        }

        Element output = this.document.createElement(XMLCarrierNS + ":Output");
        e.appendChild(output);
        if (!(p.getStageI().getOutputRef().size() == 0)) {
            for (String s : p.getStageI().getOutputRef().keySet()) {
                Element ref = this.document.createElement(XMLCarrierNS + ":ref");
                ref.setAttribute("NAME", s);
                ref.setTextContent(p.getStageI().getOutputRef().get(s));
                output.appendChild(ref);
            }
        }

    }

    /**
     * This method adds a subprocess given a specific parent identification.
     *
     * @param parentID
     *            The parent IDentification;
     * @param p
     *            The process to be added;
     * @deprecated
     */
    public void addProcess(String parentID, Process p) {
        Element fork = null;
        Element parent = null;
        NodeList nl = this.processes.getElementsByTagName(XMLCarrierNS + ":process");
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                parent = (Element) nl.item(i);
                if (parent.getAttribute("ID").equals(parentID)) {
                    NodeList forkList = parent.getElementsByTagName(XMLCarrierNS + ":fork");
                    if (forkList.getLength() == 0) {
                        fork = this.document.createElement(XMLCarrierNS + ":fork");
                        parent.appendChild(fork);
                    } else {
                        fork = (Element) forkList.item(0);
                    }
                }
            }
        }
        if (fork != null) {
            Element proc = this.document.createElement(XMLCarrierNS + ":process");
            this.writeProcessHeader(p, proc, fork);
            this.writeIOReferences(p, proc);
        }
    }

    /**
     * This method adds a single Data.
     *
     * @param d
     *      Data to be added.
     * @see Data
     * @see #addData(java.util.ArrayList)
     *
     */
    public void addData(Data d) {
        TreeMap<String, String> tm;
        Element data = this.document.createElement(XMLCarrierNS + ":Data");
        tm = d.getAllAttribs();
        for (String str : tm.keySet()) {
            data.setAttribute(str, tm.get(str));
        }
        data.setAttribute("NAME", d.getName());
        data.setAttribute("UUID", d.getId().toString());
        this.messagePassing.appendChild(data);

        Element desc = this.document.createElement(XMLCarrierNS + ":description");
        desc.setTextContent(d.getDescription());
        data.appendChild(desc);

        for (Parameter p : d.getParams()) {
            Element param = this.document.createElement(XMLCarrierNS + ":param");
            param.setAttribute("NAME", p.getName());
            param.setAttribute("TYPE", p.getType());
            param.setTextContent(String.valueOf(p.getValue()));
            data.appendChild(param);
            /*Element val = this.document.createElement(XMLCarrierNS + ":value");
            val.setTextContent(String.valueOf(p.getValue()));
            param.appendChild(val);*/
        }


    }

    /**
     * This method adds a list of Data(s).
     *
     * @param a
     *      List of Data(s) to be added.
     * @see Data
     * @see #addData(XMLcarrier.Data)
     */
    public void addData(ArrayList<Data> a) {
        TreeMap<String, String> tm;

        for (Data d : a) {
            Element data = this.document.createElement(XMLCarrierNS + ":Data");
            tm = d.getAllAttribs();

            for (String str : tm.keySet()) {
                data.setAttribute(str, tm.get(str));
            }

            data.setAttribute("NAME", d.getName());
            data.setAttribute("UUID", d.getId().toString());
            this.messagePassing.appendChild(data);

            Element desc = this.document.createElement(XMLCarrierNS + ":description");
            desc.setTextContent(d.getDescription());
            data.appendChild(desc);

            for (Parameter p : d.getParams()) {
                Element param = this.document.createElement(XMLCarrierNS + ":param");
                param.setAttribute("NAME", p.getName());
                param.setAttribute("TYPE", p.getType());
                param.setTextContent(String.valueOf(p.getValue()));
                data.appendChild(param);
                /*Element val = this.document.createElement(XMLCarrierNS + ":value");
                val.setTextContent(p.getValueString());
                param.appendChild(val);*/
            }
        }

    }

    /**
     * This method is intended to be used only for the addition of Data(s) that defines Areas.
     *
     *
     * @param a
     *      Data to be added.
     * @see #addData(XMLcarrier.Data)
     */
    public void addAreaData(Data a) {
        TreeMap<String, String> tm;
        TreeMap<String, String> areaParams;
        Element data = this.document.createElement(XMLCarrierNS + ":Data");
        tm = a.getAllAttribs();
        for (String str : tm.keySet()) {
            data.setAttribute(str, tm.get(str));
        }
        data.setAttribute("NAME", a.getName());
        data.setAttribute("UUID", a.getId().toString());
        this.messagePassing.appendChild(data);

        Element desc = this.document.createElement(XMLCarrierNS + ":description");
        desc.setTextContent(a.getDescription());
        data.appendChild(desc);

        for (AreaInfo area : a.getAreas()) {
            Element param = this.document.createElement(XMLCarrierNS + ":area");
            param.setAttribute("UUID", area.getAreaId().toString());
            param.setAttribute("fileGrpID", area.getFileGroupID().toString());
            param.setAttribute("SHAPE", area.getShape());
            param.setAttribute("BEGIN", area.getBeginString());
            param.setAttribute("COORDS", area.getCoordsString());
            param.setAttribute("END", area.getEndString());
            areaParams = area.getAllAttributes();
            for (String str : areaParams.keySet()) {
                param.setAttribute(str, areaParams.get(str));
            }
            data.appendChild(param);
        }

    }

    public void modifyAreaDataByUUID(Data d) {
        this.removeDataByUUID(d.getId().toString());
        this.addAreaData(d);

    }

    private boolean removeDataByUUID(String uuid) {
        Data d = null;
        NodeList firstChilds;
        ArrayList<Node> eFirst = new ArrayList<Node>();
        Element dataRef = null;
        Node toRemove = null;
        try {
            /*
             * Need to find out the "reference" to the data that will be removed.
             *
             */
            firstChilds = this.messagePassing.getChildNodes();
            for (int i = 0; i < firstChilds.getLength(); i++) {
                if (firstChilds.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    /*
                     * Time to search for the uuid
                     *
                     */
                    dataRef = (Element) firstChilds.item(i);
                    if (dataRef.getAttribute("UUID").equals(uuid)) {
                        /*
                         * We found it! We have to find out what kind of area we are dealing.
                         *
                         */
                        toRemove = firstChilds.item(i);
                        NodeList isArea = dataRef.getElementsByTagName(XMLCarrierNS + ":area");

                        if (isArea.getLength() != 0) {
                            /*
                             * Data with Areas...Easiest to remove!
                             *
                             *
                             */
                            NodeList areas = dataRef.getChildNodes();
                            for (int k = 0; k < areas.getLength(); k++) {
                                eFirst.add(areas.item(k));
                            }

                        } else {
                            /*
                             * Data with Parameters
                             *
                             */
                            NodeList params = dataRef.getChildNodes();
                            for (int a = 0; a < params.getLength(); a++) {

                                eFirst.add(params.item(a));

                            }

                        }

                    }
                }
            }

            for (Node e : eFirst) {
                e.getParentNode().removeChild(e);
                //this.header.normalize();
                this.data.normalize();
                this.document.normalize();
            }
            toRemove.getParentNode().removeChild(toRemove);
            //this.header.normalize();
            this.data.normalize();
            this.document.normalize();

        } catch (Exception e) {
            //TODO handle this exception
            return false;
        }

        return true;

    }

    /**
     * This method returns all the data referenced by a process(input subsection), given it's Component ID.
     *
     * @param id
     *      Process Identification.
     * @throws
     */
    public ArrayList<Data> getInput(String id) throws UUIDNotFound,
            UnknownProcessID {

        boolean flag = false;
        ArrayList<Data> dataList = new ArrayList<Data>();
        NodeList interfaceProcessList = this.document.getElementsByTagName(XMLCarrierNS + ":process");
        NodeList inputRefs = null;
        for (int i = 0; i < interfaceProcessList.getLength(); i++) {
            if (interfaceProcessList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) interfaceProcessList.item(i);
                if (e.getAttribute("COMPID").equals(id)) {
                    flag = true;
                    NodeList temp = e.getElementsByTagName(XMLCarrierNS + ":Input");
                    Element inputTemp = (Element) temp.item(0);
                    inputRefs = inputTemp.getElementsByTagName(XMLCarrierNS + ":ref");
                    for (int j = 0; j < inputRefs.getLength(); j++) {
                        if (inputRefs.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            Element eTemp = (Element) inputRefs.item(j);
                            try {
                                dataList.add(this.getDataByUUID(eTemp.getTextContent()));
                            } catch (UUIDNotFound var) {
                                throw new UUIDNotFound();
                            }

                        }
                    }
                }
            }
        }
        if (flag == true) {
            return dataList;
        } else {
            throw new UnknownProcessID();
        }
    }

    /**
     * This method returns all the data referenced by a process(input subsection), given it's Process ID.
     *
     * @param id
     *      Process Identification.
     * @throws
     */
    public ArrayList<Data> getInputByProcessID(String id) throws UUIDNotFound,
            UnknownProcessID {

        boolean flag = false;
        ArrayList<Data> dataList = new ArrayList<Data>();
        NodeList interfaceProcessList = this.document.getElementsByTagName(XMLCarrierNS + ":process");
        NodeList inputRefs = null;
        for (int i = 0; i < interfaceProcessList.getLength(); i++) {
            if (interfaceProcessList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) interfaceProcessList.item(i);
                if (e.getAttribute("ID").equals(id)) {
                    flag = true;
                    NodeList temp = e.getElementsByTagName(XMLCarrierNS + ":Input");
                    Element inputTemp = (Element) temp.item(0);
                    inputRefs = inputTemp.getElementsByTagName(XMLCarrierNS + ":ref");
                    for (int j = 0; j < inputRefs.getLength(); j++) {
                        if (inputRefs.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            Element eTemp = (Element) inputRefs.item(j);
                            try {
                                dataList.add(this.getDataByUUID(eTemp.getTextContent()));
                            } catch (UUIDNotFound var) {
                                throw new UUIDNotFound();
                            }

                        }
                    }
                }
            }
        }
        if (flag == true) {
            return dataList;
        } else {
            throw new UnknownProcessID();
        }
    }

    /**
     * This method returns all the data referenced by a process(output subsection), given it's ID.
     *
     * @param id
     *      Process Identification.
     * @throws
     */
    public ArrayList<Data> getOutput(String id) throws
            UnknownProcessID, UUIDNotFound {

        boolean flag = false;
        ArrayList<Data> dataList = new ArrayList<Data>();
        NodeList interfaceProcessList = this.document.getElementsByTagName(XMLCarrierNS + ":process");
        NodeList outputRefs = null;
        for (int i = 0; i < interfaceProcessList.getLength(); i++) {
            if (interfaceProcessList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) interfaceProcessList.item(i);
                if (e.getAttribute("COMPID").equals(id)) {
                    flag = true;
                    NodeList temp = e.getElementsByTagName(XMLCarrierNS + ":Output");
                    Element inputTemp = (Element) temp.item(0);
                    outputRefs = inputTemp.getElementsByTagName(XMLCarrierNS + ":ref");
                    for (int j = 0; j < outputRefs.getLength(); j++) {
                        if (outputRefs.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            Element eTemp = (Element) outputRefs.item(j);
                            try {
                                dataList.add(this.getDataByUUID(eTemp.getTextContent()));
                            } catch (UUIDNotFound uu) {
                                throw new UUIDNotFound();
                            }
                        }
                    }
                }
            }
        }
        if (flag == true) {
            return dataList;
        } else {
            throw new UnknownProcessID();
        }
    }

    /**
     * This method returns all the data referenced by a process(output subsection), given it's Process ID.
     *
     * @param id
     *      Process Identification.
     * @throws
     */
    public ArrayList<Data> getOutputByProcessID(String id) throws
            UnknownProcessID, UUIDNotFound {

        boolean flag = false;
        ArrayList<Data> dataList = new ArrayList<Data>();
        NodeList interfaceProcessList = this.document.getElementsByTagName(XMLCarrierNS + ":process");
        NodeList outputRefs = null;
        for (int i = 0; i < interfaceProcessList.getLength(); i++) {
            if (interfaceProcessList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) interfaceProcessList.item(i);
                if (e.getAttribute("ID").equals(id)) {
                    flag = true;
                    NodeList temp = e.getElementsByTagName(XMLCarrierNS + ":Output");
                    Element inputTemp = (Element) temp.item(0);
                    outputRefs = inputTemp.getElementsByTagName(XMLCarrierNS + ":ref");
                    for (int j = 0; j < outputRefs.getLength(); j++) {
                        if (outputRefs.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            Element eTemp = (Element) outputRefs.item(j);
                            try {
                                dataList.add(this.getDataByUUID(eTemp.getTextContent()));
                            } catch (UUIDNotFound uu) {
                                throw new UUIDNotFound();
                            }
                        }
                    }
                }
            }
        }
        if (flag == true) {
            return dataList;
        } else {
            throw new UnknownProcessID();
        }
    }

    /**
     * This method returns an ArrayList containing all FileGroups.
     *
     * @return
     */
    public ArrayList<FileGroup> getAllFileGroups()
    {
        ArrayList<FileGroup> groups = new ArrayList();

        NodeList nl = this.fileSec.getElementsByTagName(XMLCarrierNS + ":fileGrp");
        for (int j = 0; j < nl.getLength(); j++)
        {
            if (nl.item(j).getNodeType() == Node.ELEMENT_NODE)
            {
                // It's a file group. Let's get the stuff
                String ID = ((Element) nl.item(j)).getAttribute("ID");
                String use = ((Element) nl.item(j)).getAttribute("USE");
                UUID id;
                id = UUID.fromString(ID);
                ArrayList<ImageFile> images;
                ArrayList<UUID> uuids;
                try {
                     images = this.getImageList(id);
                     uuids = this.getImagePtr(id);
                     FileGroup fg = new FileGroup();
                     fg.setList(images);
                     fg.setRefList(uuids);
                     fg.setId(id);
                     fg.setUse(use);
                     groups.add(fg);
                } catch (UUIDNotFound ex) {
                    Logger.getLogger(XMLHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return groups;
    }

    public FileGroup getFileGroup(UUID filegroupID)
    {
        ArrayList<FileGroup> fgs = getAllFileGroups();

        FileGroup r = null;

        for (FileGroup f : fgs)
        {
            if (r == null && f.getId().equals(filegroupID))
                r = f;
        }

        return r;
    }

    /**
     * This method returns an ImageFile list given a fileGrp id.
     *
     * @param id
     *        FileGroup unique identification.
     * @return
     *      List of ImageFile(s).
     * @see ImageFile
     * @throws
     */
    public ArrayList<ImageFile> getImageList(UUID id) throws UUIDNotFound {

        ArrayList<ImageFile> list = new ArrayList<ImageFile>();

        if (this.verifyFileGrpUUID(id)) {
            NodeList nl = this.fileSec.getElementsByTagName(XMLCarrierNS + ":fileGrp");
            for (int j = 0; j < nl.getLength(); j++) {
                if (nl.item(j).getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) nl.item(j);
                    if (e.getAttribute("ID").equals(id.toString())) {
                        NodeList fileList = e.getElementsByTagName(XMLCarrierNS + ":file");
                        for (int k = 0; k < fileList.getLength(); k++) {
                            if (fileList.item(k).getNodeType() == Node.ELEMENT_NODE) {
                                Element f = (Element) fileList.item(k);
                                String uuid = f.getAttribute("ID");
                                String mime = f.getAttribute("MIMETYPE");
                                NodeList flocatList = f.getElementsByTagName(XMLCarrierNS + ":FLocat");
                                for (int a = 0; a < flocatList.getLength(); a++) {
                                    if (flocatList.item(a).getNodeType() == Node.ELEMENT_NODE) {
                                        Element flocat = (Element) flocatList.item(a);
                                        String url = flocat.getAttribute(XLINK_ATTRIB_NAME + ":href");
                                        String loc = flocat.getAttribute("LOCTYPE");
                                        list.add(new ImageFile(mime, url, uuid, "", loc));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return list;
        } else {
            throw new UUIDNotFound("UUID: " + id.toString() + " not found!!");
        }
    }

    public ArrayList<UUID> getImagePtr(UUID fileGrpID) {
        ArrayList<UUID> list = new ArrayList<UUID>();
        NodeList nl = this.fileSec.getElementsByTagName(XMLCarrierNS + ":fileGrp");
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nl.item(i);
                if (e.getAttribute("ID").equals(fileGrpID.toString())) {
                    NodeList refList = e.getElementsByTagName(XMLCarrierNS + ":fptr");
                    for (int j = 0; j < refList.getLength(); j++) {
                        if (refList.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            Element ref = (Element) refList.item(j);
                            list.add(UUID.fromString(ref.getTextContent()));
                        }
                    }
                }
            }
        }
        return list;
    }

    /**
     * This method returns a list of ImageFile(s) that contains all images in the working set.
     *
     * @return
     *      List of ImageFile in the working set.
     * @see ImageFile
     */
    public ArrayList<ImageFile> getImageList() {
        ArrayList<ImageFile> list = new ArrayList<ImageFile>();

        NodeList nl = this.fileSec.getElementsByTagName(XMLCarrierNS + ":file");
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nl.item(i);
                String uuid = e.getAttribute("ID");
                String mime = e.getAttribute("MIMETYPE");
                NodeList flocatList = e.getElementsByTagName(XMLCarrierNS + ":FLocat");
                for (int j = 0; j < flocatList.getLength(); j++) {
                    if (flocatList.item(j).getNodeType() == Node.ELEMENT_NODE) {
                        Element flocat = (Element) flocatList.item(j);
                        String url = flocat.getAttribute(XLINK_ATTRIB_NAME + ":href");
                        String loc = flocat.getAttribute("LOCTYPE");
                        list.add(new ImageFile(mime, url, uuid, "", loc));
                    }
                }
            }
        }
        return list;
    }

    /**
     * This method returns an ImageFile given an image's UUID.
     *
     * @param uuid
     *      Image's UUID.
     * @return
     *      ImageFile
     * @throws UUIDNotFound - if no image was found with the given UUID.
     * @see ImageFile
     */
    public ImageFile getImageByUUID(UUID uuid) throws UUIDNotFound {

        if (this.verifyFileInfo(uuid.toString())) {
            NodeList nl = this.fileSec.getElementsByTagName(XMLCarrierNS + ":file");
            for (int j = 0; j < nl.getLength(); j++) {
                if (nl.item(j).getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) nl.item(j);
                    String id = e.getAttribute("ID");
                    String mime = e.getAttribute("MIMETYPE");
                    if (id.equals(uuid.toString())) {
                        NodeList flocatList = e.getElementsByTagName(XMLCarrierNS + ":FLocat");
                        for (int a = 0; a < flocatList.getLength(); a++) {
                            if (flocatList.item(a).getNodeType() == Node.ELEMENT_NODE) {
                                Element flocat = (Element) flocatList.item(a);
                                String loc = flocat.getAttribute("LOCTYPE");
                                String url = flocat.getAttribute(XLINK_ATTRIB_NAME + ":href");
                                return new ImageFile(mime, url, id, "", loc);
                            }
                        }
                    }

                }
            }
        } else {
            throw new UUIDNotFound("UUID: " + uuid.toString() + " not found!!");
        }
        return null;
    }

    /**
     * This methods returns a Data given an UUID.
     *
     * @param uuid
     *      Data's UUID.
     * @return
     *      New Data.
     * @throws UUIDNotFound
     * @see Data
     */
    public Data getDataByUUID(String uuid) throws UUIDNotFound {
        NamedNodeMap attribList;
        Data data = new Data();
        ArrayList<Parameter> paramList;
        ArrayList<AreaInfo> areaList;
        TreeMap<String, String> treeM = new TreeMap<String, String>();

        NodeList nl = this.messagePassing.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element temp = (Element) nl.item(i);
                if (temp.getAttribute("UUID").equals(uuid)) {
                    attribList = temp.getAttributes();
                    for (int a = 0; a < attribList.getLength(); a++) {
                        if (attribList.item(a).getNodeName().equals("UUID")) {
                            data.setId(UUID.fromString(attribList.item(a).getNodeValue()));
                        } else if (attribList.item(a).getNodeName().equals("NAME")) {
                            data.setName(attribList.item(a).getNodeValue());
                        } else {
                            treeM.put(attribList.item(a).getNodeName(), attribList.item(a).getNodeValue());
                        }
                    }
                    data.setTreeM(treeM);
                    Element desc = (Element) temp.getElementsByTagName(XMLCarrierNS + ":description").item(0);
                    data.setDescription(desc.getTextContent());

                    /*
                     * Need to check if we are dealing with an area data section or a parameter
                     * data section. Code might look like non optimized but if we keep the current XML schema,
                     * we have no option.
                     */

                    NodeList isArea = temp.getElementsByTagName(XMLCarrierNS + ":area");

                    if (isArea.getLength() != 0) {

                        /* It means that we are dealing with an area data section
                         */
                        NodeList params = temp.getElementsByTagName(XMLCarrierNS + ":area");
                        areaList = new ArrayList<AreaInfo>();

                        for (int k = 0; k < params.getLength(); k++) {
                            if (params.item(k).getNodeType() == Node.ELEMENT_NODE) {
                                Element area = (Element) params.item(k);
                                AreaInfo areaI = new AreaInfo();
                                TreeMap<String, String> atMap = new TreeMap<String, String>();
                                NamedNodeMap atList = area.getAttributes();
                                for (int atCount = 0; atCount < atList.getLength(); atCount++) {
                                    if (atList.item(atCount).getNodeName().equals("fileGrpID")) {
                                        areaI.setFileGroupID(UUID.fromString(atList.item(atCount).getNodeValue()));
                                    } else if (atList.item(atCount).getNodeName().equals("UUID")) {
                                        areaI.setAreaId(UUID.fromString(atList.item(atCount).getNodeValue()));
                                    } else if (atList.item(atCount).getNodeName().equals("SHAPE")) {
                                        areaI.setShape(atList.item(atCount).getNodeValue());
                                    } else if (atList.item(atCount).getNodeName().equals("BEGIN")) {
                                        areaI.setBegin(atList.item(atCount).getNodeValue());
                                    } else if (atList.item(atCount).getNodeName().equals("COORDS")) {
                                        areaI.setCoords(atList.item(atCount).getNodeValue());
                                    } else if (atList.item(atCount).getNodeName().equals("END")) {
                                        areaI.setEnd(atList.item(atCount).getNodeValue());
                                    } else {
                                        atMap.put(atList.item(atCount).getNodeName(), atList.item(atCount).getNodeValue());
                                    }
                                }
                                areaI.setA(atMap);
                                areaList.add(areaI);

                            }
                            data.setAreas(areaList);
                        }

                    } else {
                        /* Parameter's data section,
                         */
                        paramList = new ArrayList<Parameter>();
                        NodeList params = temp.getElementsByTagName(XMLCarrierNS + ":param");

                        for (int k = 0; k < params.getLength(); k++) {
                            if (params.item(k).getNodeType() == Node.ELEMENT_NODE) {
                                Element parameter = (Element) params.item(k);
                                String name = parameter.getAttribute("NAME");
                                String type = parameter.getAttribute("TYPE");
                                //Element v = (Element) parameter.getElementsByTagName(XMLCarrierNS + ":value").item(0);
                                String value = parameter.getTextContent();
                                paramList.add(new Parameter(type, name, value));
                            }
                        }
                        data.setParams(paramList);
                    }

                }
            }
        }
        if (!(data.getId().toString().equals(uuid))) {
            throw new UUIDNotFound();
        } else {
            return data;
        }
    }

    /**
     * This method returns a list of all Data(s) present in the XML document.
     *
     * @return
     *      List of Data(s).
     */
    public ArrayList<Data> getAllData() {
        ArrayList<Data> list = new ArrayList<Data>();

        NodeList nl = this.messagePassing.getElementsByTagName(XMLCarrierNS + ":Data");
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nl.item(i);
                Data d = new Data();
                d.setId(UUID.fromString(e.getAttribute("UUID")));
                d.setName(e.getAttribute("NAME"));
                NodeList desc = e.getElementsByTagName(XMLCarrierNS + ":description");
                for (int j = 0; j < desc.getLength(); j++) {
                    if (desc.item(j).getNodeType() == Node.ELEMENT_NODE) {
                        Element eDesc = (Element) desc.item(j);
                        d.setDescription(eDesc.getTextContent());
                    }
                }
                NodeList nList = e.getElementsByTagName(XMLCarrierNS + ":param");

                if (nList.getLength() == 0) {
                    /* Area Data section
                     */

                    NodeList nArea = e.getElementsByTagName(XMLCarrierNS + ":area");
                    for (int a = 0; a < nArea.getLength(); a++) {
                        if (nArea.item(a).getNodeType() == Node.ELEMENT_NODE) {
                            Element eArea = (Element) nArea.item(a);
                            AreaInfo ai = new AreaInfo();
                            TreeMap<String, String> atMap = new TreeMap<String, String>();
                            NamedNodeMap atList = eArea.getAttributes();
                            for (int atCount = 0; atCount < atList.getLength(); atCount++) {
                                if (atList.item(atCount).getNodeName().equals("fileGrpID")) {
                                    ai.setFileGroupID(UUID.fromString(atList.item(atCount).getNodeValue()));
                                } else if (atList.item(atCount).getNodeName().equals("UUID")) {
                                    ai.setAreaId(UUID.fromString(atList.item(atCount).getNodeValue()));
                                } else if (atList.item(atCount).getNodeName().equals("SHAPE")) {
                                    ai.setShape(atList.item(atCount).getNodeValue());
                                } else if (atList.item(atCount).getNodeName().equals("BEGIN")) {
                                    ai.setBegin(atList.item(atCount).getNodeValue());
                                } else if (atList.item(atCount).getNodeName().equals("COORDS")) {
                                    ai.setCoords(atList.item(atCount).getNodeValue());
                                } else if (atList.item(atCount).getNodeName().equals("END")) {
                                    ai.setEnd(atList.item(atCount).getNodeValue());
                                } else {
                                    atMap.put(atList.item(atCount).getNodeName(), atList.item(atCount).getNodeValue());
                                }
                            }
                            ai.setA(atMap);
                            d.addAreaInfo(ai);
                        }

                    }
                    list.add(d);
                } else {
                    for (int a = 0; a < nList.getLength(); a++) {
                        if (nList.item(a).getNodeType() == Node.ELEMENT_NODE) {
                            Element parameter = (Element) nList.item(a);
                            String name = parameter.getAttribute("NAME");
                            String type = parameter.getAttribute("TYPE");
                            //Element v = (Element) parameter.getElementsByTagName(XMLCarrierNS + ":value").item(0);
                            String value = parameter.getTextContent();
                            d.addParameter(new Parameter(type, name, value));
                        }

                    }
                    list.add(d);
                }
            }
        }
        return list;
    }

    //TODO documentation for addRawInfo
    public void addComputedInfo(RawInfo ri) {
        Element maintag = this.document.createElement(XMLCarrierNS + ":" + ri.getMainTagname());
        for (String s : ri.attributes.keySet()) {
            maintag.setAttribute(s, String.valueOf(ri.attributes.get(s)));
        }
        this.computedData.appendChild(maintag);

        for (Info i : ri.list) {
            Element e = this.document.createElement(XMLCarrierNS + ":" + i.getTagname());
            for (String name : i.attributes.keySet()) {
                e.setAttribute(name, i.attributes.get(name));
            }
            e.setTextContent(i.getValue());
            maintag.appendChild(e);
        }

    }

    /**
     * This method adds a list of RawInfo the the computed data section.
     *
     * @param list
     */
    public void addComputedInfo(ArrayList<RawInfo> list) {
        for (RawInfo ri : list) {
            this.addComputedInfo(ri);
        }
    }

    /**
     * This method searches for a given name in the ComputedData section and returns all the information contained in it.
     * In the future, it is possible that this method returns more than one RawInfo.
     *
     * @param name Main tag name.
     *
     * @return RawInfo.
     */
    public RawInfo getComputedInfo(String name) {
        RawInfo ri = new RawInfo(name);
        NodeList nl = this.computedData.getElementsByTagName(XMLCarrierNS + ":" + name);
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nl.item(i);
                NamedNodeMap nnmP = e.getAttributes();
                for (int a = 0; a < nnmP.getLength(); a++) {
                    ri.addAttribute(nnmP.item(a).getNodeName(), nnmP.item(a).getNodeValue());
                }
                NodeList infos = e.getChildNodes();
                for (int j = 0; j < infos.getLength(); j++) {
                    if (infos.item(j).getNodeType() == Node.ELEMENT_NODE) {
                        Element info = (Element) infos.item(j);
                        Info inf = new Info(info.getNodeName());
                        NamedNodeMap nnm = info.getAttributes();
                        for (int k = 0; k < nnm.getLength(); k++) {
                            inf.addAttribute(nnm.item(k).getNodeName(), nnm.item(k).getNodeValue());
                        }
                        ri.addInnerTag(inf);
                    }
                }
            }
        }

        return ri;
    }

    /**
     * This method returns a RawInfo given a single attribute and its value.
     * Example of usage : <LightDirections SphereID="id">...this method return the exactly RawInfo given
     * the SphereID tag and its value.
     *
     * @param name Main tag tagname;
     * @param attr Single attribute in the main tag;
     * @param value Single attribute value;
     * @return RawInfo.
     */
    public RawInfo getComputedInfo(String name, String attr, String value) {
        RawInfo ri = new RawInfo(name);
        NodeList nl = this.computedData.getElementsByTagName(XMLCarrierNS + ":" + name);
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nl.item(i);
                if (e.getAttribute(attr).equals(value)) {
                    NamedNodeMap nnmP = e.getAttributes();
                    for (int a = 0; a < nnmP.getLength(); a++) {
                        ri.addAttribute(nnmP.item(a).getNodeName(), nnmP.item(a).getNodeValue());
                    }
                    NodeList infos = e.getChildNodes();
                    for (int j = 0; j < infos.getLength(); j++) {
                        if (infos.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            Element info = (Element) infos.item(j);
                            Info inf = new Info(info.getNodeName());
                            NamedNodeMap nnm = info.getAttributes();
                            for (int k = 0; k < nnm.getLength(); k++) {
                                inf.addAttribute(nnm.item(k).getNodeName(), nnm.item(k).getNodeValue());
                            }
                            ri.addInnerTag(inf);
                        }
                    }
                }
            }
        }

        return ri;
    }

    /**
     * This method returns a list of RawInfos given a tag name.
     *
     * @param name Main tag tagname;
     * @return ArrayList<RawInfo>
     */
    public ArrayList<RawInfo> getComputedInfos(String name) {
        ArrayList<RawInfo> list = new ArrayList<RawInfo>();
        //RawInfo ri = new RawInfo(name);
        NodeList nl = this.computedData.getElementsByTagName(XMLCarrierNS + ":" + name);
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                RawInfo ri = new RawInfo(name);
                Element e = (Element) nl.item(i);
                NamedNodeMap nnmP = e.getAttributes();
                for (int a = 0; a < nnmP.getLength(); a++) {
                    ri.addAttribute(nnmP.item(a).getNodeName(), nnmP.item(a).getNodeValue());
                }
                NodeList infos = e.getChildNodes();
                for (int j = 0; j < infos.getLength(); j++) {
                    if (infos.item(j).getNodeType() == Node.ELEMENT_NODE) {
                        Element info = (Element) infos.item(j);
                        Info inf = new Info(info.getNodeName());
                        NamedNodeMap nnm = info.getAttributes();
                        for (int k = 0; k < nnm.getLength(); k++) {
                            inf.addAttribute(nnm.item(k).getNodeName(), nnm.item(k).getNodeValue());
                        }
                        ri.addInnerTag(inf);
                    }
                }
                list.add(ri);
            }
        }

        return list;
    }

    /**
     * This method receives a new RawInfo and modifies the existing one.
     * @param ri new RawInfo
     */
    public boolean modifyComputedInfo(RawInfo ri) {
        NodeList nl = this.computedData.getElementsByTagName(XMLCarrierNS + ":" + ri.getMainTagname());

        if (nl.getLength() == 0) {
            return false;
        } else {
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i).getNodeType() == Element.ELEMENT_NODE) {

                    Element e = (Element) nl.item(i);
                    this.removeInnerTags(e);
                }
            }

            for (int a = 0; a < nl.getLength(); a++) {
                if (nl.item(a).getNodeType() == Element.ELEMENT_NODE) {
                    Element e2 = (Element) nl.item(a);

                    for (String name : ri.attributes.keySet()) {
                        e2.setAttribute(name, ri.getAttribute(name));
                    }

                    for (Info info : ri.list) {
                        Element el = this.document.createElement(XMLCarrierNS + ":" + info.getTagname());
                        for (String name : info.attributes.keySet()) {
                            el.setAttribute(name, info.attributes.get(name));
                        }
                        el.setTextContent(info.getValue());
                        e2.appendChild(el);
                    }
                }
            }
        }
        return true;
    }

    /**
     * This method receives a new RawInfo and modifies the existing one.
     * @param ri new RawInfo
     */
    public boolean modifyComputedInfo(ArrayList<RawInfo> list) {
        if (list.size() == 0) {
            return false;
        }
        ArrayList<Node> toRemove = new ArrayList<Node>();

        NodeList nl = this.computedData.getElementsByTagName(XMLCarrierNS + ":" + list.get(0).getMainTagname());

        if (nl.getLength() == 0) {
            return false;
        } else {
            for (int i = 0; i < nl.getLength(); i++) {
                toRemove.add(nl.item(i));
                if (nl.item(i).getNodeType() == Element.ELEMENT_NODE) {
                    Element e = (Element) nl.item(i);
                    this.removeInnerTags(e);
                }
            }

            for (Node n : toRemove) {
                n.getParentNode().removeChild(n);
                this.computedData.normalize();
                this.document.normalize();
            }

            for (RawInfo ri : list) {
                this.addComputedInfo(ri);
            }

            return true;
        }
    }

    /**
     * This method eliminates all tags that "belongs" to a specified outer tag.
     * @param e Outer tag (Element).
     */
    private void removeInnerTags(Element e) {
        ArrayList<Node> list = new ArrayList<Node>();

        NodeList inner = e.getChildNodes();
        for (int k = 0; k < inner.getLength(); k++) {
            list.add(inner.item(k));
        }

        for (Node n : list) {
            n.getParentNode().removeChild(n);
            this.computedData.normalize();
            this.document.normalize();

        }

    }

    /**
     * This method returns a list of parameters given a Data ID and the parameter's name.
     *
     * @param uuid
     *      Data's UUID.
     * @param n
     *      Parameter's name.
     * @return
     *      List of parameters.
     *
     * @see Parameter
     */
    private ArrayList<Parameter> getParamsByName(String uuid, String n) {
        ArrayList<Parameter> paramList = new ArrayList<Parameter>();
        NodeList nl = this.messagePassing.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element temp = (Element) nl.item(i);
                if (temp.getAttribute("UUID").equals(uuid)) {
                    NodeList params = temp.getChildNodes();
                    for (int k = 0; k < params.getLength(); k++) {
                        if (params.item(k).getNodeType() == Node.ELEMENT_NODE) {
                            Element parameter = (Element) params.item(k);
                            String name = parameter.getAttribute("NAME");
                            if (name.equals(n)) {
                                String type = parameter.getAttribute("TYPE");
                                //Element v = (Element) parameter.getElementsByTagName(XMLCarrierNS + ":value").item(0);
                                String value = parameter.getTextContent();
                                paramList.add(new Parameter(type, name, value));
                            }
                        }
                    }

                }
            }
        }
        return paramList;
    }

    /**
     * This method returns one Parameter given it's name and a Data UUID.
     *
     * @param uuid
     *      Data's UUID.
     * @param paramName
     *      Parameter's name.
     * @param str
     *      Data's name.
     * @return
     *      Parameter.
     *
     * @see Parameter.
     */
    private Parameter getParamByName(String uuid, String paramName, String str) {
        NodeList nl = this.messagePassing.getChildNodes();
        Parameter p = null;
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element temp = (Element) nl.item(i);
                if ((temp.getAttribute("UUID").equals(uuid))
                        && (temp.getAttribute("NAME").equals(str))) {
                    NodeList params = temp.getChildNodes();
                    for (int k = 0; k < params.getLength(); k++) {
                        if (params.item(k).getNodeType() == Node.ELEMENT_NODE) {
                            Element parameter = (Element) params.item(k);
                            String name = parameter.getAttribute("NAME");
                            if (name.equals(paramName)) {
                                String type = parameter.getAttribute("TYPE");
                                //Element v = (Element) parameter.getElementsByTagName(XMLCarrierNS + ":value").item(0);
                                String value = parameter.getTextContent();
                                p = new Parameter(type, name, value);
                            }
                        }
                    }

                }
            }
        }
        return p;
    }

    /**
     * This method returns the project information(Header section).
     *
     * @return
     *      New HeaderInfo.
     *
     * @see HeaderInfo
     */
    public HeaderInfo getProjectInfo() {
        HeaderInfo h = new HeaderInfo();
        Element e = (Element) this.document.getElementsByTagName(XMLCarrierNS + ":Header").item(0);
        h.setUuid(e.getAttribute("UUID"));
        NodeList nl = e.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element ee = (Element) nl.item(i);
                if (ee.getNodeName().equals(XMLCarrierNS + ":ProjectName")) {
                    h.setProjectName(getCharacterDataFromElement(ee));
                } else if (ee.getNodeName().equals(XMLCarrierNS + ":Description")) {
                    h.setDescription(getCharacterDataFromElement(ee));
                } else if (ee.getNodeName().equals(XMLCarrierNS + ":Author")) {
                    h.setAuthor(getCharacterDataFromElement(ee));
                } else if (ee.getNodeName().equals(XMLCarrierNS + ":CreationDate")) {
                    h.setCreationDate(getCharacterDataFromElement(ee));
                } else if (ee.getNodeName().equals(XMLCarrierNS + ":LastModificationDate")) {
                    h.setLastModDate(ee.getNodeValue());
                } else if (ee.getNodeName().equals(XMLCarrierNS + ":UserInfo")) {
                    h.setUserInfo(ee.getNodeValue());
                } else if (ee.getNodeName().equals(XMLCarrierNS + ":Host")) {
                    h.setHost(ee.getNodeValue());
                } else if (ee.getNodeName().equals(XMLCarrierNS + ":Timestamp")) {
                    h.setTimestamp(ee.getNodeValue());
                }
            }

        }
        Element additionalInfo = (Element) this.header.getElementsByTagName(XMLCarrierNS + ":AdditionalInfo").item(0);
        NodeList info = additionalInfo.getElementsByTagName(XMLCarrierNS + ":Info");
        Element um = (Element) info.item(0);
        NamedNodeMap nUm = um.getAttributes();
        for (int j = 0; j < nUm.getLength(); j++) {
            if (nUm.item(j).getNodeName().equals("OperatingSystem")) {
                h.setOperatingSystem(nUm.item(j).getNodeValue());
            } else if (nUm.item(j).getNodeName().equals("MacAddress")) {
                h.setMacAddress(nUm.item(j).getNodeValue());
            } else if (nUm.item(j).getNodeName().equals("Processor")) {
                h.setProcessorInfo(nUm.item(j).getNodeValue());
            } else if (nUm.item(j).getNodeName().equals("MemoryAvailable")) {
                h.setMemoryAvailable(nUm.item(j).getNodeValue());
            }
        }

        NodeList dois = additionalInfo.getElementsByTagName(XMLCarrierNS + ":Info2");
        for (int l = 0; l < dois.getLength(); l++) {
            if (dois.item(l).getNodeType() == Node.ELEMENT_NODE) {
                Element ab = (Element) dois.item(l);
                h.addParameter(ab.getAttribute("NAME"), ab.getAttribute("VALUE"));
            }
        }

        return h;
    }

    public void registEvent(Event e) {
        Element ev = this.document.createElement(XMLCarrierNS + ":event");
        ev.setAttribute("LEVEL", e.getLevel());
        ev.setAttribute("USER", e.getUser());
        ev.setAttribute("COMPID", e.getComponentID().toString());
        ev.setAttribute("DATEF", e.getDateFinished());
        ev.setAttribute("DATES", e.getDateStarted());
        ev.setTextContent(e.getText());
        this.logger.appendChild(ev);
    }

    /**
     * This method returns a textual representation of a data using the Gregorian Calendar.
     *
     * @return
     *      String representing a date (YEAR/MONTH/DAY HOUR:MINUTE:SECOND).
     */
    public String generateDate() {
        StringBuilder date = new StringBuilder();
        Calendar calendar = new GregorianCalendar();
        Date d = new Date();
        calendar.setTime(d);
        date.append(calendar.get(Calendar.YEAR));
        date.append("/");
        date.append(calendar.get(Calendar.MONTH));
        date.append("/");
        date.append(calendar.get(Calendar.DAY_OF_MONTH));

        date.append(" ");
        date.append(calendar.get(Calendar.HOUR_OF_DAY));
        date.append(":");
        date.append(calendar.get(Calendar.MINUTE));
        date.append(":");
        date.append(calendar.get(Calendar.SECOND));

        return date.toString();
    }

    /**
     * This method returns the user name.
     *
     * @return
     *      User name.
     */
    public String generateUserInfo() {
        String UI = "";
        UI = System.getProperty("user.name");
        return UI;
    }

    /**
     * This method returns the host name.
     *
     * @return
     *      Host name.
     */
    public String generateHost() {
        String host = "";
        try {
            InetAddress a = InetAddress.getLocalHost();
            host = a.getHostName();
        } catch (Exception e) {
            host = "Cannot retrieve information";
        }
        return host;
    }

    /**
     * This method returns the name/architecture of the operating system.
     *
     * @return
     *      Name and architecture of the operating sytem.
     */
    public String generateOSVersion() {
        StringBuilder os = new StringBuilder();
        os.append(System.getProperty("os.name"));
        os.append(" ");
        os.append(System.getProperty("os.arch"));
        return os.toString();
    }

    /**
     * This method returns the number of processors.
     *
     * @return
     *      Number of processors available.
     */
    public String generateProcessorInfo() {
        int n = Runtime.getRuntime().availableProcessors();
        String proc = String.valueOf(n);
        return proc;
    }

    /**
     * This method returns the total memory in the Java Virtual Machine.
     *
     * @return
     */
    public String generateMemInfo() {
        // TODO Auto-generated method stub
        long tm = Runtime.getRuntime().totalMemory();
        String mem = String.valueOf(tm);
        return mem;
    }

    /**
     * This method adds the content of the Header section. If already exists an Header, it's content will be replaced by the
     * new one.
     *
     * @param a
     *      HeaderInfo to be added.
     * @see HeaderInfo.
     */
    public void setHeaderInfo(HeaderInfo a) {
        NodeList nl = this.header.getChildNodes();

        if (nl.getLength() == 0) {

            this.writeHeader(a);
        } else {
            this.removeHeader();
            this.writeHeader(a);
        }

    }

    public String getXmlPath() {
        return xmlPath;
    }

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    /*
     * Auxiliary Methods
     */
    private static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "?";
    }
}
