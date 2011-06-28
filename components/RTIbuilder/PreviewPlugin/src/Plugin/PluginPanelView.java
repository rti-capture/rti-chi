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
package Plugin;

import DataCache.iDataCache;
import Plugin.helpers.ImageContainer;
import Exceptions.ArgumentException;
import Exceptions.ModuleException;
import Exceptions.XMLcarrierException;
import ModuleInterfaces.UserInteractionInterface;
import Plugin.helpers.FileManipulation;
import Plugin.helpers.InputReader;
import Plugin.helpers.Pair;
import Plugin.helpers.RowEditableTableModel;
import XMLcarrier.Event;
import XMLcarrier.Exceptions.DuplicateUUID;
import XMLcarrier.Exceptions.UUIDNotFound;
import XMLcarrier.Exceptions.XMLNotAvailable;
import XMLcarrier.Exceptions.XSDCantValidadeXML;
import XMLcarrier.FileGroup;
import XMLcarrier.HeaderInfo;
import XMLcarrier.ImageFile;
import XMLcarrier.Info;
import XMLcarrier.RawInfo;
import XMLcarrier.XMLHandler;
import guicomponents.ImageThumbnailButton;
import guicomponents.PreviewPanel;
import guicomponents.ProgressBarPopup;
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class PluginPanelView extends javax.swing.JPanel implements IPluginPanelView {

    /**
     * Object containing this panel's associated Model and Controller as per the
     * MVC pattern
     */
    PluginPanelModelController model;

    iDataCache cache;

    /** Task thar loads image files (when we open image folder) */
    OpenImageFilesTask openImageFilesTask = null;
    /** Task thar loads image files (when we open lp file) */
    OpenLPFileTask openLPFileTask = null;

    /**Preview panel to display the project images*/
    PreviewPanel preview_panel;

    /** Image preview thumbnails*/
    Map<UUID, BufferedImage> thumbnails;

    /** Special marker to determine whether thumbnails were init'ed early */
    boolean prematureThumbnailInit = false;

    /** Creates new form PluginPanelView */
    public PluginPanelView() {
        initComponents();
    }

    /**Initializes the preview panel changing labels and visible components according to the user permission.**/
    public void start(StringBuffer XMLurl, int user_option) throws ArgumentException, ModuleException, XMLcarrierException {

        model = new PluginPanelModelController(this, XMLurl, user_option);

        // Reset selected image
        if (preview_panel != null && preview_panel.getImage() != null)
        {
            //preview_panel.getParent().setVisible(false);
            preview_panel.setImage(null);
            preview_panel.repaint();
        }

        //Reset in case of re-execution
        //remove all possivel thumbnails
        jpDSPPreviewThumbs.removeAll();

        //Reset remove reasons
        jtRemoveReason.setText("");

        //Set the open button label
        String label = (model.user_open_choice == OPEN_FOLDER) ? "Open Folder" : "Open LP file";
        jbOpenFolder_File.setText(label);

        //Set the remove image panel visable only if we are opening a folder
        jpRemovePicturePanel.setVisible(model.user_open_choice == OPEN_FOLDER);
        jbRemovePicture.setEnabled(false);
        jtRemoveReason.setEnabled(false);

        //System.out.println(preview_panel.toString());

        //Preview_Panel_jp.add(preview_panel);

        //Initialization of properties table
        jtPropertiesTable.setModel(model.getTableModel());
        jtPropertiesTable.getColumnModel().getColumn(0).setHeaderValue("Field");
        jtPropertiesTable.getColumnModel().getColumn(1).setHeaderValue("Value");
        jtPropertiesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jtPropertiesTable.getColumnModel().getColumn(0).setResizable(true);
        jtPropertiesTable.getColumnModel().getColumn(1).setResizable(true);

        if (model.getProjectImages()!=null && model.getProjectImages().size()>0)
        {
            this.initPreviewMenu();
            this.prematureThumbnailInit=true;
        }

        this.validate();
        save_table_properties_button.setVisible(false);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane5 = new javax.swing.JSplitPane();
        Preview_Panel_jp = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jpDSPPreviewThumbs = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jbAdd_propertie = new javax.swing.JButton();
        jScrollPane7 = new javax.swing.JScrollPane();
        jtPropertiesTable = new javax.swing.JTable();
        jbRemove_propertie = new javax.swing.JButton();
        save_table_properties_button = new javax.swing.JButton();
        jpRemovePicturePanel = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        jtRemoveReason = new javax.swing.JTextArea();
        jbRemovePicture = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jbOpenFolder_File = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jSplitPane5.setDividerLocation(280);
        jSplitPane5.setPreferredSize(new java.awt.Dimension(9, 4));

        Preview_Panel_jp.setMinimumSize(new java.awt.Dimension(300, 300));
        Preview_Panel_jp.setPreferredSize(new java.awt.Dimension(300, 300));
        Preview_Panel_jp.setLayout(new java.awt.BorderLayout());
        jSplitPane5.setLeftComponent(Preview_Panel_jp);

        jpDSPPreviewThumbs.setLayout(new java.awt.GridLayout(0, 7));
        jScrollPane6.setViewportView(jpDSPPreviewThumbs);

        jSplitPane5.setRightComponent(jScrollPane6);

        add(jSplitPane5, java.awt.BorderLayout.CENTER);

        jPanel13.setMinimumSize(new java.awt.Dimension(55, 23));

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Project Properties"));

        jbAdd_propertie.setText("Add");
        jbAdd_propertie.setToolTipText("Add new field to project properties");
        jbAdd_propertie.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAdd_propertieActionPerformed(evt);
            }
        });

        jScrollPane7.setAutoscrolls(true);

        jtPropertiesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Field", "Value"
            }
        ));
        jtPropertiesTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        jtPropertiesTable.setColumnSelectionAllowed(true);
        jtPropertiesTable.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jtPropertiesTablePropertyChange(evt);
            }
        });
        jScrollPane7.setViewportView(jtPropertiesTable);
        jtPropertiesTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        jbRemove_propertie.setText("Remove");
        jbRemove_propertie.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbRemove_propertieActionPerformed(evt);
            }
        });

        save_table_properties_button.setText("Save");
        save_table_properties_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                save_table_properties_buttonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel10Layout = new org.jdesktop.layout.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel10Layout.createSequentialGroup()
                .add(jScrollPane7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jbAdd_propertie, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jbRemove_propertie, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                    .add(save_table_properties_button, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel10Layout.createSequentialGroup()
                .add(jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel10Layout.createSequentialGroup()
                        .add(jbAdd_propertie)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jbRemove_propertie)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 119, Short.MAX_VALUE)
                        .add(save_table_properties_button))
                    .add(jScrollPane7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE))
                .addContainerGap())
        );

        jpRemovePicturePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Removal Reason"));

        jtRemoveReason.setColumns(20);
        jtRemoveReason.setRows(5);
        jScrollPane8.setViewportView(jtRemoveReason);

        jbRemovePicture.setText("Remove Picture");
        jbRemovePicture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbRemovePictureActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jpRemovePicturePanelLayout = new org.jdesktop.layout.GroupLayout(jpRemovePicturePanel);
        jpRemovePicturePanel.setLayout(jpRemovePicturePanelLayout);
        jpRemovePicturePanelLayout.setHorizontalGroup(
            jpRemovePicturePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jpRemovePicturePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jpRemovePicturePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
                    .add(jbRemovePicture))
                .addContainerGap())
        );
        jpRemovePicturePanelLayout.setVerticalGroup(
            jpRemovePicturePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jpRemovePicturePanelLayout.createSequentialGroup()
                .add(jScrollPane8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(jbRemovePicture)
                .addContainerGap())
        );

        jbOpenFolder_File.setText("Open Folder");
        jbOpenFolder_File.setMaximumSize(new java.awt.Dimension(155, 23));
        jbOpenFolder_File.setMinimumSize(new java.awt.Dimension(155, 23));
        jbOpenFolder_File.setPreferredSize(new java.awt.Dimension(155, 23));
        jbOpenFolder_File.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbOpenFolder_FileActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jbOpenFolder_File, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 146, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(4, 4, 4)
                .add(jbOpenFolder_File, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(189, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanel13Layout = new org.jdesktop.layout.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel13Layout.createSequentialGroup()
                .add(13, 13, 13)
                .add(jPanel10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jpRemovePicturePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 79, Short.MAX_VALUE)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel13Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jpRemovePicturePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        add(jPanel13, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Action when "Add" button is clicked
     *
     * Opens a new editable line, inserting a new row if needed.
     *
     * @param evt
     */
    // First pass done
    private void jbAdd_propertieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAdd_propertieActionPerformed

        Pair<Boolean,Integer> result = model.addNewEditableRow();

        // Sanity check
        if (result==null) return;

        if (result.first) {
                jtPropertiesTable.changeSelection(result.last, 0, false, false);
                jtPropertiesTable.editCellAt(result.last, 0);
            } else {
                jtPropertiesTable.editCellAt(result.last, 0);
                jtPropertiesTable.changeSelection(result.last, 1, false, false);
            }
            model.writeHeader();
}//GEN-LAST:event_jbAdd_propertieActionPerformed

    /**
     * Action when "Remove" button is clicked
     *
     * Deletes a row in the properties table
     *
     * @param evt
     */
    // First pass done
    private void jbRemove_propertieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbRemove_propertieActionPerformed
        //Deletes a row in the properties table.
        int selected_row = jtPropertiesTable.getSelectedRow();

        if (selected_row != -1 && selected_row > 4) {
            if (jtPropertiesTable.isEditing()) {
                jtPropertiesTable.editCellAt(0, 0);
            }
            jtPropertiesTable.changeSelection((selected_row - 1), 0, false, false);
            model.removeRow(selected_row);
        }
        model.writeHeader();
}//GEN-LAST:event_jbRemove_propertieActionPerformed

    /**
     * Action when "Remove Picture" button is clicked
     *
     * @param evt
     */
    // First pass sort of done... Thumbnails needs to have a look at
    private void jbRemovePictureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbRemovePictureActionPerformed
        //Removes a picture from project, storing a reason.
        if (model.hasSelectedImage()) {
            ImageThumbnailButton selectedImage = model.getSelectedImage();
            jpDSPPreviewThumbs.remove(selectedImage);

            // Remove selected image from data model, and give reason
            model.removeSelectedImage(jtRemoveReason.getText());
            jpDSPPreviewThumbs.validate();
            jpDSPPreviewThumbs.repaint();


            //For all images set the thumbnails for the other plugins.
            UserInteractionInterface parent = (UserInteractionInterface) this.getParent();
            thumbnails = new TreeMap<UUID, BufferedImage>();
            for (ImageContainer container : model.getProjectImages().values()) {
                thumbnails.put(container.getImageId(), container.getThumbnailImage());
            }
            parent.setImagesThumbnails(thumbnails);
            this.getDataCache().remove("Cached Original Thumbs");
            this.getDataCache().put("Cached Original Thumbs", thumbnails);


            //There is a minimum of 6 picture in a project.
            if (model.getProjectImages().size() < 2) {
                jtRemoveReason.setText("You need at least 1 picture.");
                jtRemoveReason.setEnabled(false);
            }
            model.setSelectedImage(null);

            //Set the carrier time stamp, to indicate action
            model.getXMLHandler().incTimestamp();

            try {
                model.getXMLHandler().writeXML();
            } catch (Exception ex) {
                Logger.getLogger(PluginPanelView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.gc();
}//GEN-LAST:event_jbRemovePictureActionPerformed

    /**
     * Action when "Open Folder" button is clicked
     *
     * @param evt
     */
    // First pass done
    private void jbOpenFolder_FileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbOpenFolder_FileActionPerformed
        if (model.getOpenChoice() == OPEN_FOLDER) {
            openFolder();
        } else if (model.getOpenChoice() == OPEN_FILE_LP) {
            openLPFile();
        } else if (model.getOpenChoice() == OPEN_DOME_LP) {
            openDomeLPFile();
        }
    }//GEN-LAST:event_jbOpenFolder_FileActionPerformed

    /**
     * Action when "Save" button is clicked
     *
     * @param evt
     */
    // First pass done
    private void save_table_properties_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_save_table_properties_buttonActionPerformed
        // TODO add your handling code here:
        model.writeHeader();
        save_table_properties_button.setVisible(false);
    }//GEN-LAST:event_save_table_properties_buttonActionPerformed

    /**
     * Action triggered whenever a property in the Project Properties table is
     * changed.
     *
     * @param evt
     */
    // Huh? Figure out what this is supposed to do exactly? First pass done anyway.
    private void jtPropertiesTablePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jtPropertiesTablePropertyChange
        //if (model==null) return; // Sanity check
        if (model.getValueAt(0, 0) != null && !model.getValueAt(0, 0).equals("")) {
            save_table_properties_button.setVisible(true);
        }
    }//GEN-LAST:event_jtPropertiesTablePropertyChange

    public void openFolder() {

        boolean canceled = false;

        File[] SelectedFiles = null;
        //File chooser for selecting the image folder.
        JFileChooser jfc = new JFileChooser("Select image folder");
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int result = jfc.showOpenDialog(new JFrame());

        if (result == JFileChooser.APPROVE_OPTION) {
            if (jfc.getSelectedFile().isFile()) {
                System.err.println("Is not a directory.");
                canceled = true;
            } else {
                //Load the images in the folder.
                File projectFolder = jfc.getSelectedFile();
                File image_folder = new File(projectFolder.getAbsoluteFile() + File.separator + model.getImageExportsDir());
                if (image_folder.isDirectory()) {

                    model.setCurrentSelectedFolder(projectFolder);
                    SelectedFiles = image_folder.listFiles();
                    if (SelectedFiles.length == 0) {
                        JOptionPane.showMessageDialog(null, "There are no files in folder.", "Empty Folder", JOptionPane.ERROR_MESSAGE);
                        canceled = true;
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "The " + model.getImageExportsDir() + " folder is missing in your project directory.", "Folder missing", JOptionPane.ERROR_MESSAGE);
                    canceled = true;
                }
            }
        } else if (result == JFileChooser.CANCEL_OPTION) {
            canceled = true;
        }

        if (!canceled) {
            final ProgressBarPopup pbp = new ProgressBarPopup(0, SelectedFiles.length);
//			final int size = SelectedFiles.length;
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    pbp.createAndShowGUI("Loading images");
                }
            });
            openImageFilesTask = new OpenImageFilesTask(pbp, SelectedFiles, model.getReader(), this);
            openImageFilesTask.execute();
        }
    }


    public void openLPFile() {

        File selectedFile = null;
        File folder = null;

        LPmenu lpmenu = new LPmenu(null, true);
        lpmenu.showLPMenu();

        if (lpmenu.isLPFileSelected()) {
            selectedFile = lpmenu.getLPFile();
            folder = lpmenu.getProjectFolder();

            int size = model.numberLinesFile(selectedFile) - 1;
            System.out.println("Nlinhas: " + size);
            final ProgressBarPopup pbp = new ProgressBarPopup(0, size);
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    pbp.createAndShowGUI("Loading images");
                }
            });

            openLPFileTask = new OpenLPFileTask(pbp, selectedFile, null, folder, false, null, 0, 0, this, model.getReader());
            openLPFileTask.execute();

        }
    }

    public void openDomeLPFile() {
        File SelectedFile;

        //File chooser for selecting the lp file and prefix if needed
        DomeLPmenu lp_menu = new DomeLPmenu(null, true);
        lp_menu.showLPMenu();

        if (lp_menu.isLPFileSelected()) {
            SelectedFile = lp_menu.getLPFile();

            File images_folder = lp_menu.getImageFolder();
            File project_folder = lp_menu.getProjectFolder();
            String filename = null; //lp_menu.getImagesAbsolutePath();
            int start = 0; //lp_menu.getImagesStartNumber();
            int digit_number = 0; //lp_menu.getDigitNumber();
            //open images and extract lp values


            int size = model.numberLinesFile(SelectedFile) - 1;
            final ProgressBarPopup pbp = new ProgressBarPopup(0, size);
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    pbp.createAndShowGUI("Loading images");
                }
            });

            openLPFileTask = new OpenLPFileTask(pbp, SelectedFile, images_folder, project_folder, true, filename, start, digit_number, this, model.getReader());
            openLPFileTask.execute();
        }
    }


    public void initPreviewMenu()
    {
        initPreviewMenu(true);
    }

    public void initPreviewMenu(boolean addLPinfo) {


        model.initPreviewMenuModel(this, addLPinfo);

        preview_panel = new PreviewPanel();

        Preview_Panel_jp.add(preview_panel);
        Preview_Panel_jp.setVisible(true);
        preview_panel.setVisible(true);
        //preview_panel.repaint();

        Iterator<ImageContainer> itImageContainer = model.getProjectImagesIterator();
        int s = model.getProjectImages().size();
        System.out.println("Images: " + s);
        int i = 0;
        jpDSPPreviewThumbs.removeAll();
        jtRemoveReason.setEnabled(s > 1);
        jbRemovePicture.setEnabled(s > 1);


        ImageThumbnailButton imgBt = null;

        //For all images
        while (itImageContainer.hasNext()) {

            ImageContainer imgContainer = itImageContainer.next();

            //New thumbnail button for each image
            imgBt = new ImageThumbnailButton(imgContainer.getImageName(), imgContainer.getImageId());
            imgBt.getImageButton().addActionListener(new java.awt.event.ActionListener() {

                //A listener for each button, so if a click is made the image is shown on panel.
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    //Get the image thumbnail button parent of the clicked image button
                    JButton image_button = (JButton) (evt.getSource());
                    ImageThumbnailButton itb = (ImageThumbnailButton) (image_button.getParent());
                    model.setSelectedImage(itb);
                    BufferedImage img_aux = model.getBufferedImageByID(itb.getImageId());
                    preview_panel.setImage(img_aux);
                    preview_panel.repaint();
                }
            });
            //Inserting the button on a panel with the picture name, or a check box for image selection.
            imgBt.setLabelMode();
            imgBt.setIcon(imgContainer.getIcon());
            jpDSPPreviewThumbs.add(imgBt);
            System.gc();
            i++;
//            parent.getProgressBar().setValue(i);

        }
        if (imgBt != null) {
            jpDSPPreviewThumbs.setSize(jpDSPPreviewThumbs.getWidth() - 1, jpDSPPreviewThumbs.getHeight());
            jpDSPPreviewThumbs.validate();
            jpDSPPreviewThumbs.repaint();
            jpDSPPreviewThumbs.setSize(jpDSPPreviewThumbs.getWidth() + 1, jpDSPPreviewThumbs.getHeight());
            //Do a click on a picture to make it apear on preview panel.
            imgBt.getImageButton().doClick();
        }

        //Actually SHOULDN'T set the carrier time stamp, to indicate action
        //model.getXMLHandler().incTimestamp();
        System.out.println("Time open folder end" + model.getXMLHandler().getTimestamp());

        try {
            model.getXMLHandler().writeXML();
        } catch (Exception ex) {
            Logger.getLogger(PluginPanelView.class.getName()).log(Level.SEVERE, null, ex);
        }


        //For all images set the thumbnails for the other plugins.
        UserInteractionInterface parent = (UserInteractionInterface) this.getParent();
        thumbnails = new TreeMap<UUID, BufferedImage>();
        for (ImageContainer container : model.getProjectImages().values()) {
            thumbnails.put(container.getImageId(), container.getThumbnailImage());
        }

        if (parent!=null)
        {
            parent.setImagesThumbnails(thumbnails);
            this.getDataCache().remove("Cached Original Thumbs");
            this.getDataCache().put("Cached Original Thumbs", thumbnails);
            parent.done();
        }
        Preview_Panel_jp.validate();
        Preview_Panel_jp.repaint();
        //Preview_Panel_jp.getParent().repaint();

    }



    /**Sets the plugin current selected folder*/
    public void setCurrentFolder(File folder) {
        model.setCurrentSelectedFolder(folder);
    }

    /**Set the project images*/
    public synchronized void setProjectImages(TreeMap<UUID, ImageContainer> images) {
        model.setProjectImages(images);
    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Preview_Panel_jp;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JSplitPane jSplitPane5;
    private javax.swing.JButton jbAdd_propertie;
    private javax.swing.JButton jbOpenFolder_File;
    private javax.swing.JButton jbRemovePicture;
    private javax.swing.JButton jbRemove_propertie;
    private javax.swing.JPanel jpDSPPreviewThumbs;
    private javax.swing.JPanel jpRemovePicturePanel;
    private javax.swing.JTable jtPropertiesTable;
    private javax.swing.JTextArea jtRemoveReason;
    private javax.swing.JButton save_table_properties_button;
    // End of variables declaration//GEN-END:variables

    public iDataCache getDataCache() {

        return cache;
    }

    public void setDataCache(iDataCache cache) {
        this.cache = cache;
    }

    TreeMap<UUID, ImageContainer> getProjectImages()
    {
        return model.getProjectImages();
    }
}
