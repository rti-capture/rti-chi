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

import ModuleInterfaces.UserInteractionInterface;
import java.awt.CardLayout;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import RTIbuilder.pluginhelpers.Pipeline;
import RTIbuilder.pluginhelpers.PipelineParser;
import RTIbuilder.pluginhelpers.Plugin;
import RTIbuilder.pluginhelpers.PluginParser;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import static java.lang.System.out;

public class PipelineMenu extends javax.swing.JPanel {

	/** Name of the class, for layout purposes*/
	public static final String PANEL_NAME = "Pipeline_card";
	/** Instance of the main application*/
	private LPtrackerView parent;
	/**Loaded plugins*/
	private ArrayList<Plugin> plugins;
	/**List of diferent pipelines for execution*/
	private ArrayList<Pipeline> pipelines;
	/**List of diferent pipelines for execution*/
	private Pipeline selected_pipeline;

	/** Creates new form PipelineMenu */
	public PipelineMenu(LPtrackerView parent) {
		this.parent = parent;
		initComponents();
		jDialog_new_plugin.setVisible(false);

	}

	public void initPipelineMenu() {

		jButton_new_pipeline.setEnabled(false);
		jButton_new_plugin.setEnabled(false);
		jButton_save_as_pipeline.setEnabled(false);
		jButton_save_pipeline.setEnabled(false);

		plugins = parent.plugins;
		pipelines = parent.pipelines;

		resetTables();
		initTables();

	}

	public void resetTables() {

		DefaultTableModel model = (DefaultTableModel) jTable_pipeline_list.getModel();
		while (model.getRowCount() > 0) {
			model.removeRow(0);
		}
		model = (DefaultTableModel) jTable_plugin_list.getModel();
		while (model.getRowCount() > 0) {
			model.removeRow(0);
		}
		model = (DefaultTableModel) jTable_plugin_order.getModel();
		while (model.getRowCount() > 0) {
			model.removeRow(0);
		}
		model = (DefaultTableModel) jTable_plugins_to_add.getModel();
		while (model.getRowCount() > 0) {
			model.removeRow(0);
		}
	}

	public void initTables() {
		//set plugin table values
		for (Plugin p : plugins) {
			addNewPlugin(p);
		}

		//set plugin table values
		for (Pipeline p : pipelines) {
			addNewPipeline(p);
		}

		//set listeners
		SelectionListener listener = new SelectionListener(jTable_plugins_to_add);
		jTable_plugins_to_add.getSelectionModel().addListSelectionListener((ListSelectionListener) listener);
		jTable_plugins_to_add.getColumnModel().getSelectionModel().addListSelectionListener(listener);
		SelectionListener listener2 = new SelectionListener(jTable_plugin_order);
		jTable_plugin_order.getSelectionModel().addListSelectionListener((ListSelectionListener) listener2);
		jTable_plugin_order.getColumnModel().getSelectionModel().addListSelectionListener(listener2);
		SelectionListener listener3 = new SelectionListener(jTable_pipeline_list);
		jTable_pipeline_list.getSelectionModel().addListSelectionListener((ListSelectionListener) listener3);
		jTable_pipeline_list.getColumnModel().getSelectionModel().addListSelectionListener(listener3);


	}

	private class SelectionListener implements ListSelectionListener {

		private JTable table;
		private String last_pipeline = "";

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
				if (table.equals(jTable_pipeline_list)) {
					if (table.getSelectedRow() != -1) {
						String name = (String) ((DefaultTableModel) jTable_pipeline_list.getModel()).getValueAt(table.getSelectedRow(), 0);
						if (!last_pipeline.equals(name)) {
							last_pipeline = name;
							for (Pipeline p : pipelines) {
								if (p.getName().equals(name)) {
									setSelectedPipeline(p);
								}
							}
						}
					}
				} else if (table.getSelectedRow() != -1) {
					JTable table2 = null;
					if (table.equals(jTable_plugins_to_add)) {
						table2 = jTable_plugin_order;
					} else {
						table2 = jTable_plugins_to_add;
					}
					ListSelectionModel model = table2.getSelectionModel();
					int nrows = ((DefaultTableModel) table2.getModel()).getRowCount();
					model.removeSelectionInterval(0, nrows - 1);
				}
//                out.println("Column selection changed");
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

	public void addNewPlugin(Plugin p) {

		((DefaultTableModel) jTable_plugin_list.getModel()).insertRow(0,
			new Object[]{p.getPluginName(), p.getVersion(), p.getFilePath(), p.getUser_interface().getClass().getName()});

		((DefaultTableModel) jTable_plugins_to_add.getModel()).insertRow(0,
			new Object[]{p.getPluginName()});

		return;
	}

	public void addNewPipeline(Pipeline p) {

		((DefaultTableModel) jTable_pipeline_list.getModel()).insertRow(0,
			new Object[]{p.getName()});

		((ListSelectionModel) jTable_pipeline_list.getSelectionModel()).setSelectionInterval(0, 0);

		setSelectedPipeline(p);
	}

	public void setSelectedPipeline(Pipeline p) {
		int i = 0;

		String[] plugins_used = new String[p.getPlugins().size()];
		ArrayList<String> plugins_not_used = new ArrayList<String>();
		for (Plugin pl : p.getPlugins().values()) {
			i++;
			plugins_used[p.getPluginOrder(pl) - 1] = pl.getPluginName();
		}


		for (Plugin pl2 : plugins) {
			boolean used = false;
			for (i = 0; i < plugins_used.length; i++) {
				if (plugins_used[i].equals(pl2.getPluginName())) {
					used = true;
				}
			}
			if (!used) {
				plugins_not_used.add(pl2.getPluginName());
			}
		}

		DefaultTableModel model = (DefaultTableModel) jTable_plugin_order.getModel();
		while (model.getRowCount() > 0) {
			model.removeRow(0);
		}
		DefaultTableModel model2 = (DefaultTableModel) jTable_plugins_to_add.getModel();
		while (model2.getRowCount() > 0) {
			model2.removeRow(0);
		}

		for (i = 0; i < plugins_used.length; i++) {
			model.insertRow(0, new Object[]{plugins_used.length - i, plugins_used[plugins_used.length - i - 1]});
		}
		for (i = 0; i < plugins_not_used.size(); i++) {
			model2.insertRow(0, new Object[]{plugins_not_used.get(i)});
		}

		selected_pipeline = p;
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jDialog_new_plugin = new javax.swing.JDialog();
        jPanel_new_plugin = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel13 = new javax.swing.JPanel();
        jPanel_pipeline = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel10 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel_available_pipelines = new javax.swing.JLabel();
        jScrollPane_pipeline_list = new javax.swing.JScrollPane();
        jTable_pipeline_list = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jButton_new_pipeline = new javax.swing.JButton();
        jButton_open_pipeline = new javax.swing.JButton();
        jButton_rem_pipeline = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel_available_plugins = new javax.swing.JLabel();
        jScrollPane_plugins_to_add = new javax.swing.JScrollPane();
        jTable_plugins_to_add = new javax.swing.JTable();
        jPanel8 = new javax.swing.JPanel();
        jButton_add_plugin_to_pipe = new javax.swing.JButton();
        jButton_rem_plugin_from_pipe = new javax.swing.JButton();
        jButton_up_selected = new javax.swing.JButton();
        jButton_down_selected = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jLabel_pipeline_sequence = new javax.swing.JLabel();
        jScrollPane_plugin_order = new javax.swing.JScrollPane();
        jTable_plugin_order = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jButton_save_pipeline = new javax.swing.JButton();
        jButton_save_as_pipeline = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        jPanel_plugins = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jButton_new_plugin = new javax.swing.JButton();
        jButton_open_plugin = new javax.swing.JButton();
        jButton_remove_plugin = new javax.swing.JButton();
        jScrollPane_plugin_list = new javax.swing.JScrollPane();
        jTable_plugin_list = new javax.swing.JTable();
        jPanel11 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jButton_back = new javax.swing.JButton();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(RTIbuilder.RTIbuilderApp.class).getContext().getResourceMap(PipelineMenu.class);
        jDialog_new_plugin.setTitle(resourceMap.getString("jDialog_new_plugin.title")); // NOI18N
        jDialog_new_plugin.setName("jDialog_new_plugin"); // NOI18N
        jDialog_new_plugin.setResizable(false);

        jPanel_new_plugin.setName("jPanel_new_plugin"); // NOI18N
        jPanel_new_plugin.setPreferredSize(new java.awt.Dimension(350, 250));
        jPanel_new_plugin.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 6, 3, 6);
        jPanel_new_plugin.add(jLabel1, gridBagConstraints);

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 6, 3, 6);
        jPanel_new_plugin.add(jLabel2, gridBagConstraints);

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 6, 3, 6);
        jPanel_new_plugin.add(jLabel3, gridBagConstraints);

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 6, 3, 6);
        jPanel_new_plugin.add(jLabel4, gridBagConstraints);

        jTextField1.setText(resourceMap.getString("jTextField1.text")); // NOI18N
        jTextField1.setName("jTextField1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 6, 3, 6);
        jPanel_new_plugin.add(jTextField1, gridBagConstraints);

        jTextField2.setText(resourceMap.getString("jTextField2.text")); // NOI18N
        jTextField2.setName("jTextField2"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 6, 3, 6);
        jPanel_new_plugin.add(jTextField2, gridBagConstraints);

        jTextField3.setText(resourceMap.getString("jTextField3.text")); // NOI18N
        jTextField3.setName("jTextField3"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 6, 3, 6);
        jPanel_new_plugin.add(jTextField3, gridBagConstraints);

        jTextField4.setText(resourceMap.getString("jTextField4.text")); // NOI18N
        jTextField4.setName("jTextField4"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 6, 3, 6);
        jPanel_new_plugin.add(jTextField4, gridBagConstraints);

        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(8, 7, 8, 7);
        jPanel_new_plugin.add(jButton1, gridBagConstraints);

        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(8, 7, 8, 7);
        jPanel_new_plugin.add(jButton2, gridBagConstraints);

        jLabel5.setFont(resourceMap.getFont("jLabel5.font")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 6, 10, 6);
        jPanel_new_plugin.add(jLabel5, gridBagConstraints);

        jDialog_new_plugin.getContentPane().add(jPanel_new_plugin, java.awt.BorderLayout.CENTER);

        setAutoscrolls(true);
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(800, 600));
        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(280);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jPanel13.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 20, 20));
        jPanel13.setName("jPanel13"); // NOI18N
        jPanel13.setLayout(new javax.swing.BoxLayout(jPanel13, javax.swing.BoxLayout.LINE_AXIS));

        jPanel_pipeline.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel_pipeline.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel_pipeline.border.titleFont"))); // NOI18N
        jPanel_pipeline.setName("jPanel_pipeline"); // NOI18N
        jPanel_pipeline.setLayout(new javax.swing.BoxLayout(jPanel_pipeline, javax.swing.BoxLayout.LINE_AXIS));

        jPanel15.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        jPanel15.setName("jPanel15"); // NOI18N
        jPanel15.setLayout(new java.awt.BorderLayout());

        jSplitPane2.setBorder(null);
        jSplitPane2.setDividerLocation(300);
        jSplitPane2.setName("jSplitPane2"); // NOI18N

        jPanel10.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel10.setName("jPanel10"); // NOI18N
        jPanel10.setLayout(new javax.swing.BoxLayout(jPanel10, javax.swing.BoxLayout.LINE_AXIS));

        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setLayout(new java.awt.BorderLayout());

        jLabel_available_pipelines.setText(resourceMap.getString("jLabel_available_pipelines.text")); // NOI18N
        jLabel_available_pipelines.setName("jLabel_available_pipelines"); // NOI18N
        jPanel4.add(jLabel_available_pipelines, java.awt.BorderLayout.NORTH);

        jScrollPane_pipeline_list.setName("jScrollPane_pipeline_list"); // NOI18N

        jTable_pipeline_list.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable_pipeline_list.setName("jTable_pipeline_list"); // NOI18N
        jTable_pipeline_list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable_pipeline_list.setShowHorizontalLines(false);
        jTable_pipeline_list.setShowVerticalLines(false);
        jScrollPane_pipeline_list.setViewportView(jTable_pipeline_list);
        jTable_pipeline_list.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTable_pipeline_list.columnModel.title0")); // NOI18N

        jPanel4.add(jScrollPane_pipeline_list, java.awt.BorderLayout.CENTER);

        jPanel10.add(jPanel4);

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jButton_new_pipeline.setText(resourceMap.getString("jButton_new_pipeline.text")); // NOI18N
        jButton_new_pipeline.setName("jButton_new_pipeline"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jButton_new_pipeline, gridBagConstraints);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(RTIbuilder.RTIbuilderApp.class).getContext().getActionMap(PipelineMenu.class, this);
        jButton_open_pipeline.setAction(actionMap.get("loadPipeline")); // NOI18N
        jButton_open_pipeline.setText(resourceMap.getString("jButton_open_pipeline.text")); // NOI18N
        jButton_open_pipeline.setName("jButton_open_pipeline"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jButton_open_pipeline, gridBagConstraints);

        jButton_rem_pipeline.setText(resourceMap.getString("jButton_rem_pipeline.text")); // NOI18N
        jButton_rem_pipeline.setName("jButton_rem_pipeline"); // NOI18N
        jButton_rem_pipeline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_rem_pipelineActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jButton_rem_pipeline, gridBagConstraints);

        jPanel10.add(jPanel2);

        jSplitPane2.setLeftComponent(jPanel10);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 10));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setLayout(new javax.swing.BoxLayout(jPanel5, javax.swing.BoxLayout.LINE_AXIS));

        jPanel7.setName("jPanel7"); // NOI18N
        jPanel7.setLayout(new java.awt.BorderLayout());

        jLabel_available_plugins.setText(resourceMap.getString("jLabel_available_plugins.text")); // NOI18N
        jLabel_available_plugins.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jLabel_available_plugins.setName("jLabel_available_plugins"); // NOI18N
        jPanel7.add(jLabel_available_plugins, java.awt.BorderLayout.NORTH);

        jScrollPane_plugins_to_add.setName("jScrollPane_plugins_to_add"); // NOI18N

        jTable_plugins_to_add.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable_plugins_to_add.setName("jTable_plugins_to_add"); // NOI18N
        jTable_plugins_to_add.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable_plugins_to_add.setShowHorizontalLines(false);
        jTable_plugins_to_add.setShowVerticalLines(false);
        jScrollPane_plugins_to_add.setViewportView(jTable_plugins_to_add);
        jTable_plugins_to_add.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTable_plugins_to_add.columnModel.title0")); // NOI18N

        jPanel7.add(jScrollPane_plugins_to_add, java.awt.BorderLayout.CENTER);

        jPanel5.add(jPanel7);

        jPanel8.setName("jPanel8"); // NOI18N
        jPanel8.setLayout(new java.awt.GridBagLayout());

        jButton_add_plugin_to_pipe.setFont(resourceMap.getFont("jButton_rem_plugin_from_pipe.font")); // NOI18N
        jButton_add_plugin_to_pipe.setIcon(resourceMap.getIcon("jButton_add_plugin_to_pipe.icon")); // NOI18N
        jButton_add_plugin_to_pipe.setText(resourceMap.getString("jButton_add_plugin_to_pipe.text")); // NOI18N
        jButton_add_plugin_to_pipe.setBorderPainted(false);
        jButton_add_plugin_to_pipe.setMinimumSize(new java.awt.Dimension(30, 26));
        jButton_add_plugin_to_pipe.setName("jButton_add_plugin_to_pipe"); // NOI18N
        jButton_add_plugin_to_pipe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_add_plugin_to_pipeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel8.add(jButton_add_plugin_to_pipe, gridBagConstraints);

        jButton_rem_plugin_from_pipe.setFont(resourceMap.getFont("jButton_rem_plugin_from_pipe.font")); // NOI18N
        jButton_rem_plugin_from_pipe.setIcon(resourceMap.getIcon("jButton_rem_plugin_from_pipe.icon")); // NOI18N
        jButton_rem_plugin_from_pipe.setText(resourceMap.getString("jButton_rem_plugin_from_pipe.text")); // NOI18N
        jButton_rem_plugin_from_pipe.setBorderPainted(false);
        jButton_rem_plugin_from_pipe.setMinimumSize(new java.awt.Dimension(30, 26));
        jButton_rem_plugin_from_pipe.setName("jButton_rem_plugin_from_pipe"); // NOI18N
        jButton_rem_plugin_from_pipe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_rem_plugin_from_pipeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel8.add(jButton_rem_plugin_from_pipe, gridBagConstraints);

        jButton_up_selected.setIcon(resourceMap.getIcon("jButton_up_selected.icon")); // NOI18N
        jButton_up_selected.setText(resourceMap.getString("jButton_up_selected.text")); // NOI18N
        jButton_up_selected.setBorderPainted(false);
        jButton_up_selected.setMinimumSize(new java.awt.Dimension(30, 26));
        jButton_up_selected.setName("jButton_up_selected"); // NOI18N
        jButton_up_selected.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_up_selectedActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        jPanel8.add(jButton_up_selected, gridBagConstraints);

        jButton_down_selected.setIcon(resourceMap.getIcon("jButton_down_selected.icon")); // NOI18N
        jButton_down_selected.setText(resourceMap.getString("jButton_down_selected.text")); // NOI18N
        jButton_down_selected.setBorderPainted(false);
        jButton_down_selected.setMinimumSize(new java.awt.Dimension(30, 26));
        jButton_down_selected.setName("jButton_down_selected"); // NOI18N
        jButton_down_selected.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_down_selectedActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        jPanel8.add(jButton_down_selected, gridBagConstraints);

        jPanel5.add(jPanel8);

        jPanel9.setName("jPanel9"); // NOI18N
        jPanel9.setLayout(new java.awt.BorderLayout());

        jLabel_pipeline_sequence.setText(resourceMap.getString("jLabel_pipeline_sequence.text")); // NOI18N
        jLabel_pipeline_sequence.setName("jLabel_pipeline_sequence"); // NOI18N
        jPanel9.add(jLabel_pipeline_sequence, java.awt.BorderLayout.NORTH);

        jScrollPane_plugin_order.setName("jScrollPane_plugin_order"); // NOI18N

        jTable_plugin_order.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "#", "Plugin"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class
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
        jTable_plugin_order.setName("jTable_plugin_order"); // NOI18N
        jTable_plugin_order.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable_plugin_order.setShowHorizontalLines(false);
        jTable_plugin_order.setShowVerticalLines(false);
        jScrollPane_plugin_order.setViewportView(jTable_plugin_order);
        jTable_plugin_order.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTable_plugin_order.columnModel.title0")); // NOI18N
        jTable_plugin_order.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTable_plugin_order.columnModel.title1")); // NOI18N

        jPanel9.add(jScrollPane_plugin_order, java.awt.BorderLayout.CENTER);

        jPanel5.add(jPanel9);

        jPanel1.add(jPanel5);

        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setLayout(new javax.swing.BoxLayout(jPanel6, javax.swing.BoxLayout.LINE_AXIS));

        jButton_save_pipeline.setText(resourceMap.getString("jButton_save_pipeline.text")); // NOI18N
        jButton_save_pipeline.setName("jButton_save_pipeline"); // NOI18N
        jPanel6.add(jButton_save_pipeline);

        jButton_save_as_pipeline.setText(resourceMap.getString("jButton_save_as_pipeline.text")); // NOI18N
        jButton_save_as_pipeline.setName("jButton_save_as_pipeline"); // NOI18N
        jPanel6.add(jButton_save_as_pipeline);

        jPanel1.add(jPanel6);

        jSplitPane2.setRightComponent(jPanel1);

        jPanel15.add(jSplitPane2, java.awt.BorderLayout.CENTER);

        jPanel_pipeline.add(jPanel15);

        jPanel13.add(jPanel_pipeline);

        jSplitPane1.setBottomComponent(jPanel13);

        jPanel12.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 5, 20));
        jPanel12.setName("jPanel12"); // NOI18N
        jPanel12.setLayout(new javax.swing.BoxLayout(jPanel12, javax.swing.BoxLayout.LINE_AXIS));

        jPanel_plugins.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel_plugins.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel_plugins.border.titleFont"))); // NOI18N
        jPanel_plugins.setName("jPanel_plugins"); // NOI18N
        jPanel_plugins.setLayout(new javax.swing.BoxLayout(jPanel_plugins, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel14.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 5, 20));
        jPanel14.setName("jPanel14"); // NOI18N
        jPanel14.setLayout(new java.awt.BorderLayout());

        jPanel3.setName("jPanel3"); // NOI18N

        jButton_new_plugin.setAction(actionMap.get("newPlugin")); // NOI18N
        jButton_new_plugin.setText(resourceMap.getString("jButton_new_plugin.text")); // NOI18N
        jButton_new_plugin.setName("jButton_new_plugin"); // NOI18N
        jPanel3.add(jButton_new_plugin);

        jButton_open_plugin.setAction(actionMap.get("loadPlugin")); // NOI18N
        jButton_open_plugin.setText(resourceMap.getString("jButton_open_plugin.text")); // NOI18N
        jButton_open_plugin.setName("jButton_open_plugin"); // NOI18N
        jPanel3.add(jButton_open_plugin);

        jButton_remove_plugin.setText(resourceMap.getString("jButton_remove_plugin.text")); // NOI18N
        jButton_remove_plugin.setName("jButton_remove_plugin"); // NOI18N
        jButton_remove_plugin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_remove_pluginActionPerformed(evt);
            }
        });
        jPanel3.add(jButton_remove_plugin);

        jPanel14.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        jScrollPane_plugin_list.setName("jScrollPane_plugin_list"); // NOI18N
        jScrollPane_plugin_list.setPreferredSize(new java.awt.Dimension(100, 200));

        jTable_plugin_list.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Version", "Jar File", "UI File"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jTable_plugin_list.setName("jTable_plugin_list"); // NOI18N
        jTable_plugin_list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable_plugin_list.setShowHorizontalLines(false);
        jTable_plugin_list.setShowVerticalLines(false);
        jScrollPane_plugin_list.setViewportView(jTable_plugin_list);
        jTable_plugin_list.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTable_plugin_list.columnModel.title0")); // NOI18N
        jTable_plugin_list.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTable_plugin_list.columnModel.title1")); // NOI18N
        jTable_plugin_list.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("jTable_plugin_list.columnModel.title2")); // NOI18N
        jTable_plugin_list.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("jTable_plugin_list.columnModel.title3")); // NOI18N

        jPanel14.add(jScrollPane_plugin_list, java.awt.BorderLayout.CENTER);

        jPanel_plugins.add(jPanel14);

        jPanel12.add(jPanel_plugins);

        jSplitPane1.setTopComponent(jPanel12);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jPanel11.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 25, 10, 25));
        jPanel11.setName("jPanel11"); // NOI18N
        jPanel11.setLayout(new java.awt.BorderLayout());

        jPanel16.setName("jPanel16"); // NOI18N
        jPanel16.setLayout(new java.awt.BorderLayout());

        jButton_back.setText(resourceMap.getString("jButton_back.text")); // NOI18N
        jButton_back.setMargin(new java.awt.Insets(5, 2, 5, 2));
        jButton_back.setName("jButton_back"); // NOI18N
        jButton_back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_backActionPerformed(evt);
            }
        });
        jPanel16.add(jButton_back, java.awt.BorderLayout.EAST);

        jPanel11.add(jPanel16, java.awt.BorderLayout.CENTER);

        add(jPanel11, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_remove_pluginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_remove_pluginActionPerformed
		int selected = jTable_plugin_list.getSelectedRow();
		if (selected == -1) {
			out.println("none selected");
		} else {
			out.println("selected removed");
			String name = (String) ((DefaultTableModel) jTable_plugin_list.getModel()).getValueAt(selected, 0);
			((DefaultTableModel) jTable_plugin_list.getModel()).removeRow(selected);

			int i = 0, j = -1;
			for (Plugin p : plugins) {
				if (p.getPluginName().equals(name)) {
					j = i;
				}
				i++;
			}
			if (j != -1) {
				plugins.remove(j);
				out.println("selected removed 2");
			}
		}
    }//GEN-LAST:event_jButton_remove_pluginActionPerformed

    private void jButton_rem_pipelineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_rem_pipelineActionPerformed
		int selected = jTable_pipeline_list.getSelectedRow();
		if (selected == -1) {
			out.println("none selected");
		} else {
			out.println("selected removed");
			String name = (String) ((DefaultTableModel) jTable_pipeline_list.getModel()).getValueAt(selected, 0);
			((DefaultTableModel) jTable_pipeline_list.getModel()).removeRow(selected);
			int i = 0, j = -1;
			for (Pipeline p : pipelines) {
				if (p.getName().equals(name)) {
					j = i;
				}
				i++;
			}
			if (j != -1) {
				pipelines.remove(j);
				out.println("selected removed 2");
			}
		}
    }//GEN-LAST:event_jButton_rem_pipelineActionPerformed

    private void jButton_add_plugin_to_pipeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_add_plugin_to_pipeActionPerformed
		int selected = jTable_plugins_to_add.getSelectedRow();
		if (selected == -1) {
			out.println("none selected");
		} else {
			out.println("selected added");
			String name = (String) ((DefaultTableModel) jTable_plugins_to_add.getModel()).getValueAt(selected, 0);
			((DefaultTableModel) jTable_plugins_to_add.getModel()).removeRow(selected);
			for (Plugin p : plugins) {
				if (p.getPluginName().equals(name)) {
					int order = ((DefaultTableModel) jTable_plugin_order.getModel()).getRowCount() + 1;
					int selected2 = jTable_plugin_order.getSelectedRow();
					order = (selected2 == -1) ? order : selected2 + 1;
					((DefaultTableModel) jTable_plugin_order.getModel()).insertRow(order - 1,
						new Object[]{order, p.getPluginName()});

					selected_pipeline.addPlugin(order, p);
				}
			}
		}
    }//GEN-LAST:event_jButton_add_plugin_to_pipeActionPerformed

    private void jButton_rem_plugin_from_pipeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_rem_plugin_from_pipeActionPerformed
		int selected = jTable_plugin_order.getSelectedRow();
		if (selected == -1) {
			out.println("none selected");
		} else {
			out.println("selected removed");
			String name = (String) ((DefaultTableModel) jTable_plugin_order.getModel()).getValueAt(selected, 1);
			((DefaultTableModel) jTable_plugins_to_add.getModel()).insertRow(0, new Object[]{name});
			for (Plugin p : plugins) {
				if (p.getPluginName().equals(name)) {
					((DefaultTableModel) jTable_plugin_order.getModel()).removeRow(selected);
					selected_pipeline.removePlugin(p);
				}
			}
			int i, nrows = ((DefaultTableModel) jTable_plugin_order.getModel()).getRowCount();
			for (i = selected; i < nrows; i++) {
				((DefaultTableModel) jTable_plugin_order.getModel()).setValueAt(i + 1, i, 0);
			}
			if (nrows >= selected) {
				ListSelectionModel model = jTable_plugin_order.getSelectionModel();
				model.setSelectionInterval(selected, selected);
			}
		}
    }//GEN-LAST:event_jButton_rem_plugin_from_pipeActionPerformed

    private void jButton_up_selectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_up_selectedActionPerformed
		int selected = jTable_plugin_order.getSelectedRow();
		int selected2 = jTable_plugins_to_add.getSelectedRow();
		if (selected != -1) {
			if (selected != 0) {
				selected_pipeline.swapPlugins(selected, selected + 1);
				String name = (String) ((DefaultTableModel) jTable_plugin_order.getModel()).getValueAt(selected, 1);
				String name2 = (String) ((DefaultTableModel) jTable_plugin_order.getModel()).getValueAt(selected - 1, 1);
				((DefaultTableModel) jTable_plugin_order.getModel()).setValueAt(name2, selected, 1);
				((DefaultTableModel) jTable_plugin_order.getModel()).setValueAt(name, selected - 1, 1);
				((ListSelectionModel) jTable_plugin_order.getSelectionModel()).setSelectionInterval(selected - 1, selected - 1);
			}
		} else if (selected2 != -1) {
			if (selected2 != 0) {
				String name = (String) ((DefaultTableModel) jTable_plugins_to_add.getModel()).getValueAt(selected2, 0);
				String name2 = (String) ((DefaultTableModel) jTable_plugins_to_add.getModel()).getValueAt(selected2 - 1, 0);
				((DefaultTableModel) jTable_plugins_to_add.getModel()).setValueAt(name2, selected2, 0);
				((DefaultTableModel) jTable_plugins_to_add.getModel()).setValueAt(name, selected2 - 1, 0);
				((ListSelectionModel) jTable_plugins_to_add.getSelectionModel()).setSelectionInterval(selected2 - 1, selected2 - 1);
			}

		} else {
			out.println("none selected");
		}
    }//GEN-LAST:event_jButton_up_selectedActionPerformed

    private void jButton_down_selectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_down_selectedActionPerformed
		int selected = jTable_plugin_order.getSelectedRow();
		int selected2 = jTable_plugins_to_add.getSelectedRow();
		if (selected != -1) {
			int nrows = ((DefaultTableModel) jTable_plugin_order.getModel()).getRowCount();
			if (selected < nrows - 1) {
				selected_pipeline.swapPlugins(selected + 1, selected + 2);
				String name = (String) ((DefaultTableModel) jTable_plugin_order.getModel()).getValueAt(selected, 1);
				String name2 = (String) ((DefaultTableModel) jTable_plugin_order.getModel()).getValueAt(selected + 1, 1);
				((DefaultTableModel) jTable_plugin_order.getModel()).setValueAt(name2, selected, 1);
				((DefaultTableModel) jTable_plugin_order.getModel()).setValueAt(name, selected + 1, 1);
				((ListSelectionModel) jTable_plugin_order.getSelectionModel()).setSelectionInterval(selected + 1, selected + 1);
			}
		} else if (selected2 != -1) {
			int nrows = ((DefaultTableModel) jTable_plugins_to_add.getModel()).getRowCount();
			if (selected2 < nrows - 1) {
				String name = (String) ((DefaultTableModel) jTable_plugins_to_add.getModel()).getValueAt(selected2, 0);
				String name2 = (String) ((DefaultTableModel) jTable_plugins_to_add.getModel()).getValueAt(selected2 + 1, 0);
				((DefaultTableModel) jTable_plugins_to_add.getModel()).setValueAt(name2, selected2, 0);
				((DefaultTableModel) jTable_plugins_to_add.getModel()).setValueAt(name, selected2 + 1, 0);
				((ListSelectionModel) jTable_plugins_to_add.getSelectionModel()).setSelectionInterval(selected2 + 1, selected2 + 1);
			}

		} else {
			out.println("none selected");
		}
    }//GEN-LAST:event_jButton_down_selectedActionPerformed

    private void jButton_backActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_backActionPerformed
		parent.initialMenu.resetTablePipeline();
		CardLayout cl = (CardLayout) parent.getMainPanel().getLayout();
		cl.show(parent.getMainPanel(), InitialMenu.PANEL_NAME);
    }//GEN-LAST:event_jButton_backActionPerformed

	@Action
	public Task newPlugin() {
		return new NewPluginTask(org.jdesktop.application.Application.getInstance(RTIbuilder.RTIbuilderApp.class));
	}

	private class NewPluginTask extends org.jdesktop.application.Task<Object, Void> {

		NewPluginTask(org.jdesktop.application.Application app) {
			super(app);
			out.print("NEWWWWWWWWWWWWW");
			jDialog_new_plugin.setVisible(true);

		}

		@Override
		protected Object doInBackground() {


			return null;  // return your result
		}

		@Override
		protected void succeeded(Object result) {
			// Runs on the EDT.  Update the GUI based on
			// the result computed by doInBackground().
		}
	}

	@Action
	public Task loadPlugin() {
		return new LoadPluginTask(org.jdesktop.application.Application.getInstance(RTIbuilder.RTIbuilderApp.class));
	}

	private class LoadPluginTask extends org.jdesktop.application.Task<Object, Void> {

		/**Variable that reports the process cancelation by the user or an error.*/
		boolean canceled = false;

		LoadPluginTask(org.jdesktop.application.Application app) {
			// Runs on the EDT.  Copy GUI state that
			// doInBackground() depends on from parameters
			// to LoadPluginTask fields, here.
			super(app);
		}

		@Override
		protected Object doInBackground() {

			File selectedFile;
			//File chooser for selecting the lp file or image folder.
			JFileChooser jfc = new JFileChooser("Select LP file");
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int result = jfc.showOpenDialog(new JFrame());
			Plugin p = null;
			if (result == JFileChooser.APPROVE_OPTION) {
				if (jfc.getSelectedFile().isFile()) {
					selectedFile = jfc.getSelectedFile();
					p = PluginParser.getPlugin(selectedFile.getAbsolutePath());
					addNewPlugin(p);
				} else {
					System.err.println("Is not a file.");
					this.setMessage("Is not a file.");
				}
			} else if (result == JFileChooser.CANCEL_OPTION) {
				canceled = true;
			}

			return p;  // return your result
		}

		@Override
		protected void succeeded(Object p) {
			if (canceled) {
				return;
			}
			if (p != null) {
				Plugin p2 = (Plugin) p;
				plugins.add(p2);
				UserInteractionInterface gui = p2.getUser_interface();
				parent.getPluginGUI().add(gui, p2.getPluginName());
				parent.getPluginGUI().validate();
			}
			this.setMessage("Plugin loaded.");
		}
	}
//
//    @Action
//    public Task beginPipeline() {
//        return new BeginPipelineTask(org.jdesktop.application.Application.getInstance(lptracker.LPtrackerApp.class));
//    }
//
//    private class BeginPipelineTask extends org.jdesktop.application.Task<Object, Void> {
//
//        BeginPipelineTask(org.jdesktop.application.Application app) {
//            super(app);
//        }
//
//        @Override
//        protected Object doInBackground() {
//
//            int selected = jTable_pipeline_list.getSelectedRow();
//            if (selected == -1) {
//                //criar popup
//                out.println("No pipeline selected");
//            } else {
//                String name = (String) ((DefaultTableModel) jTable_pipeline_list.getModel()).getValueAt(selected, 0);
//                for (Pipeline p : pipelines) {
//                    if (p.getName().equals(name)) {
//                        parent.pipeline_in_use = p;
//                        this.setMessage("Pipeline selected: " + name);
//                    }
//                }
//            }
//
//
//            return selected;  // return your result
//        }
//
//        @Override
//        protected void succeeded(Object result) {
//            if ((Integer) result == -1) {
//                return;
//            }
//
//            CardLayout cl = (CardLayout) parent.getMainPanel().getLayout();
//            cl.show(parent.getMainPanel(), PreviewMenu.PANEL_NAME);
//
//            parent.previewMenu.initPreviewMenu();
//        }
//    }

	@Action
	public Task loadPipeline() {
		return new LoadPipelineTask(org.jdesktop.application.Application.getInstance(RTIbuilder.RTIbuilderApp.class));
	}

	private class LoadPipelineTask extends org.jdesktop.application.Task<Object, Void> {

		/**Variable that reports the process cancelation by the user or an error.*/
		boolean canceled = false;

		LoadPipelineTask(org.jdesktop.application.Application app) {
			// Runs on the EDT.  Copy GUI state that
			// doInBackground() depends on from parameters
			// to LoadPipelineTask fields, here.
			super(app);
		}

		@Override
		protected Object doInBackground() {

			File selectedFile;
			//File chooser for selecting the lp file or image folder.
			JFileChooser jfc = new JFileChooser("Select LP file");
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int result = jfc.showOpenDialog(new JFrame());
			Pipeline p = null;
			if (result == JFileChooser.APPROVE_OPTION) {
				if (jfc.getSelectedFile().isFile()) {
					selectedFile = jfc.getSelectedFile();
					p = PipelineParser.getPipeline(selectedFile.getAbsolutePath());
					p.loadPlugins(plugins);
					addNewPipeline(p);
				} else {
					System.err.println("Is not a file.");
					this.setMessage("Is not a file.");
				}
			} else if (result == JFileChooser.CANCEL_OPTION) {
				canceled = true;
			}

			return p;  // return your result
		}

		@Override
		protected void succeeded(Object p) {
			if (canceled) {
				return;
			}
			if (p != null) {
				pipelines.add((Pipeline) p);
			}
			this.setMessage("Plugin loaded.");
		}
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton_add_plugin_to_pipe;
    private javax.swing.JButton jButton_back;
    private javax.swing.JButton jButton_down_selected;
    private javax.swing.JButton jButton_new_pipeline;
    private javax.swing.JButton jButton_new_plugin;
    private javax.swing.JButton jButton_open_pipeline;
    private javax.swing.JButton jButton_open_plugin;
    private javax.swing.JButton jButton_rem_pipeline;
    private javax.swing.JButton jButton_rem_plugin_from_pipe;
    private javax.swing.JButton jButton_remove_plugin;
    private javax.swing.JButton jButton_save_as_pipeline;
    private javax.swing.JButton jButton_save_pipeline;
    private javax.swing.JButton jButton_up_selected;
    private javax.swing.JDialog jDialog_new_plugin;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel_available_pipelines;
    private javax.swing.JLabel jLabel_available_plugins;
    private javax.swing.JLabel jLabel_pipeline_sequence;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanel_new_plugin;
    private javax.swing.JPanel jPanel_pipeline;
    private javax.swing.JPanel jPanel_plugins;
    private javax.swing.JScrollPane jScrollPane_pipeline_list;
    private javax.swing.JScrollPane jScrollPane_plugin_list;
    private javax.swing.JScrollPane jScrollPane_plugin_order;
    private javax.swing.JScrollPane jScrollPane_plugins_to_add;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTable jTable_pipeline_list;
    private javax.swing.JTable jTable_plugin_list;
    private javax.swing.JTable jTable_plugin_order;
    private javax.swing.JTable jTable_plugins_to_add;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables
}
