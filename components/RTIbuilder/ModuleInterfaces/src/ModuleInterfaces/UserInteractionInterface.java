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

/*
 *  All puglin interface panels for the LPtracker V2 must implement this interface.
 *  It will allow the LPtracker user to interact with the component.
 */
package ModuleInterfaces;

import DataCache.DataCache;
import DataCache.iDataCache;
import Exceptions.ArgumentException;
import Exceptions.ModuleException;
import Exceptions.XMLcarrierException;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.UUID;

public abstract class UserInteractionInterface extends javax.swing.JPanel {

    private Component UIpanel;
    private Component AdvanceComponent;
    protected iDataCache cache;
    protected PluginMetaInfo plugInfo;

    /** Creates new form GUIInterface */
    public UserInteractionInterface() {
        initComponents();

    }

    /**Defines the plug-in interface panel designed by the developer.
     *@param comp the user interaction interface to be used.
     */
    public void defineUserInterface(java.awt.Component comp) {
        UIpanel = comp;
        this.validate();
    }

    ;

    /**Displays the plug-in user interface*/
    public void displayPluginInterface() {
        this.add(UIpanel);
        UIpanel.setVisible(true);
        this.validate();
    }

    /**Starts the plug-in defined by user process.<br>
     *In this method, the plug-in usually uses the XMLcarrier for input operations,
     *preparing the interface to the user before showing it.
     *<br>When the flag loadStage is set to true, then the plug-in should be able to load information from previous executions.
     *@param loadStage (TODO) This flag indicates if the plug-in should run from scratch if false, otherwise it should load a previous execution.
     *@param XMLurl  An absolute URL giving the base location of the project xml.
     *@throws ArgumentException
     *@throws ModuleException
     *@throws XMLcarrierException
     */
    public abstract void start(StringBuffer XMLurl, boolean loadStage) throws ArgumentException, ModuleException, XMLcarrierException;

    /**Indicates that the process is terminated and that the user can advance.*/
    public void done() {
        AdvanceComponent.setEnabled(true);
    }

    /**Alows the image preview buttons usage in the plug-in interface. This buttons originate from the LPtracker main interface.*/
    public abstract void setImagesThumbnails(Map<UUID, BufferedImage> thumbnails);

    /**
     * Sets a new DataCache instance on the plugin
     * 
     * @param newCache
     */
    public void setDataCache(iDataCache newCache)
    {
        if (newCache!=null)
            this.cache = newCache;
    }

    /**
     * Sets a new DataCache instance on the plugin and returns the old one. If
     * a null pointer is passed as the new cache, the cache is not replaced.
     *
     * @param newCache
     * @return
     */
    public iDataCache replaceDataCache(iDataCache newCache)
    {
        iDataCache tmp = this.cache;
        if (newCache!=null)
            this.cache = newCache;
        return tmp;
    }

    public iDataCache getDataCache()
    {
        return this.cache;
    }

    public void setMetaInfo(PluginMetaInfo pI)
    {
        this.plugInfo = pI;
        System.out.println("Outputting PluginMetaInfo just set:");
        System.out.println(pI.toString());
        System.out.println("");
    }

    public PluginMetaInfo getMetaInfo()
    {
        return this.plugInfo;
    }

    /**Returns the user defined interface*/
    public Component getUIpanel() {
        return UIpanel;
    }

    /**Gives the plug-in a restrict interface, so he can interact with main application.
     *@param parentApplication the parent application interface.
     */
    public abstract void setParentApplication(RTIBuilderInterface parentApplication);

    /**Sets the component to be activated in the plug-in termination.
     *@param advanceComponent the component to be enabled when the process is terminated.
     */
    public void setAdvanceComponet(java.awt.Component advanceComponent) {
        this.AdvanceComponent = advanceComponent;
        this.AdvanceComponent.setEnabled(false);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
      // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
      private void initComponents() {

            setBorder(javax.swing.BorderFactory.createEtchedBorder());
            setName("PluginGUI"); // NOI18N
            setLayout(new java.awt.BorderLayout());
      }// </editor-fold>//GEN-END:initComponents


      // Variables declaration - do not modify//GEN-BEGIN:variables
      // End of variables declaration//GEN-END:variables

}
