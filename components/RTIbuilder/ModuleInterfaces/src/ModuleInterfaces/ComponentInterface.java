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
import java.net.URI; 
import java.util.UUID; 

/**
 *  All puglin components for the LPtracker V2 must implement this interface. 
 *      It will allow the LPtracker UI application to communicate and interact 
 *      with the component.
 *  <br>
 *  <br>
 *  <br>
 *  <br>@author jbarbosa
 */
// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.2629C83D-ADA9-46DF-4D16-9999B59EB6E2]
// </editor-fold> 
public interface ComponentInterface {

    /**
     *  <p style="margin-top: 0">
     *        Contains the name of the module. The name must describe the 
     *        functionality.
     *  <br>(e.g. Sphere Detection, Highlight Detection)
     *      </p>
     */
    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.61CD4BCB-7699-BDE9-470C-0F0F82C49A00]
    // </editor-fold> 
    public static final String name = "";

    /**
     *  <p style="margin-top: 0">
     *        Component version, see LPtracker Specification and Architecture for 
     *        details.
     *      </p>
     */
    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.7351D5FC-427B-F794-8753-818F2689CD9A]
    // </editor-fold> 
    public static final int version = 1;

    /**
     *  <p style="margin-top: 0">
     *        Component realease number, see LPtracker V2 for details
     *      </p>
     */
    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.D14F60B2-1EFA-7E75-86E6-708F52F5BA10]
    // </editor-fold> 
    public static final int release = 0;

    /**
     *  <p style="margin-top: 0">
     *        Revision number, see LPtracker V2 Specification and Architeture for 
     *        details.
     *      </p>
     */
    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.C1D5ADC3-8276-922B-A3A0-B9316DA780BA]
    // </editor-fold> 
    public static final int revision = 0;

    /**
     *  <p style="margin-top: 0">
     *        Component Unique Universal Identifier. Indentifies a specific version 
     *        and realease. See LPtracker V2 Specification and Architecture for 
     *        details.
     *      </p>
     */
    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.0ABE9EC4-5B41-2543-B6B5-1BB9EBA5955E]
    // </editor-fold> 
    public static final java.util.UUID UUID = null;

    /**
     *  <p style="margin-top: 0">
     *        Starts the execution of the operations specified in the XMLcarrier 
     *        description for the particular module.
     *  <br>Recives the XML descriptions as a StringBuffer and returns the same XML 
     *        description with the module final result and log operations.
     *  <br>
     *  <br>If an error occurs, throws an Exception depending on the type of error. 
     *        (See Exceptions for details)
     *      </p>
     */
    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.C31BA3FC-CFD6-66E0-A5C8-19C9C6B83B17]
    // </editor-fold> 
    public StringBuffer executeModule (StringBuffer XMLcarrier) throws Exceptions.ModuleException;

    /**
     *  <p style="margin-top: 0">
     *        Starts the execution of the operations specified in the XMLcarrier 
     *        description for the particular module.<br>Receives the XML file path to 
     *        be used as a string.<br><br>If an error occurs, throws an Exception 
     *        depending on the type of error. (See Exceptions for details)
     *      </p>
     */
    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.BE0866A1-2E7B-C1AA-813C-D9146DF2EADD]
    // </editor-fold> 
    public void executeModule (String XMLcarrierPath) throws Exceptions.ModuleException;

    /**
     *  <p style="margin-top: 0">
     *        Starts the execution of the operations specified in the XMLcarrier 
     *        description for the particular module.<br>
     *      </p>
     *      <p style="margin-top: 0">
     *        <br>
     *        Receives the XML to be used as java.io.File.<br><br>If an error occurs, 
     *        throws an Exception depending on the type of error. (See Exceptions for 
     *        details)
     *      </p>
     */
    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.0866355D-A9A7-B38D-7898-D5B9690EE2E7]
    // </editor-fold> 
    public void executeModule (File XMLcarrierFile) throws Exceptions.ModuleException;

    /**
     *  <p style="margin-top: 0">
     *        Starts the execution of the operations specified in the XMLcarrier 
     *        description for the particular module.<br>Receives the XML file location 
     *        as URI descriptor.<br><br>If an error occurs, throws an Exception 
     *        depending on the type of error. (See Exceptions for details)
     *      </p>
     */
    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.417B4679-20A5-887E-DB30-3252CD1F4A72]
    // </editor-fold> 
    public void executeModule (URI XMLcarrierURI) throws Exceptions.ModuleException;

    /**
     *  <p style="margin-top: 0">
     *        Returns the component UUID
     *      </p>
     */
    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.A15AD2C9-D35E-5745-A99F-524087356086]
    // </editor-fold> 
    public UUID getModuleUUID ();

    /**
     *  <p style="margin-top: 0">
     *        Returns the component name.
     *      </p>
     */
    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.0D69FDEF-E57B-CFCD-BD2F-9F7EE91B440E]
    // </editor-fold> 
    public String getModuleNAME ();

    /**
     *  <p style="margin-top: 0">
     *        Returns the component version as a String:
     *  <br>
     *  <br>(version+&quot;.&quot;+release+&quot;.&quot;+revision)
     *      </p>
     */
    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.7F1B5EF1-2631-F6F7-D33A-6BDEBDFF5D01]
    // </editor-fold> 
    public String getModuleVersion ();

}

