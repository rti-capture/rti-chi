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
package RTIbuilder.GUI;

import DataCache.DataCache;
import Exceptions.ArgumentException;
import Exceptions.ModuleException;
import Exceptions.XMLcarrierException;
import ModuleInterfaces.PluginMetaInfo;
import ModuleInterfaces.RTIBuilderInterface;
import XMLcarrier.Exceptions.XMLNotAvailable;
import XMLcarrier.Exceptions.XSDCantValidadeXML;
import java.beans.PropertyChangeListener;
import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import RTIbuilder.*;
import XMLcarrier.XMLHandler;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.UUID;
import java.awt.CardLayout;
import java.awt.Image;
import java.awt.Toolkit;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.Task.BlockingScope;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import java.util.Map;
import javax.swing.JOptionPane;
import RTIbuilder.helpers.ImageContainer;
import RTIbuilder.pluginhelpers.Pipeline;
import RTIbuilder.pluginhelpers.Plugin;

/**
 * The application's main frame.
 */
public class LPtrackerView extends FrameView implements RTIBuilderInterface {

    /** Points to the current selected image folder*/
    File current_selected_folder;
    /** Holds current Image Files in Current Folder. This is a set of ImageContainer maped by their unique identifier.
    @see lptracker.helpers.ImageContainer*/
    TreeMap<UUID, ImageContainer> original_images;
    /** User choice: true=load folder ; false=load lp file. */
    boolean user_open_choice;
    /** Image preview thumbnails*/
    Map<UUID, BufferedImage> thumbnails;
    /** Data Cache, meant to replace original_images and thumbnails both **/
    DataCache cache;
    /** Project name */
    String project_name = "";
    /** The current project image extension.*/
    String images_extension = "";
    //Plugin variables
    /**Loaded plugins*/
    ArrayList<Plugin> plugins;
    /**The selected plugins to execution, in a specific order*/
    Pipeline pipeline_in_use;
    /**List of diferent pipelines for execution*/
    ArrayList<Pipeline> pipelines;
    //XML variables
    /**The XML carrier that contains all the information relative to the project.<br>
     * This carrier is used to store this information and comunicate with other services<p>
    @see XMLHandler
     */
    XMLHandler carrier = null;
    /** The path to the XML carrier*/
    StringBuffer xmlPath = null;
    /**LPtracker module id*/
    public static final String XML_VERSION_TAG = "RTIbuilder V1.1";
    /**Time stamp*/
    int time_stamp = 0;

    /**
     * This method is an ugly hack to work around a technical issue.
     * 
     * If someone with a Netbeans that doesn't break this window's UI when the graphical editor is used to modify this class file would fix this through the graphical UI editor, it would be nice and make this ugly hack unnecessary.
     * 
     * No, I had no other practical and safe way around it.
     * 
     * Basically, this dumps the menu container and reinitializes it with the same menu options, plus an Open... option for opening the project files. Because the graphical UI couldn't be edited on my end for who knows WHAT reason without breaking things, this workaround was written... and I don't like it any more than you should.
     */
    private void fixMenuBarBecauseCannotFixItThroughUIEditor(InitialMenu initMenu)
    {
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem openMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(RTIbuilder.RTIbuilderApp.class).getContext().getResourceMap(LPtrackerView.class);
        org.jdesktop.application.ResourceMap resourceMap2 = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(InitialMenu.class);

        /*
        jButtonOpenXMLProject.setText(resourceMap.getString("jButtonOpenXMLProject.text")); // NOI18N
        jButtonOpenXMLProject.setName("jButtonOpenXMLProject"); // NOI18N
        jButtonOpenXMLProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenXMLProjectActionPerformed(evt);
            }
        });*/


        menuBar.removeAll();

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(RTIbuilder.RTIbuilderApp.class).getContext().getActionMap(LPtrackerView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        

        openMenuItem.setText("Open Project");
        openMenuItem.setName("openMenuItem");
        //openMenuItem.setAction()

        javax.swing.AbstractAction openMenuAction = new OpenActionImpl(initMenu);
        openMenuItem.setAction(openMenuAction);

        fileMenu.add(openMenuItem);
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);
    }


    public LPtrackerView(SingleFrameApplication app) {
        super(app);

        initComponents();




        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);


        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            @Override
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });


        // start initial panel
        pipelineMenu = new PipelineMenu(this);
        mainPanel.add(pipelineMenu, PipelineMenu.PANEL_NAME);
        initialMenu = new InitialMenu(this);
        mainPanel.add(initialMenu, InitialMenu.PANEL_NAME);

        //initialMenu.performOpenXMLbuttonclick();
        // High priority to get rid of this if able to! Makes UI changes that are HIDDEN from the graphical UI editor, and that is always bad.
        // Unfortunately due to technical reasons (check comments), it was the only practical and safe way of doing things that had to be done.
        fixMenuBarBecauseCannotFixItThroughUIEditor(initialMenu);

        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, InitialMenu.PANEL_NAME);


    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = RTIbuilderApp.getApplication().getMainFrame();
            aboutBox = new LPtrackerAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        RTIbuilderApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        PluginPanel = new javax.swing.JPanel();
        PluginButtonPanel = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        Next = new javax.swing.JButton();
        Back = new javax.swing.JButton();
        PluginGUI = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new java.awt.CardLayout());

        PluginPanel.setName("PluginPanel"); // NOI18N

        PluginButtonPanel.setMinimumSize(new java.awt.Dimension(644, 42));
        PluginButtonPanel.setName("PluginButtonPanel"); // NOI18N
        PluginButtonPanel.setPreferredSize(new java.awt.Dimension(744, 42));

        jSeparator1.setName("jSeparator1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(RTIbuilder.RTIbuilderApp.class).getContext().getResourceMap(LPtrackerView.class);
        Next.setText(resourceMap.getString("Next.text")); // NOI18N
        Next.setName("Next"); // NOI18N
        Next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NextActionPerformed(evt);
            }
        });
        Next.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                NextPropertyChange(evt);
            }
        });

        Back.setText(resourceMap.getString("Back.text")); // NOI18N
        Back.setName("Back"); // NOI18N
        Back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BackActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PluginButtonPanelLayout = new javax.swing.GroupLayout(PluginButtonPanel);
        PluginButtonPanel.setLayout(PluginButtonPanelLayout);
        PluginButtonPanelLayout.setHorizontalGroup(
            PluginButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PluginButtonPanelLayout.createSequentialGroup()
                .addContainerGap(928, Short.MAX_VALUE)
                .addComponent(Back, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Next, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
            .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 1117, Short.MAX_VALUE)
        );
        PluginButtonPanelLayout.setVerticalGroup(
            PluginButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PluginButtonPanelLayout.createSequentialGroup()
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PluginButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Next, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Back, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        PluginGUI.setMinimumSize(new java.awt.Dimension(600, 400));
        PluginGUI.setName("PluginGUI"); // NOI18N
        PluginGUI.setPreferredSize(new java.awt.Dimension(600, 400));
        PluginGUI.setLayout(new java.awt.CardLayout());

        javax.swing.GroupLayout PluginPanelLayout = new javax.swing.GroupLayout(PluginPanel);
        PluginPanel.setLayout(PluginPanelLayout);
        PluginPanelLayout.setHorizontalGroup(
            PluginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PluginGUI, javax.swing.GroupLayout.DEFAULT_SIZE, 1117, Short.MAX_VALUE)
            .addComponent(PluginButtonPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1117, Short.MAX_VALUE)
        );
        PluginPanelLayout.setVerticalGroup(
            PluginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PluginPanelLayout.createSequentialGroup()
                .addComponent(PluginGUI, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE)
                .addComponent(PluginButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        mainPanel.add(PluginPanel, "PluginPanel");

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(RTIbuilder.RTIbuilderApp.class).getContext().getActionMap(LPtrackerView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 1117, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 947, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

	private void BackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BackActionPerformed
            back();
            //System.out.println("BACK");
	}//GEN-LAST:event_BackActionPerformed

	private void NextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NextActionPerformed
            next();
            //System.out.println("NEXT");
	}//GEN-LAST:event_NextActionPerformed

	private void NextPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_NextPropertyChange
            JButton button = (JButton) evt.getSource();
            if (button.isEnabled() && !project_name.equals("")) {
                if (pipeline_in_use.hasNextStage()) {
                    pipeline_in_use.addStage(1);
                    if (!pipeline_in_use.getStagePlugin().isExecuted()) {
                        pipeline_in_use.getStagePlugin().setAcessible(true);
                    }
                    pipeline_in_use.addStage(-1);
                }
            }
	}//GEN-LAST:event_NextPropertyChange

    public void next() {
        if (pipeline_in_use.hasNextStage()) {
            pipeline_in_use.addStage(1);
            Plugin p = pipeline_in_use.getStagePlugin();
            //load to get the actual time stamp
            try {
                carrier.loadXML();
            } catch (XMLNotAvailable ex) {
                Logger.getLogger(LPtrackerView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XSDCantValidadeXML ex) {
                Logger.getLogger(LPtrackerView.class.getName()).log(Level.SEVERE, null, ex);
            }
            int current_time_stamp = carrier.getTimestamp();

            System.out.println("TimeStamp on next: " + current_time_stamp + "RTIBuilder: " + time_stamp);

            if (current_time_stamp > time_stamp || !p.isExecuted()) {

                //	System.out.println("New start on "+p.getPluginName());

                p.getUser_interface().displayPluginInterface();
                try {
                    p.getUser_interface().replaceDataCache(cache);
                    p.setMetaInfo(new PluginMetaInfo(p.getUuid(), this.pipeline_in_use.getStage() ));
                    p.getUser_interface().start(xmlPath, false);

                } catch (ArgumentException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error in the plug-in inputs", JOptionPane.ERROR_MESSAGE);
                } catch (XMLcarrierException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error on XMLcarrier", JOptionPane.ERROR_MESSAGE);
                } catch (ModuleException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error in the plug-in", JOptionPane.ERROR_MESSAGE);
                }
                p.getUser_interface().setParentApplication(this);
                p.getUser_interface().setImagesThumbnails(thumbnails);
                p.getUser_interface().setAdvanceComponet(Next);
                Next.setEnabled(false);
                time_stamp = current_time_stamp;
                //Make all the plugins in front as not executed.
                int stage = pipeline_in_use.getStage();
                while (pipeline_in_use.hasNextStage()) {
                    pipeline_in_use.addStage(1);
                    pipeline_in_use.getStagePlugin().setExecuted(false);
                    pipeline_in_use.getStagePlugin().setAcessible(false);
                }
                pipeline_in_use.setStage(stage);
            }
            //Disable next if needed
            if (pipeline_in_use.hasNextStage()) {
                //Go to the next plugin and see if it was executed or is acessible.
                pipeline_in_use.addStage(1);
                if (!pipeline_in_use.getStagePlugin().isExecuted() && !pipeline_in_use.getStagePlugin().isAcessible()) {
                    Next.setEnabled(false);
                }
                //go back to the previous plugin
                pipeline_in_use.addStage(-1);
            }

            //If the plugin is the last, hide the next button
            if (pipeline_in_use.isLast()) {
                Next.setVisible(false);
            }
            //In the end set this plugin as executed
            if (!p.isExecuted()) {
                p.setExecuted(true);
            }
            CardLayout cl = (CardLayout) PluginGUI.getLayout();
            cl.show(PluginGUI, p.getPluginName());
        }
    }

    public void back() {

        //load to get the actual time stamp
        try {
            carrier.loadXML();
        } catch (XMLNotAvailable ex) {
            Logger.getLogger(LPtrackerView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XSDCantValidadeXML ex) {
            Logger.getLogger(LPtrackerView.class.getName()).log(Level.SEVERE, null, ex);
        }


        if (pipeline_in_use.hasNextStage()) {
            pipeline_in_use.addStage(1);
            if (carrier.getTimestamp() > time_stamp) {
                pipeline_in_use.getStagePlugin().setExecuted(false);
                //System.out.println("Plugin "+pipeline_in_use.getStagePlugin().getPluginName()+" chaged to not executed");
            }
            pipeline_in_use.addStage(-1);
        }
        time_stamp = carrier.getTimestamp();
        Next.setVisible(true);
        if (pipeline_in_use.getStage() > 0) {
            pipeline_in_use.addStage(-1);

            Plugin p = pipeline_in_use.getStagePlugin();

            // Make sure the plugin HAS executed at some point. If it has not,
            // then launch it.
            if (pipeline_in_use.getStage() > 0 && !p.isExecuted())
            {
                p.getUser_interface().setParentApplication(this);
                p.getUser_interface().setImagesThumbnails(thumbnails);
                p.getUser_interface().setAdvanceComponet(Next);
                try {
                    p.getUser_interface().replaceDataCache(cache);
                    p.setMetaInfo(new PluginMetaInfo(p.getUuid(), this.pipeline_in_use.getStage() ));
                    p.getUser_interface().start(xmlPath, false);

                } catch (ArgumentException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error in the plug-in inputs", JOptionPane.ERROR_MESSAGE);
                } catch (XMLcarrierException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error on XMLcarrier", JOptionPane.ERROR_MESSAGE);
                } catch (ModuleException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error in the plug-in", JOptionPane.ERROR_MESSAGE);
                }
                p.getUser_interface().displayPluginInterface();

            }

            if (pipeline_in_use.getStage() == 0) {
                CardLayout cl = (CardLayout) mainPanel.getLayout();
                cl.previous(mainPanel);
            } else {
                CardLayout cl = (CardLayout) PluginGUI.getLayout();
                cl.show(PluginGUI, p.getPluginName());
                Next.setEnabled(true);
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Back;
    private javax.swing.JButton Next;
    private javax.swing.JPanel PluginButtonPanel;
    private javax.swing.JPanel PluginGUI;
    private javax.swing.JPanel PluginPanel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
    InitialMenu initialMenu;
    PipelineMenu pipelineMenu;

    public TreeMap<UUID, ImageContainer> getORIGINAL_IMAGES_IN_FOLDER() {
        return original_images;
    }

    public void setORIGINAL_IMAGES_IN_FOLDER(TreeMap<UUID, ImageContainer> ORIGINAL_IMAGES_IN_FOLDER) {
        this.original_images = ORIGINAL_IMAGES_IN_FOLDER;
    }

    public PipelineMenu getPipelineMenu() {
        return pipelineMenu;
    }

    public void setPipelineMenu(PipelineMenu pipelineMenu) {
        this.pipelineMenu = pipelineMenu;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setMainPanel(JPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    public JPanel getPluginGUI() {
        return PluginGUI;
    }

    public void setPluginGUI(JPanel PluginGUI) {
        this.PluginGUI = PluginGUI;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public JLabel getStatusMessageLabel() {
        return statusMessageLabel;
    }

    public void setStatusMessageLabel(JLabel statusMessageLabel) {
        this.statusMessageLabel = statusMessageLabel;
    }

    public JButton getNext() {
        return Next;
    }

    public void setNext(JButton Next) {
        this.Next = Next;
    }

    public Map<UUID, BufferedImage> getThumbnails() {
        return thumbnails;
    }

    @Override
    public void setThumbnails(Map<UUID, BufferedImage> thumbnails) {
        this.thumbnails = thumbnails;
    }

    public void setXMLcarrier(StringBuffer XMLcarrierPath) {
        String old_path = carrier.getXmlPath();
        this.xmlPath = XMLcarrierPath;
        File f = new File(old_path);
        f.delete();
        carrier.setXmlPath(xmlPath.toString());
        try {
            carrier.writeXML();
        } catch (Exception ex) {
            Logger.getLogger(LPtrackerView.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public XMLHandler getXMLcarrier()
    {
        return this.carrier;
    }

    public void setTimestamp(int time) {
        time_stamp = time;
    }

    private static class OpenActionImpl extends javax.swing.AbstractAction {

        private InitialMenu im;

        public OpenActionImpl(InitialMenu initMenu) {
            super("Open Project");
            im = initMenu;
            this.putValue(SHORT_DESCRIPTION, "Open an existing Project");
            
        }

        public void actionPerformed(ActionEvent e) {
            //throw new UnsupportedOperationException("Not supported yet.");
            im.performOpenXMLbuttonclick();
        }




    }
}
