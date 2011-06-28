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
import ModuleInterfaces.UserConfig;
import ModuleInterfaces.UserInteractionInterface;
import XMLcarrier.Exceptions.XMLNotAvailable;
import XMLcarrier.Exceptions.XSDCantValidadeXML;
import XMLcarrier.XMLHandler;
import XMLcarrier.Process;
import java.awt.CardLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import RTIbuilder.pluginhelpers.Pipeline;
import RTIbuilder.pluginhelpers.PipelineParser;



        import RTIbuilder.pluginhelpers.Plugin;
import RTIbuilder.pluginhelpers.PluginParser;
import XMLcarrier.HeaderInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

public class InitialMenu extends javax.swing.JPanel {

	/** Name of the class, for layout purposes*/
	public static final String PANEL_NAME = "Initial_card";
    /** Instance of the main application*/
    private LPtrackerView parent;
    /**List of diferent pipelines for execution*/
    private Pipeline selected_pipeline;
    /**List of diferent pipelines for execution*/
    private ArrayList<Pipeline> pipelines;
    /**The chosen pipeline*/
    String selected_pipeline_name = "";
    /**The selected project name*/
    String selected_project_name = null;

    /** Creates new form InitialMenu */
    public InitialMenu(LPtrackerView parent) {
        this.parent = parent;
        initComponents();
        // Removing unwanted button for now.
        jPanel5.remove(jButton_customize_pipeline);
        //jButton_open_xmlCarrier.setVisible(false);
        initInitialMenu();

    }

    public void initInitialMenu() {

        parent.pipelines = PipelineParser.getPipelines();
        parent.plugins = PluginParser.getPlugins();
        for (Pipeline p : parent.pipelines) {
            p.loadPlugins(parent.plugins);
        }
        for (Plugin p : parent.plugins) {
            UserInteractionInterface gui = p.getUser_interface();
            parent.getPluginGUI().add(gui, p.getPluginName());
            System.out.println("PLUGIN LOADED: " + p.getPluginName());
        }

        parent.getPluginGUI().validate();
        pipelines = parent.pipelines;
        selected_pipeline = null;
        jTextField_project_name.setText(parent.project_name);
        jButton_begin_pipeline.setEnabled(false);

        SelectionListener listener = new SelectionListener(jTable_pipeline_list);
        jTable_pipeline_list.getSelectionModel().addListSelectionListener((ListSelectionListener) listener);
        jTable_pipeline_list.getColumnModel().getSelectionModel().addListSelectionListener(listener);

        for (Pipeline p : pipelines) {
            addPipeline(p);
        }

    }

    private class SelectionListener implements ListSelectionListener {

        private JTable table;

        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        SelectionListener(JTable table) {
            this.table = table;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            // If cell selection is enabled, both row and column change events are fired
            if (e.getSource() == table.getSelectionModel() && table.getRowSelectionAllowed()) {
                // Column selection changed
//                int first = e.getFirstIndex();
//                int last = e.getLastIndex();
//                out.println("Column selection changed");

                if (table.getSelectedRow() != -1) {
                    String name = (String) ((DefaultTableModel) jTable_pipeline_list.getModel()).getValueAt(table.getSelectedRow(), 0);
                    for (Pipeline p : pipelines) {
                        if (p.getName().equals(name)) {
                            selected_pipeline = p;
                        }
                    }
                } else {
                    selected_pipeline = null;
                }
                validateStart();
            } else if (e.getSource() == table.getColumnModel().getSelectionModel() && table.getColumnSelectionAllowed()) {
                // Row selection changed
//                int first = e.getFirstIndex();
//                int last = e.getLastIndex();
//                out.println("Row selection changed");
            }

            if (e.getValueIsAdjusting()) {
                // The mouse button has not yet been released
            }
        }
    }

    public void resetTablePipeline() {
        DefaultTableModel model = (DefaultTableModel) jTable_pipeline_list.getModel();
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }


        for (Pipeline p : pipelines) {
            addPipeline(p);
        }
    }

    private void addPipeline(Pipeline p) {

        int i = 0;
        String[] plugins_used = new String[p.getPlugins().size()];
        for (Plugin pl : p.getPlugins().values()) {
            i++;
            plugins_used[p.getPluginOrder(pl) - 1] = pl.getPluginName();
        }

        StringBuffer sb = new StringBuffer();
        for (i = 0; i < plugins_used.length; i++) {
            sb.append(plugins_used[i]);
            sb.append(" -> ");
        }
        String s = sb.toString();
        s = s.substring(0, s.length() - 4);
        ((DefaultTableModel) jTable_pipeline_list.getModel()).insertRow(0,
                new Object[]{p.getName(), s});
    }

    private void validateStart() {
        String text = jTextField_project_name.getText().trim();
        if (!text.equalsIgnoreCase("") && selected_pipeline != null) {
            jButton_begin_pipeline.setEnabled(true);
        } else {
            jButton_begin_pipeline.setEnabled(false);
        }
    }

    private boolean valideProjectName() {
        String text = jTextField_project_name.getText().trim();
        boolean valid = true;
        if (!text.equalsIgnoreCase("") && selected_pipeline != null) {
            String regex = "[A-Za-z0-9_-]";
            StringBuffer suggestionName = new StringBuffer("");
            int i;
            for (i = 0; i < text.length(); i++) {
                String c = text.charAt(i) + "";
                if (!c.matches(regex)) {
                    valid = false;
                } else {
                    suggestionName.append(c);
                }
            }

            if (valid == false) {

                String suggestion = suggestionName.toString().trim();
                if (suggestion.equalsIgnoreCase("")) {
                    suggestion = "RTIbuilderProject";
                }
                String title = "Invalid Project name";
                String message = "Project name must only contain letters (A-Z/a-z), numbers (0-9) and underscores (_)!\n";
                message += "No whitespace is allowed!\n";
                message += "Please change project name!\n\n";
                message += "Suggestion: " + suggestion;
                JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
                jButton_begin_pipeline.setEnabled(false);
            } else {
                jButton_begin_pipeline.setEnabled(true);
            }
            jButton_begin_pipeline.setEnabled(true);
        } else {
            valid = false;
            jButton_begin_pipeline.setEnabled(false);
        }
        return valid;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel_title = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jTextField_project_name = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jButton_begin_pipeline = new javax.swing.JButton();
        jButtonOpenXMLProject = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jButton_customize_pipeline = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable_pipeline_list = new javax.swing.JTable();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 20));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.BorderLayout());

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(InitialMenu.class);
        jLabel_title.setBackground(resourceMap.getColor("jLabel_title.background")); // NOI18N
        jLabel_title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_title.setIcon(resourceMap.getIcon("jLabel_title.icon")); // NOI18N
        jLabel_title.setText(resourceMap.getString("jLabel_title.text")); // NOI18N
        jLabel_title.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel_title.setName("jLabel_title"); // NOI18N
        jPanel1.add(jLabel_title, java.awt.BorderLayout.NORTH);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), resourceMap.getString("jPanel3.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel3.border.titleFont"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel10.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 30, 15, 30));
        jPanel10.setName("jPanel10"); // NOI18N
        jPanel10.setOpaque(false);
        jPanel10.setLayout(new javax.swing.BoxLayout(jPanel10, javax.swing.BoxLayout.LINE_AXIS));

        jTextField_project_name.setText(resourceMap.getString("jTextField_project_name.text")); // NOI18N
        jTextField_project_name.setName("jTextField_project_name"); // NOI18N
        jTextField_project_name.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_project_nameActionPerformed(evt);
            }
        });
        jTextField_project_name.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField_project_nameKeyReleased(evt);
            }
        });
        jPanel10.add(jTextField_project_name);

        jPanel3.add(jPanel10, java.awt.BorderLayout.PAGE_START);

        jPanel1.add(jPanel3, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.PAGE_START);

        jPanel6.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 25, 10, 25));
        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setOpaque(false);
        jPanel6.setLayout(new javax.swing.BoxLayout(jPanel6, javax.swing.BoxLayout.LINE_AXIS));

        jPanel7.setName("jPanel7"); // NOI18N
        jPanel7.setOpaque(false);

        jButton_begin_pipeline.setBackground(resourceMap.getColor("jButton_begin_pipeline.background")); // NOI18N
        jButton_begin_pipeline.setText(resourceMap.getString("jButton_begin_pipeline.text")); // NOI18N
        jButton_begin_pipeline.setMargin(new java.awt.Insets(5, 2, 5, 2));
        jButton_begin_pipeline.setName("jButton_begin_pipeline"); // NOI18N
        jButton_begin_pipeline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_begin_pipelineActionPerformed(evt);
            }
        });
        jPanel7.add(jButton_begin_pipeline);

        jButtonOpenXMLProject.setText(resourceMap.getString("jButtonOpenXMLProject.text")); // NOI18N
        jButtonOpenXMLProject.setName("jButtonOpenXMLProject"); // NOI18N
        jButtonOpenXMLProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenXMLProjectActionPerformed(evt);
            }
        });
        jPanel7.add(jButtonOpenXMLProject);

        jPanel6.add(jPanel7);

        jPanel11.setName("jPanel11"); // NOI18N
        jPanel11.setLayout(new java.awt.GridBagLayout());
        jPanel6.add(jPanel11);

        add(jPanel6, java.awt.BorderLayout.SOUTH);

        jPanel9.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 20));
        jPanel9.setName("jPanel9"); // NOI18N
        jPanel9.setLayout(new java.awt.BorderLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel2.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel2.border.titleFont"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel8.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 30, 30, 30));
        jPanel8.setName("jPanel8"); // NOI18N
        jPanel8.setLayout(new java.awt.BorderLayout());

        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setLayout(new javax.swing.BoxLayout(jPanel5, javax.swing.BoxLayout.LINE_AXIS));

        jButton_customize_pipeline.setText(resourceMap.getString("jButton_customize_pipeline.text")); // NOI18N
        jButton_customize_pipeline.setEnabled(false);
        jButton_customize_pipeline.setName("jButton_customize_pipeline"); // NOI18N
        jButton_customize_pipeline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_customize_pipelineActionPerformed(evt);
            }
        });
        jPanel5.add(jButton_customize_pipeline);

        jPanel8.add(jPanel5, java.awt.BorderLayout.PAGE_END);

        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTable_pipeline_list.setFont(resourceMap.getFont("jTable_pipeline_list.font")); // NOI18N
        jTable_pipeline_list.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Operation Sequence"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable_pipeline_list.setName("jTable_pipeline_list"); // NOI18N
        jTable_pipeline_list.setRowHeight(20);
        jTable_pipeline_list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable_pipeline_list.setShowHorizontalLines(false);
        jTable_pipeline_list.setShowVerticalLines(false);
        jScrollPane1.setViewportView(jTable_pipeline_list);
        jTable_pipeline_list.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jTable_pipeline_list.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTable_pipeline_list.columnModel.title0")); // NOI18N
        jTable_pipeline_list.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTable_pipeline_list.columnModel.title1")); // NOI18N

        jPanel4.add(jScrollPane1);

        jPanel8.add(jPanel4, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel8, java.awt.BorderLayout.CENTER);

        jPanel9.add(jPanel2, java.awt.BorderLayout.CENTER);

        add(jPanel9, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField_project_nameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_project_nameActionPerformed
        parent.project_name = jTextField_project_name.getText();
        validateStart();
    }//GEN-LAST:event_jTextField_project_nameActionPerformed

    private void jButton_customize_pipelineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_customize_pipelineActionPerformed

        parent.pipelineMenu.initPipelineMenu();
        CardLayout cl = (CardLayout) parent.getMainPanel().getLayout();
        cl.show(parent.getMainPanel(), PipelineMenu.PANEL_NAME);
    }//GEN-LAST:event_jButton_customize_pipelineActionPerformed

    private void jButton_begin_pipelineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_begin_pipelineActionPerformed

        if (!valideProjectName()) {
            System.out.println("Project name not valid");
            return;
        }

        //See if there is a need to start a new plug-in, or just go forward
        boolean start = !(selected_pipeline.getName().equals(selected_pipeline_name) && selected_project_name != null && selected_project_name.equals(jTextField_project_name.getText()));

        parent.pipeline_in_use = selected_pipeline;
        parent.pipeline_in_use.setStage(1);
        Plugin p = selected_pipeline.getStagePlugin();

        if (start) {
            // Create a temporary directory for RTIbuilder
            String path = System.getProperty("user.home") + File.separator + ".rtibuilder";
            File dir = new File(path);
            if (!dir.exists())
            {
                boolean succ = dir.mkdir();
                if (!succ)
                {
                    JOptionPane.showMessageDialog(null, "Cannot create temporary directory: "+path+"\nPlease make sure you have permissions to write to it.", "Unable to create temporary directory", JOptionPane.ERROR_MESSAGE);
                }
            }
            // Grab UserConfig, if it exists
            UserConfig config = new UserConfig(path + File.separator + ".userconfig");

            //Create the XMLcarrier path correspondent carrier
            parent.xmlPath = new StringBuffer(path + File.separator + parent.project_name + ".xml");
            parent.carrier = new XMLHandler(parent.xmlPath.toString());
            parent.carrier.createXML();

            // Add "New Project" marker


            // Insert process information to record intended pipeline
            HashMap<UUID, Plugin> plugins = selected_pipeline.getPlugins();
            while (!plugins.isEmpty())
            {
                int order = Integer.MAX_VALUE;
                Map.Entry<UUID,Plugin> plug = null;
                Iterator i = plugins.entrySet().iterator();
                Map.Entry<UUID,Plugin> plugT = null;
                // Seek first plugin in the pipeline order remaining in list
                for ( ; i.hasNext() ; )
                {
                    plugT = (Map.Entry<UUID,Plugin>)i.next();
                    if (selected_pipeline.getPluginOrder(plugT.getValue()) < order)
                    {
                        plug = plugT;
                        order = selected_pipeline.getPluginOrder(plugT.getValue());
                    }
                }
                // Remove chosen plugin from list
                plugins.remove(plug.getKey());

                // Insert information into XMLcarrier
                XMLcarrier.Process proc = new XMLcarrier.Process();

                proc.setStatus("NONE");
                proc.setComponentID(plug.getKey().toString());
                proc.setId(UUID.randomUUID().toString());
                proc.setType("PIPELINE");
                proc.setSequenceNumber(""+order);

                parent.carrier.addProcess(proc);
            }

            // Initialize some header info
            String user = System.getProperties().getProperty("user.name");
            UUID id = UUID.randomUUID();
            String filename = parent.project_name;

            HeaderInfo header = new HeaderInfo();
            header.setUuid(id.toString());
            header.setAuthor(user);
            header.setProjectName(filename);
            header.setCreationDate(parent.carrier.generateDate());
            header.setHost(parent.carrier.generateHost());
            header.setLastModDate(parent.carrier.generateDate());
            header.setMemoryAvailable(parent.carrier.generateMemInfo());
            header.setOperatingSystem(parent.carrier.generateOSVersion());
            header.setProcessorInfo(parent.carrier.generateProcessorInfo());
            header.setUserInfo(parent.carrier.generateUserInfo());

            parent.carrier.setHeaderInfo(header);
            parent.carrier.setTimestamp(1);
            
            //Write the initial inputs in the XMLcarrier
            try {
                parent.carrier.writeXML();
            } catch (Exception e) {
                e.printStackTrace();
            }

            parent.cache = new DataCache();
            p.getUser_interface().replaceDataCache(parent.cache);
            parent.cache.put("UserConfig", config);
            try {
                p.setMetaInfo(new PluginMetaInfo(p.getUuid(), 1));
                p.getUser_interface().start(parent.xmlPath, false);
            } catch (ArgumentException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error in the plug-in inputs", JOptionPane.ERROR_MESSAGE);
            } catch (XMLcarrierException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error on XMLcarrier", JOptionPane.ERROR_MESSAGE);
            } catch (ModuleException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error in the plug-in", JOptionPane.ERROR_MESSAGE);
            }
            parent.setTimestamp(0);
            p.getUser_interface().displayPluginInterface();
            p.getUser_interface().setAdvanceComponet(parent.getNext());
            p.getUser_interface().setParentApplication(parent);

            selected_pipeline_name = selected_pipeline.getName();
            selected_project_name = jTextField_project_name.getText();
        }
        CardLayout cl = (CardLayout) parent.getMainPanel().getLayout();
        cl.show(parent.getMainPanel(), "PluginPanel");
        CardLayout clp = (CardLayout) parent.getPluginGUI().getLayout();
        clp.show(parent.getPluginGUI(), p.getPluginName());
    }//GEN-LAST:event_jButton_begin_pipelineActionPerformed

	private void jTextField_project_nameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField_project_nameKeyReleased
            parent.project_name = jTextField_project_name.getText();
            validateStart();
	}//GEN-LAST:event_jTextField_project_nameKeyReleased

        public void performOpenXMLbuttonclick()
        {
            // Create a temporary directory for RTIbuilder
            String path = System.getProperty("user.home") + File.separator + ".rtibuilder";
            File dir = new File(path);
            if (!dir.exists()) {
                boolean succ = dir.mkdir();
                if (!succ) {
                    JOptionPane.showMessageDialog(null, "Cannot create temporary directory: " + path + "\nPlease make sure you have permissions to write to it.", "Unable to create temporary directory", JOptionPane.ERROR_MESSAGE);
                }
            }
            // Grab UserConfig, if it exists
            UserConfig config = new UserConfig(path + File.separator + ".userconfig");

            //this.jButton_open_xmlCarrierActionPerformed(null);
            // Create and launch an open file dialog window
            File jfcFilePath;
            if (config.readEntry("RTIBuilder.LastOpened")!=null)
                jfcFilePath = new File(config.readEntry("RTIBuilder.LastOpened"));
            else
                jfcFilePath = new File(".");

            System.out.println("Starting Open on: "+jfcFilePath.getAbsolutePath());

            JFileChooser jfc = new JFileChooser(jfcFilePath);
            //JFileChooser jfc = new JFileChooser("Select XML File");
            jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            // Not forgetting to allow only XML files to be chosen
            jfc.setFileFilter(new FileFilter() {
                public boolean accept(File f)
                {
                    return f.getName().endsWith(".xml") || f.isDirectory();
                }

                public String getDescription() {return "XML Files and Folders";}
            });

            // Launch!
            int result = jfc.showOpenDialog(new JFrame());

            if (result == JFileChooser.APPROVE_OPTION) {
                // Alright, XML file OR folder chosen
                System.out.println("Chose file:"+jfc.getSelectedFile().getAbsolutePath());
                File f = jfc.getSelectedFile();
                if (!f.isFile())
                {
                    // -_- Chose folder, or bad file somehow
                    return;
                }
                else
                {
                    // try reading XML
                    XMLHandler xmlcarrier = new XMLHandler(f.getAbsolutePath());
                    boolean valid = false;
                    try {
                        xmlcarrier.loadXML();
                        valid = true;
                    } catch (XMLNotAvailable ex) {
                        Logger.getLogger(InitialMenu.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (XSDCantValidadeXML ex) {
                        Logger.getLogger(InitialMenu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //xmlcarrier.getTimestamp();
                    if (valid)
                    {
                        // Remember path
                        config.writeEntry("RTIBuilder.LastOpened", f.getParent());
                        // Load it up

                        // Step 1 - Ensure we know and have the required plugins available
                        //          and initialize a pipeline
                        ArrayList<Process> procs = xmlcarrier.getAllProcesses();
                        Pipeline pipe = new Pipeline();

                        int stageCount = 0;
                        for(Process p : procs)
                        {
                            if(p.getType().equals("PIPELINE"))
                            {
                                stageCount++;
                                //pipe.addStage(new Integer(p.getSequenceNumber()));
                                String uuid = p.getComponentID();
                                for (Plugin plug : parent.plugins)
                                {
                                    if (plug.getUuid().toString().equals(uuid))
                                    {
                                        pipe.addPlugin(new Integer(p.getSequenceNumber()), plug);
                                        plug.setExecuted(false);
                                    }
                                }
                            }
                        }

                        int pipelineOption = javax.swing.JOptionPane.NO_OPTION;
                        // Sanity/Option check...
                        if (this.selected_pipeline!=null && stageCount == this.selected_pipeline.getPlugins().size())
                        {
                            pipelineOption = javax.swing.JOptionPane.showConfirmDialog(null, "Use selected pipeline instead of the stored project pipeline?","Select pipeline",javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        }

                        if (pipelineOption == javax.swing.JOptionPane.CANCEL_OPTION) return;
                        
                        // Sanity check... What if the XML file has NO Pipeline Info?
                        // Use Selected Pipeline instead, then.
                        if (stageCount==0 || pipelineOption == javax.swing.JOptionPane.YES_OPTION)
                        {
                            pipe = this.selected_pipeline;
                            // And sanity check
                            if (pipe==null)
                            {
                                int pickedOption = javax.swing.JOptionPane.showConfirmDialog(null, "No Pipeline information could be obtained from the XML file - please pick the corresponding pipeline from the list and try again.","Missing Pipeline",javax.swing.JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }

                        // Step 2 - Make sure the project's path matches ... if not, change it.

                        String oldpath = xmlcarrier.getProjectInfo().getParamterByName("ProjectPath");
                        File oldPath = new File(oldpath);
                        File xmlPath = f;
                        if (xmlPath.isFile()) xmlPath = xmlPath.getParentFile();

                        if (!oldPath.toString().equals(xmlPath.toString()))
                        {
                            int pickedOption = javax.swing.JOptionPane.showConfirmDialog(null, "The project seems to have been moved from its original path. In order to continue, RTIBuilder must register its new location.\nContinue loading?","Mismatching paths",javax.swing.JOptionPane.YES_NO_OPTION);
                            // If user doesn't want to update, he'll choose not to and nothing happens.
                            if (pickedOption == javax.swing.JOptionPane.NO_OPTION)
                            {
                                return;
                            }
                            XMLcarrier.HeaderInfo header = xmlcarrier.getProjectInfo();
                            header.getMap().remove("ProjectPath");
                            header.getMap().put("ProjectPath", xmlPath.getAbsolutePath());
                            System.out.println("Rebased XML to "+xmlPath.getAbsolutePath());
                            header.setTimestamp(xmlcarrier.getTimestamp());
                            xmlcarrier.setHeaderInfo(header);
                            try {
                                xmlcarrier.writeXML();
                            } catch (Exception ex) {
                                Logger.getLogger(InitialMenu.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        // Step 3 - Find out which stage to load
                        int stage = xmlcarrier.getTimestamp();
                        //Process p = (Process)procs.get(stage-1);
                        pipe.setStage(stage);

                        // Step 4 - Load the plugin and start it using the loaded xml
                        parent.pipeline_in_use=pipe;
                        Plugin plug = parent.pipeline_in_use.getStagePlugin();

                        parent.carrier=xmlcarrier;
                        parent.xmlPath = new StringBuffer(f.getAbsolutePath());

                        parent.cache = new DataCache();
                        plug.getUser_interface().replaceDataCache(parent.cache);
                        parent.cache.put("UserConfig", config);
                        try {
                            plug.setMetaInfo(new PluginMetaInfo(plug.getUuid(), parent.pipeline_in_use.getStage()));
                            plug.getUser_interface().start(parent.xmlPath, false);
                        } catch (ArgumentException ex) {
                            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error in the plug-in inputs", JOptionPane.ERROR_MESSAGE);
                        } catch (XMLcarrierException ex) {
                            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error on XMLcarrier", JOptionPane.ERROR_MESSAGE);
                        } catch (ModuleException ex) {
                            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error in the plug-in", JOptionPane.ERROR_MESSAGE);
                        }


                        plug.getUser_interface().displayPluginInterface();
                        plug.getUser_interface().setAdvanceComponet(parent.getNext());
                        plug.getUser_interface().setParentApplication(parent);

                        //selected_pipeline_name = selected_pipeline.getName();
                        //selected_project_name = jTextField_project_name.getText();

                        CardLayout cl = (CardLayout) parent.getMainPanel().getLayout();
                        cl.show(parent.getMainPanel(), "PluginPanel");
                        CardLayout clp = (CardLayout) parent.getPluginGUI().getLayout();
                        clp.show(parent.getPluginGUI(), plug.getPluginName());

                        // ... profit?
                        System.out.println(xmlcarrier.getTimestamp());
                    }

                }
            }
            else
            {
                if (result == JFileChooser.CANCEL_OPTION)
                {
                    return;
                }
                else
                {
                    // Deal with error?
                }
            }

        }

        /**
         * Open XML File pre-generated when button is clicked.
         *
         * @param evt
         */
        private void jButtonOpenXMLProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenXMLProjectActionPerformed
            // TODO add your handling code here:
            performOpenXMLbuttonclick();
        }//GEN-LAST:event_jButtonOpenXMLProjectActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonOpenXMLProject;
    private javax.swing.JButton jButton_begin_pipeline;
    private javax.swing.JButton jButton_customize_pipeline;
    private javax.swing.JLabel jLabel_title;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable_pipeline_list;
    private javax.swing.JTextField jTextField_project_name;
    // End of variables declaration//GEN-END:variables
}
