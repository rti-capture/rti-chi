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
import LPCompute.LPComputeMain;
import Plugin.helpers.ImageProcessing;
import Plugin.helpers.PTMFitterThread;
import Plugin.helpers.XMLPluginParser;
import XMLcarrier.Event;
import XMLcarrier.FileGroup;
import XMLcarrier.HeaderInfo;
import XMLcarrier.Info;
import XMLcarrier.RawInfo;
import XMLcarrier.XMLHandler;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import guicomponents.AreaSelectionPanel;
import guicomponents.AreaSelectionPanelRefined;
import guicomponents.exceptions.AreaNotDefined;
import java.awt.Component;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import operations.DefaultProjectImagesOpenTask;
import operations.OpenDataTask;

/**
 *
 * @author matheus
 */
public class PluginPanel extends javax.swing.JPanel {

    /**
     * Represent the absolute path to the xml.
     *
     */
    StringBuffer xmlPath;

    /**
     * Represents the name in the XML that contains all spheres and its positions.
     *
     */
    private final String SPHERES_TAG = "Spheres";

    /**
     * Number of spheres;
     *
     */
    private short NUMBER_OF_SPHERES;
    private ArrayList<Sphere> sphereList;

    private int cropWidth;
    private int cropHeight;

    /**
     * This inner class is a information container.
     * It represents a sphere (X,Y,Radix).
     *
     */

    public class Sphere
    // <editor-fold defaultstate="collapsed" desc="Sphere Inner Class">
    {

        private float x, y, r;
        private UUID uuid;


        public Sphere(float x, float y, float r, UUID uuid)
        // <editor-fold defaultstate="collapsed" desc="Constructor">
        {
            this.x = x;
            this.y = y;
            this.r = r;
            this.uuid = uuid;
        }// </editor-fold>

        public float getR()
        // <editor-fold defaultstate="collapsed" desc="Code">
        {
            return r;
        }// </editor-fold>

        public void setR(float r)
        // <editor-fold defaultstate="collapsed" desc="Code">
        {
            this.r = r;
        }// </editor-fold>

        public float getX()
        // <editor-fold defaultstate="collapsed" desc="Code">
        {
            return x;
        }// </editor-fold>

        public void setX(float x)
        // <editor-fold defaultstate="collapsed" desc="Code">
        {
            this.x = x;
        }// </editor-fold>

        public float getY()
        // <editor-fold defaultstate="collapsed" desc="Code">
        {
            return y;
        }// </editor-fold>

        public void setY(float y)
        // <editor-fold defaultstate="collapsed" desc="Code">
        {
            this.y = y;
        }// </editor-fold>

        public UUID getUuid()
        // <editor-fold defaultstate="collapsed" desc="Code">
        {
            return uuid;
        }// </editor-fold>

        public void setUuid(UUID uuid)
        // <editor-fold defaultstate="collapsed" desc="Code">
        {
            this.uuid = uuid;
        }// </editor-fold>
    }// </editor-fold>

    AreaSelectionPanel AREA_SELECTION_PANEL;
    AreaSelectionPanelRefined SELECTION;
//    ImageCropPanel IMG_CROP_PANEL;
    /**Execution flag*/
    private boolean executed;

    private String FITTER_ABSOLUTE_PATH;

    /**
     * WIDTH is the width size written in the XML carrier.
     *
     */
    private int WIDTH;
    private int RESIZED_WIDTH;

    /**
     * HEIGHT is the height size written in the XML carrier.
     *
     */
    private int HEIGHT;
    private int RESIZED_HEIGHT;

    private float PROPORTION;

    private String OUTPUT;

    private String PROJECT_NAME;

    /**
     * 1- LRGB
     * 2- RGB
     */
    private short PTM_TYPE_DEFAULT = 1;

    ButtonGroup groupType;

    BufferedImage bf;

    boolean isCropped = false;
    boolean alreadyCropped = false;

    private String CROP_PATH;

    private String PROJECT_PATH;

    UUID imgGtLuminance;

    ArrayList<Point> cropPoints;

    ArrayList<Point> lastCrop;

    boolean auxMousePressed = false;
    boolean definedArea = false;

    private FileGroup fg = null;

    iDataCache cache = null;
    UUID lastCache = null;

    /** Creates new form PluginPanel */
    public PluginPanel() {
        initComponents();
        System.out.println("new PluginPanel");
        this.sphereList = new ArrayList<Sphere>();

        this.cropPoints = new ArrayList<Point>();

        this.lastCrop = new ArrayList<Point>();

        executed = false;
        cache = null;
        lastCache = UUID.randomUUID();
        //Initalize preview and area selection panel
        AREA_SELECTION_PANEL = new AreaSelectionPanel();
        SELECTION = new AreaSelectionPanelRefined();

        AREA_SELECTION_PANEL.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                try {
                    int selected = AREA_SELECTION_PANEL.getSelectedArea();
                    comboSpheres.setSelectedIndex(selected);
                } catch (Exception e) {
                    System.out.println("Exception catched");
                    e.printStackTrace();
                }

            }
        });

        groupType = new ButtonGroup();
        groupType.add(this.radioBLRGB);
        groupType.add(this.radioBRGB);

        this.jPanel1.add(AREA_SELECTION_PANEL, java.awt.BorderLayout.CENTER);
        //this.jPanel1.add(SELECTION, java.awt.BorderLayout.CENTER);

        this.labelNewHeight.setVisible(false);
        this.labelNewWidth.setVisible(false);
        this.textNewHeight.setVisible(false);
        this.textNewWidth.setVisible(false);
        this.labelPx1.setVisible(false);
        this.labelPx2.setVisible(false);

    }

    @SuppressWarnings("static-access")
    public void start(StringBuffer xmlPath) {
        this.xmlPath = xmlPath;
        boolean executeLP = true;
        try {
            XMLHandler xml = new XMLHandler(xmlPath.toString());
            xml.loadXML();

            HeaderInfo hi = xml.getProjectInfo();
            TreeMap<String, String> info = hi.getMap();

            this.WIDTH = (Float.valueOf(info.get("Image width"))).intValue();
            this.HEIGHT = (Float.valueOf(info.get("Image height"))).intValue();

            this.cropWidth = 0;
            this.cropHeight = 0;

            this.CROP_PATH = hi.getCroppedDir();

            if (this.CROP_PATH == null || this.CROP_PATH.equals("")) {
                this.CROP_PATH = "CROPPED";
            }

            this.PROPORTION = (float) (this.WIDTH * 1.0f / this.HEIGHT);
            System.out.println("Prop : " + this.PROPORTION);

            this.originalHeight.setText(String.valueOf(this.HEIGHT) + " (px)");
            this.originalWidth.setText(String.valueOf(this.WIDTH) + " (px)");

            this.OUTPUT = hi.getProjectName() + "_" + this.WIDTH + ".ptm";
            this.PROJECT_NAME = hi.getProjectName();
            this.textOutputFileName.setText(this.OUTPUT);

            RawInfo ri = xml.getComputedInfo(SPHERES_TAG);

            this.NUMBER_OF_SPHERES = (short) ri.getAllInnerInformation().size();
            ArrayList<Info> spheres = ri.getAllInnerInformation();

            this.PROJECT_PATH = hi.getParamterByName("ProjectPath");

            File auxP = new File(this.PROJECT_PATH);
            if (!auxP.isDirectory()) {
                this.PROJECT_PATH = auxP.getParent() + File.separator;
            }

            ArrayList<FileGroup> fgl = xml.getAllFileGroups();

            // Get the relevant file group
            for(FileGroup fgi : fgl)
            {
                if (fgi.getUse().equalsIgnoreCase("Original image files"))
                {
                    this.fg = fgi;
                }
            }

            switch (this.PTM_TYPE_DEFAULT) {
                case 1:
                    this.radioBLRGB.setSelected(true);
                    break;
                case 2:
                    this.radioBRGB.setSelected(true);
                    break;
            }

            //Reset interface
            resetInterface();

            SELECTION.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {

                @Override
                public void mouseDragged(MouseEvent e) {

                    cbResize.setSelected(false);

                    ArrayList<Point> pointList = null;
                    try {
                        //if(SELECTION.draw_new_areas){
                        pointList = SELECTION.getCropPoints();

                        if(pointList.size() > 2){
                            Polygon CROPPOLYGON = new Polygon();

                            for (Point p : pointList) {
                                CROPPOLYGON.addPoint(p.x, p.y);
                            }

                            Rectangle r = CROPPOLYGON.getBounds();
                            originalWidth.setText(r.width + " (px)");
                            originalHeight.setText(r.height + " (px)");

                            cropWidth = r.width;
                            cropHeight = r.height;

                            textOutputFileName.setText(PROJECT_NAME + "_cropped_" + cropWidth + ".ptm");
                        }
                    } catch (AreaNotDefined ex) {
                        Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }


                }
            });


            SELECTION.addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent e) {
                    System.out.println("Mouse Released");
                    if (checkEditImage.isSelected()) {
                        if (!SELECTION.origin_point.equals(SELECTION.end_point) &&
                                SELECTION.getCropStyle() == SELECTION.RECTANGULAR_CROP) {
                            if (!definedArea) {
                                SELECTION.confirmSelectionArea();
                                definedArea = true;
                            } else {
                                try {
                                    ArrayList<Point> cropPoints = SELECTION.getCropPoints();
                                    SELECTION.cleanCropArea();
                                    SELECTION.repaint();
                                    SELECTION.setCropPoints(cropPoints);
                                    //SELECTION.addToSelectedAreas(cropPoints);
                                    SELECTION.confirmSelectionArea();
                                } catch (AreaNotDefined ex) {
                                    Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            }
                        }else if(SELECTION.getCropStyle() == SELECTION.FREE_CROP){
                            
                            Point[] pts = SELECTION.getBoundsFromPoints();
                            
                            int cropWidth = Math.abs(pts[1].x - pts[0].x);
                            int cropHeight = Math.abs(pts[1].y - pts[0].y);

                            // Sanity check
                            if (cropWidth != 0 && cropHeight != 0)
                            {
                                originalWidth.setText(cropWidth + " (px)");
                                originalHeight.setText(cropHeight + " (px)");
                            }
                            
                            /*ArrayList<Point> pointList = null;
                            try {
                            pointList = SELECTION.getCropPoints();
                            
                            Polygon CROPPOLYGON = new Polygon();
                            
                            for (Point p : pointList) {
                            CROPPOLYGON.addPoint(p.x, p.y);
                            }
                            
                            Rectangle r = CROPPOLYGON.getBounds();
                            
                            originalWidth.setText(r.width + " (px)");
                            originalHeight.setText(r.height + " (px)");
                            
                            cropWidth = r.width;
                            cropHeight = r.height;
                            
                            textOutputFileName.setText(PROJECT_NAME + "_cropped_" + cropWidth + ".ptm");
                            } catch (AreaNotDefined ex) {
                            Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
                            }*/



                        }
                    }

                }

            });

            // Handle Selection Panels' image
            handleSelectionPanelImage();

            /*
            this.AREA_SELECTION_PANEL.setImage(bf);
            this.AREA_SELECTION_PANEL.repaint();

            this.SELECTION.setImage(bf);
            this.SELECTION.setCropStyle(SELECTION.RECTANGULAR_CROP);
            this.SELECTION.repaint();*/

            int cont = 1;
            this.sphereList.clear();
            this.comboSpheres.removeAllItems();

            System.out.println("Number of spheres : " + this.NUMBER_OF_SPHERES);

            // Disable Spheres ... it's NOT necessary nor desirable
            //this.comboSpheres.setEnabled(false);


            
            if (this.NUMBER_OF_SPHERES == 0) {
                // It means a .lp file was read instead of normal pipeline process.
                this.comboSpheres.setEnabled(false);
            } else {
                for (short i = 0; i < this.NUMBER_OF_SPHERES; i++) {
                    Sphere s = new Sphere(Float.valueOf(spheres.get(i).getAttribute("x")), Float.valueOf(spheres.get(i).getAttribute("y")),
                            Float.valueOf(spheres.get(i).getAttribute("r")), UUID.fromString(spheres.get(i).getAttribute("ID")));
                    this.sphereList.add(s);

                    Rectangle r = new Rectangle(new Float(s.getX() - s.getR()).intValue(), new Float(s.getY() - s.getR()).intValue(),
                    new Float(s.getR() * 2).intValue(), new Float(s.getR() * 2).intValue());
                    this.AREA_SELECTION_PANEL.setSelectionArea(r);
                    this.AREA_SELECTION_PANEL.setSelected(0);
                    this.comboSpheres.addItem("Sphere " + cont);
                    cont++;
                }
            }


            this.AREA_SELECTION_PANEL.enableDraw(false);
            this.AREA_SELECTION_PANEL.repaint();

            SELECTION.enableDraw(false);

            Event ev = new Event();
            Event ev2 = new Event();
            try {
                XMLPluginParser parser = new XMLPluginParser("Plugins/PluginPTMfitter.xml");
                String fitterLoc = parser.getFitterPath();
                String cropPath = parser.getCropFolder();

                if (fitterLoc.equals("")) {
                    JOptionPane.showMessageDialog(null, "Unable to automatically locate the PTMfitter Path!");
                    ev.setLevel(Level.WARNING.toString());
                    ev.setText("Unable to automatically locate the PTMfitter Path!");
                    xml.registEvent(ev);

                } else {
                    this.textHSHfitterLocation.setText(new File(parser.getFitterPath()).getAbsolutePath());
                    this.FITTER_ABSOLUTE_PATH = this.textHSHfitterLocation.getText();
                    ev.setText("PTMfitter Path found : " + this.textHSHfitterLocation.getText());
                    xml.registEvent(ev);
                }

                if (cropPath.equals("")) {
                    ev2.setLevel(Level.WARNING.toString());
                    ev2.setText("Unable to automatically locate the Crop folder definition!");
                    xml.registEvent(ev2);
                    //this.CROP_PATH = "CROPPED";

                } else {
                    ev2.setLevel(Level.WARNING.toString());
                    ev2.setText("Crop path found! Using : " + cropPath);
                    xml.registEvent(ev2);
                    //this.CROP_PATH = cropPath;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error retrieving PluginPTMfitter properties!");
                ev.setLevel(Level.SEVERE.toString());
                ev.setText("Error retrieving PluginPTMfitter properties!");
                xml.registEvent(ev);
            }

            // Make sure we execute the Light Direction calculations... but ONLY once.
            RawInfo rilp = xml.getComputedInfo("LightDirections");

            if ((rilp.getAttribute("SphereID") == null) || (rilp.getAttribute("SphereID") != null && rilp.getAttribute("SphereID").isEmpty())) {
                //Means we dont have any lightDirections and we need to calculate it later.
                executeLP = true;
            } else {
                executeLP = false;
            }

            xml.writeXML();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (executeLP)
        {
            LPComputeMain lpcm = new LPComputeMain(this.xmlPath.toString());
            lpcm.computeLP();
        }

        this.comboCrop.removeAllItems();
        this.comboCrop.addItem("Rectangular");
        this.comboCrop.addItem("Free");
        this.comboCrop.setEnabled(false);

    }

    public void handleSelectionPanelImage()
    {

        final PluginPanel parentWindow = this;
        final iDataCache pluginCache = cache;

        boolean useCached = false;

        TreeMap<UUID,BufferedImage> thumbs = null;

        // Determine whether to use cache...
        if (this.cache != null)
        {
            System.out.println("PTMFitterPlugin: Cache exists! ID:"+cache.getID());
            thumbs = (TreeMap<UUID,BufferedImage>)cache.get("Cached Original Thumbs");

            if (thumbs != null)
                useCached = true;
        }

        if (thumbs == null)
            thumbs = new TreeMap();

        OpenDataTask tasker = new OpenDataTask(new DefaultProjectImagesOpenTask(true, useCached, parentWindow.PROJECT_PATH + File.separator) {

            PluginPanel parentW = null;

            {
                parentW = parentWindow;
            }

            @Override
            public void taskOnDone() {
                synchronized (this) {
                    TreeMap<UUID,BufferedImage> outputdata = this.getData();
                    // Initialize thumbnail panel as a callback...
                    {
                        parentW.getDataCache().remove("Cached Original Thumbs");
                        parentW.getDataCache().put("Cached Original Thumbs", outputdata);
                    }
                    // Now let's find out the image to set...

                    Iterator<UUID> i = outputdata.keySet().iterator();
                    int max = 0;
                    UUID uuid = null;
                    while (i.hasNext()) {
                        UUID auxUUID = i.next();
                        int aux = ImageProcessing.calculateLuminance(outputdata.get(auxUUID));
                        //System.out.println("Luminance : " + aux);
                        //System.out.println("Aux : " + aux + " Max : " + max);

                        if (aux > max) {
                            uuid = auxUUID;
                            max = aux;
                        }
                    }

                    imgGtLuminance = uuid;
                    System.out.println("IMGGTLUMINANCE  : " + uuid.toString());

                    try {
                        XMLHandler xml = new XMLHandler(xmlPath.toString());
                        xml.loadXML();
                        bf = ImageProcessing.LoadImage(this.relativePathModifier+xml.getImageByUUID(uuid).getUrl());
                        //AREA_SELECTION_PANEL.setImage(bf);
                        //SELECTION.setImage(bf);

                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, e.getMessage());
                    }
                    useSetPanelImage();
                }
            }
        }, fg, thumbs, fg.getList().size(), "Loading project images...", "Image being loaded: ");

        tasker.execute();

    }

    public void useSetPanelImage()
    {
        if (bf!=null)
        {
            this.AREA_SELECTION_PANEL.setImage(bf);
            this.AREA_SELECTION_PANEL.repaint();

            this.SELECTION.setImage(bf);
            this.SELECTION.setCropStyle(SELECTION.RECTANGULAR_CROP);
            this.SELECTION.repaint();
        }
    }


    private short getPTMType() {
        if (this.radioBLRGB.isSelected()) {
            return (short) 1;
        } else {
            return (short) 2;
        }
    }

    private void resetInterface() {

        AREA_SELECTION_PANEL.reset();
        AREA_SELECTION_PANEL.repaint();

        SELECTION.reset();
        SELECTION.repaint();

    }

    private UUID getSelectedSphereID() {
        if (this.comboSpheres.getItemCount() == 0) {
            return null;
        } else {
            int a = this.comboSpheres.getSelectedIndex();
            return this.sphereList.get(a).getUuid();
        }
    }

    private boolean isFreeCrop() {
        int index = this.comboCrop.getSelectedIndex();
        if (index == 1) {
            return true;
        } else {
            return false;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonExecute = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        textOutputFileName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        originalHeight = new javax.swing.JLabel();
        originalWidth = new javax.swing.JLabel();
        cbResize = new javax.swing.JCheckBox();
        labelNewHeight = new javax.swing.JLabel();
        labelNewWidth = new javax.swing.JLabel();
        textNewHeight = new javax.swing.JTextField();
        textNewWidth = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        textHSHfitterLocation = new javax.swing.JTextField();
        buttonFindHSHloc = new javax.swing.JButton();
        labelPx2 = new javax.swing.JLabel();
        labelPx1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        radioBLRGB = new javax.swing.JRadioButton();
        radioBRGB = new javax.swing.JRadioButton();
        comboSpheres = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        checkEditImage = new javax.swing.JCheckBox();
        comboCrop = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        buttonExecute.setText("Execute");
        buttonExecute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonExecuteActionPerformed(evt);
            }
        });

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Data"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Output File Name : ");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 260, -1, -1));
        jPanel2.add(textOutputFileName, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 260, 280, -1));

        jLabel4.setText("Actual Size : ");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, -1));

        jLabel5.setText("Width : ");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 110, -1, -1));

        jLabel6.setText("Height :");
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 130, -1, -1));

        originalHeight.setText("yy");
        jPanel2.add(originalHeight, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 130, -1, -1));

        originalWidth.setText("xx");
        jPanel2.add(originalWidth, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 110, -1, -1));

        cbResize.setText("Resize");
        cbResize.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                cbResizeStateChanged(evt);
            }
        });
        cbResize.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbResizeItemStateChanged(evt);
            }
        });
        jPanel2.add(cbResize, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 120, -1, -1));

        labelNewHeight.setText("New Height : ");
        jPanel2.add(labelNewHeight, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 130, -1, -1));

        labelNewWidth.setText("New Width : ");
        jPanel2.add(labelNewWidth, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 110, -1, -1));

        textNewHeight.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textNewHeightKeyReleased(evt);
            }
        });
        jPanel2.add(textNewHeight, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 130, 80, 20));

        textNewWidth.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textNewWidthKeyReleased(evt);
            }
        });
        jPanel2.add(textNewWidth, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 110, 80, 20));

        jLabel9.setText("PTMFitter Location : ");
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, -1));

        textHSHfitterLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textHSHfitterLocationActionPerformed(evt);
            }
        });
        jPanel2.add(textHSHfitterLocation, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 30, 190, -1));

        buttonFindHSHloc.setText("Find");
        buttonFindHSHloc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonFindHSHlocActionPerformed(evt);
            }
        });
        jPanel2.add(buttonFindHSHloc, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 30, -1, -1));

        labelPx2.setText("(px)");
        jPanel2.add(labelPx2, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 130, -1, -1));

        labelPx1.setText("(px)");
        jPanel2.add(labelPx1, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 110, -1, -1));

        jLabel2.setText("PTM Type : ");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, -1, -1));

        radioBLRGB.setText("LRGB");
        jPanel2.add(radioBLRGB, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 200, -1, -1));

        radioBRGB.setText("RGB");
        jPanel2.add(radioBRGB, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 220, -1, -1));

        comboSpheres.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboSpheresActionPerformed(evt);
            }
        });
        jPanel2.add(comboSpheres, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 310, -1, -1));

        jLabel3.setText("Select sphere : ");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 310, -1, -1));

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("PTMfitter Output Information"));
        jPanel3.setToolTipText("Fitter Output Information");
        jPanel3.setName(""); // NOI18N
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jPanel3.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 260, 440));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Crop Properties"));

        checkEditImage.setText("Use Crop");
        checkEditImage.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkEditImageItemStateChanged(evt);
            }
        });
        checkEditImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkEditImageActionPerformed(evt);
            }
        });

        comboCrop.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comboCropItemStateChanged(evt);
            }
        });

        jLabel7.setText("Crop style : ");

        jButton1.setText("Clear Crop");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(checkEditImage)
                        .addContainerGap(275, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(31, 31, 31))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboCrop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(253, Short.MAX_VALUE))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(checkEditImage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(comboCrop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(72, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(88, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                        .addGap(33, 33, 33)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 457, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 618, Short.MAX_VALUE)
                        .addComponent(buttonExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
                        .addGap(18, 18, 18)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(buttonExecute, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(14, 14, 14))
        );

        jPanel3.getAccessibleContext().setAccessibleName("Fitter Output Information");
    }// </editor-fold>//GEN-END:initComponents

    private void buttonExecuteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonExecuteActionPerformed
        // TODO add your handling code here:
        this.jTextArea1.setText("Initializing the process\n");

        PTMFitterThread th = new PTMFitterThread(this.xmlPath.toString(), this.FITTER_ABSOLUTE_PATH, "", this.getPTMType(),
                this.textOutputFileName.getText(), this.getSelectedSphereID());

        th.setButtonExecute(this.buttonExecute);
        th.setTextArea(this.jTextArea1);

        if (cbResize.isSelected()) {
            try {
                RESIZED_HEIGHT = Integer.valueOf(this.textNewHeight.getText());
                RESIZED_WIDTH = Integer.valueOf(this.textNewWidth.getText());
            }
            catch (NumberFormatException nfe)
            {
                // BAAAAAAD!
                JOptionPane.showMessageDialog(null, "Problem with resize dimensions! Verify new WIDTH and HEIGHT to continue");
                return;
            }
            if ((RESIZED_HEIGHT > 0) && (RESIZED_HEIGHT <= HEIGHT) && (RESIZED_WIDTH > 0) && (RESIZED_WIDTH <= WIDTH)) {
                System.out.println(RESIZED_WIDTH + " " + RESIZED_HEIGHT);
                th.setImagesSize(RESIZED_WIDTH, RESIZED_HEIGHT);
            } else {
                JOptionPane.showMessageDialog(null, "Problem with resize dimensions! Verify new WIDTH and HEIGHT to continue");
                return;
            }
        }

        if (checkEditImage.isSelected() && !this.isFreeCrop()) {
            if ((cropHeight > 0) && (cropHeight <= HEIGHT) && (cropWidth > 0) && (cropWidth <= WIDTH)) {
                //Looks ok to crop
                th.cropUsed(true);
                th.setCropStyle(this.isFreeCrop());
                th.setCropHeight(this.cropHeight);
                th.setCropWidth(this.cropWidth);
                
                try {
                    th.setCropPoints(this.SELECTION.getCropPoints());
                } catch (AreaNotDefined ex) {
                    Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
                }

                //TODO USE REFINED AREA SELECTION PANEL
                /*try {
                th.setCropPoints(IMG_CROP_PANEL.getCropPoints());
                } catch (AreaNotDefined ex) {
                Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
                }*/

            } else {
                JOptionPane.showMessageDialog(null, "Problem with crop dimensions! Verify the crop area to continue");
                return;
            }
        } else if (checkEditImage.isSelected() && this.isFreeCrop()) {
            try {
                if (this.SELECTION.getCropPoints().size() >= 3) {
                    // Looks ok to crop
                    Point[] p = this.SELECTION.getBoundsFromPoints(cropPoints); //SELECTION

                    cropHeight = p[1].y - p[0].y;
                    cropWidth = p[1].x - p[0].x;

                    th.cropUsed(true);
                    th.setCropStyle(this.isFreeCrop());
                    th.setCropHeight(this.cropHeight);
                    th.setCropWidth(this.cropWidth);

                    try {
                        th.setCropPoints(this.SELECTION.getCropPoints());
                    } catch (AreaNotDefined ex) {
                        Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Problem with crop dimensions! Verify the crop area to continue");
                    return;
                }
            } catch (AreaNotDefined ex) {
                Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            th.cropUsed(false);
        }


        File f = new File(this.FITTER_ABSOLUTE_PATH);
        if ((f.isFile() && f.canExecute())) {

            Thread t = new Thread(th);
            t.start();
            this.buttonExecute.setEnabled(false);

        } else {
            JOptionPane.showMessageDialog(null, "Verify your PTMfitter path!");
        }


}//GEN-LAST:event_buttonExecuteActionPerformed

    private void buttonFindHSHlocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonFindHSHlocActionPerformed
        // TODO add your handling code here:
        JFileChooser fitterChooser = new JFileChooser();
        int returnVal = fitterChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.textHSHfitterLocation.setText(fitterChooser.getSelectedFile().getAbsolutePath());
            this.FITTER_ABSOLUTE_PATH = fitterChooser.getSelectedFile().getAbsolutePath();
            XMLPluginParser xmlaux;
            try {
                xmlaux = new XMLPluginParser("Plugins/PluginPTMfitter.xml");
                xmlaux.setFitterPath(this.FITTER_ABSOLUTE_PATH.toString());
            } catch (Exception ex) {
                Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Event e = new Event();
        e.setLevel(Level.INFO.toString());
        e.setText("PTMfitter path changed to : " + this.textHSHfitterLocation.getText());
        try {
            XMLHandler xml = new XMLHandler(this.xmlPath.toString());
            xml.loadXML();
            xml.registEvent(e);

            xml.writeXML();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }//GEN-LAST:event_buttonFindHSHlocActionPerformed

    private void textHSHfitterLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textHSHfitterLocationActionPerformed
        // TODO add your handling code here:
        this.FITTER_ABSOLUTE_PATH = this.textHSHfitterLocation.getText();
        XMLPluginParser xmlaux;
        try {
            xmlaux = new XMLPluginParser("Plugins/PluginPTMfitter.xml");
            xmlaux.setFitterPath(this.FITTER_ABSOLUTE_PATH.toString());
        } catch (Exception ex) {
            Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_textHSHfitterLocationActionPerformed

    private void cbResizeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_cbResizeStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_cbResizeStateChanged

    private void comboSpheresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboSpheresActionPerformed
        // TODO add your handling code here:
        int selectedIndex = this.comboSpheres.getSelectedIndex();
        this.AREA_SELECTION_PANEL.setSelected(selectedIndex);
    }//GEN-LAST:event_comboSpheresActionPerformed

    private void textNewWidthKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textNewWidthKeyReleased
        // TODO add your handling code here:
        System.out.println("Entrou textNewWidthActionPerformed");
        int new_width = 0;
        int new_height = 0;
        boolean success = true;


        if (this.textNewWidth.getText().equals("") && this.textNewHeight.getText().equals("")) {
            return;
        }

        if (!this.textNewWidth.getText().equals("")) {
            try {
                new_width = Integer.valueOf(this.textNewWidth.getText());
            } catch (Exception e) {
                success = false;
                String s = this.textNewWidth.getText();
                this.textNewWidth.setText(s.substring(0, s.length() - 1));
                //Toolkit.getDefaultToolkit().beep();
                //JOptionPane.showMessageDialog(null, "Invalid Character");

            }
        }

        //if(this.textNewHeight.getText().equals("")){
        if (!checkEditImage.isSelected()) {
            if (new_width >= this.WIDTH) {
                //new_height = (int)this.PROPORTION*new_width;
                new_height = this.HEIGHT;
                new_width = this.WIDTH;
            } else {


                new_height = (int) (new_width / this.PROPORTION);
                if (new_height > this.HEIGHT) {
                    new_height = this.HEIGHT;
                }
            }
        } else {
            if ((cropHeight == 0) && (cropWidth == 0)) {
                JOptionPane.showMessageDialog(null, "Crop area not selected!");
                this.textNewHeight.setText("");
                this.textNewWidth.setText("");
                return;
            } else {
                if (new_width >= this.cropWidth) {
                    //new_height = (int)this.PROPORTION*new_width;
                    new_height = this.cropHeight;
                    new_width = this.cropWidth;
                } else {
                    new_height = (int) (new_width / this.PROPORTION);
                    if (new_height > this.cropHeight) {
                        new_height = this.cropHeight;
                    }
                }
            }

        }
        //}


        /*if(new_width > this.WIDTH){
        new_height *= this.PROPORTION;
        }else{
        new_height /= this.PROPORTION;
        }*/

        if (!success) {
            return;
        }

        this.textNewHeight.setText(String.valueOf(new_height));
        this.textNewWidth.setText(String.valueOf(new_width));

        RESIZED_HEIGHT = new_height;
        RESIZED_WIDTH = new_width;

        if (this.checkEditImage.isSelected()) {
            this.OUTPUT = this.PROJECT_NAME + "_cropped_" + new_width + ".ptm";
        } else {
            this.OUTPUT = this.PROJECT_NAME + "_" + new_width + ".ptm";
        }
        this.textOutputFileName.setText(this.OUTPUT);
    }//GEN-LAST:event_textNewWidthKeyReleased

    private void textNewHeightKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textNewHeightKeyReleased
        // TODO add your handling code here:
        System.out.println("Entrou textNewHeightActionPerformed");
        int new_width = 0;
        int new_height = 0;
        boolean success = true;

        if (this.textNewWidth.getText().equals("") && this.textNewHeight.getText().equals("")) {
            return;
        }

        if (!this.textNewHeight.getText().equals("")) {
            try {
                new_height = Integer.valueOf(this.textNewHeight.getText());
            } catch (Exception e) {
                success = false;
                String s = this.textNewHeight.getText();

                this.textNewHeight.setText(s.substring(0, s.length() - 1));
                //Toolkit.getDefaultToolkit().beep();
                //JOptionPane.showMessageDialog(null, "Invalid Character");
            }
        }

        //if(this.textNewHeight.getText().equals("")){
        if (!checkEditImage.isSelected()) {
            if (new_height >= this.HEIGHT) {
                new_width = (int) (float) (1.0f / this.PROPORTION) * new_height;
                new_height = this.HEIGHT;
            } else {
                new_width = (int) (new_height / (1.0f / this.PROPORTION));
                if (new_width > this.WIDTH) {
                    new_width = this.WIDTH;
                }
            }
        } else {
            if ((cropHeight == 0) && (cropWidth == 0)) {
                JOptionPane.showMessageDialog(null, "Crop area not selected!");
                this.textNewHeight.setText("");
                this.textNewWidth.setText("");
                return;
            } else {
                if (new_height >= this.cropHeight) {
                    new_width = (int) (float) (1.0f / this.PROPORTION) * new_height;
                    new_height = this.cropHeight;
                } else {
                    new_width = (int) (new_height / (1.0f / this.PROPORTION));
                    if (new_width > this.cropWidth) {
                        new_width = this.cropWidth;
                    }
                }
            }
        }

        if (!success) {
            return;
        }

        this.textNewWidth.setText(String.valueOf(new_width));
        this.textNewHeight.setText(String.valueOf(new_height));
        RESIZED_HEIGHT = new_height;
        RESIZED_WIDTH = new_width;

        if (this.checkEditImage.isSelected()) {
            this.OUTPUT = this.PROJECT_NAME + "_cropped_" + new_width + ".ptm";
        } else {
            this.OUTPUT = this.PROJECT_NAME + "_" + new_width + ".ptm";
        }
        this.textOutputFileName.setText(this.OUTPUT);
    }//GEN-LAST:event_textNewHeightKeyReleased

    @SuppressWarnings("static-access")
    private void comboCropItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comboCropItemStateChanged
        // TODO add your handling code here:

        if (this.comboCrop.getSelectedIndex() == 1) {
            /* this.IMG_CROP_PANEL.reset();
            this.IMG_CROP_PANEL.setCropType(IMG_CROP_PANEL.FREE_CROP);
            this.IMG_CROP_PANEL.setImage(bf);
            this.IMG_CROP_PANEL.validate();
            this.IMG_CROP_PANEL.repaint();*/
            this.SELECTION.cleanCropArea();

            this.SELECTION.repaint();
            this.SELECTION.validate();
            this.SELECTION.setCropStyle(SELECTION.FREE_CROP);


        } else {
            /*this.IMG_CROP_PANEL.reset();
            this.IMG_CROP_PANEL.setCropType(IMG_CROP_PANEL.SELECTION);
            this.IMG_CROP_PANEL.setImage(bf);
            this.IMG_CROP_PANEL.validate();
            this.IMG_CROP_PANEL.repaint();*/
            this.SELECTION.cleanCropArea();
            this.SELECTION.repaint();
            this.SELECTION.validate();
            this.SELECTION.setCropStyle(SELECTION.RECTANGULAR_CROP);
        }

        /*
         * The area has gone so we need to actualize the actual size
         *
         */

        originalWidth.setText(WIDTH + " (px)");
        originalHeight.setText(HEIGHT + " (px)");
        textOutputFileName.setText(PROJECT_NAME + "_" + WIDTH + ".ptm");

        cropWidth = 0;
        cropHeight = 0;
        this.PROPORTION = (float) (this.WIDTH * 1.0f / this.HEIGHT);
        this.cbResize.setSelected(false);
        this.textNewHeight.setText("");
        this.textNewWidth.setText("");

    }//GEN-LAST:event_comboCropItemStateChanged

    private void checkEditImageItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_checkEditImageItemStateChanged
        // TODO add your handling code here:

        ArrayList<Point> pointList = null;
        if (checkEditImage.isSelected()) {

            this.jPanel1.remove(AREA_SELECTION_PANEL);
            SELECTION.enableDraw(true);
            this.jPanel1.add(SELECTION, java.awt.BorderLayout.CENTER);



            cropWidth = 0;
            cropHeight = 0;

            try {
                pointList = SELECTION.getCropPoints();

                if (!pointList.isEmpty())
                {
                    Polygon CROPPOLYGON = new Polygon();
                    for (Point p : pointList) {
                        CROPPOLYGON.addPoint(p.x, p.y);
                    }
                    Rectangle r = CROPPOLYGON.getBounds();
                    cropWidth = r.width;
                    cropHeight = r.height;
                }
            } catch (AreaNotDefined ex) {
                Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (cropWidth != 0) {
                //We can consider that no crop area was drawn so we
                //dont want to change the output file name
                if (cbResize.isSelected()) {

                    //this.textOutputFileName.setText(this.PROJECT_NAME + "_cropped_" + this.textNewWidth.getText() + ".ptm");
                    this.cbResize.setSelected(false);
                    this.textNewHeight.setText("");
                    this.textNewWidth.setText("");
                    this.originalHeight.setText(cropHeight + " (px)");
                    this.originalWidth.setText(cropWidth + " (px)");

                } else {
                    this.textOutputFileName.setText(this.PROJECT_NAME + "_cropped_" + this.cropWidth + ".ptm");
                    this.originalHeight.setText(String.valueOf(this.cropHeight) + " (px)");
                    this.originalWidth.setText(String.valueOf(this.cropWidth) + " (px)");
                }
                this.PROPORTION = (float) (this.cropWidth * 1.0f / this.cropHeight);
            } else {
                this.PROPORTION = (float) (this.WIDTH * 1.0f / this.HEIGHT);
            }

            this.jPanel1.validate();
            this.jPanel1.repaint();
            SELECTION.repaint();
            this.comboCrop.setEnabled(true);

        } else {
            SELECTION.cleanCropArea();
            SELECTION.enableDraw(false);
            SELECTION.repaint();
            //System.out.println("Is this thing even running??");
            //this.jPanel1.remove(SELECTION);
            this.jPanel1.add(AREA_SELECTION_PANEL, java.awt.BorderLayout.CENTER);
            this.jPanel1.validate();
            this.jPanel1.repaint();
            this.AREA_SELECTION_PANEL.enableDraw(false);
            this.comboCrop.setEnabled(false);

            this.originalHeight.setText(String.valueOf(this.HEIGHT) + " (px)");
            this.originalWidth.setText(String.valueOf(this.WIDTH) + " (px)");

            this.PROPORTION = (float) (this.WIDTH * 1.0f / this.HEIGHT);

            if (cbResize.isSelected()) {
                this.textOutputFileName.setText(this.PROJECT_NAME + "_" + this.textNewWidth.getText() + ".ptm");
                this.textNewHeight.setText("");
                this.textNewWidth.setText("");
            }
            this.textOutputFileName.setText(this.PROJECT_NAME + "_" + this.WIDTH + ".ptm");
        }
    }//GEN-LAST:event_checkEditImageItemStateChanged

    private void cbResizeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbResizeItemStateChanged
        // TODO add your handling code here:
        if (this.cbResize.isSelected()) {
            this.labelNewHeight.setVisible(true);
            this.labelNewWidth.setVisible(true);
            this.textNewHeight.setVisible(true);
            this.textNewWidth.setVisible(true);
            this.labelPx1.setVisible(true);
            this.labelPx2.setVisible(true);
            this.textOutputFileName.setText(this.PROJECT_NAME + "_" + this.textNewWidth.getText() + ".ptm");
            //this.checkCropPreview.setEnabled(true);
            if (this.checkEditImage.isSelected()) {
                this.textNewHeight.setText("");
                this.textNewWidth.setText("");
                if (cropWidth != 0) {

                    this.PROPORTION = (float) (this.cropWidth * 1.0f / this.cropHeight);
                } else {
                    this.PROPORTION = (float) (this.WIDTH * 1.0f / this.HEIGHT);
                }
            } else {
                this.PROPORTION = (float) (this.WIDTH * 1.0f / this.HEIGHT);
                this.textNewHeight.setText("");
                this.textNewWidth.setText("");
                this.textOutputFileName.setText(this.PROJECT_NAME + "_" + this.WIDTH + ".ptm");

            }
        } else {
            this.labelNewHeight.setVisible(false);
            this.labelNewWidth.setVisible(false);
            this.textNewHeight.setVisible(false);
            this.textNewWidth.setVisible(false);
            this.labelPx1.setVisible(false);
            this.labelPx2.setVisible(false);
            if (this.checkEditImage.isSelected()) {
                this.textOutputFileName.setText(this.PROJECT_NAME + "_cropped_" + this.cropWidth + ".ptm");
            } else {
                this.textOutputFileName.setText(this.PROJECT_NAME + "_" + this.WIDTH + ".ptm");
            }
            //this.checkCropPreview.setEnabled(false);
        }
    }//GEN-LAST:event_cbResizeItemStateChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        this.SELECTION.cleanCropArea();
        this.SELECTION.validate();
        this.cropPoints = new ArrayList<Point>();
        this.originalHeight.setText(HEIGHT + " (px)");
        this.originalWidth.setText(WIDTH + " (px)");
        if (cbResize.isSelected()) {
            this.cbResize.setSelected(false);
            this.textNewHeight.setText("");
            this.textNewWidth.setText("");
        }

        this.textOutputFileName.setText(this.PROJECT_NAME + "_" + this.WIDTH + ".ptm");
        this.PROPORTION = (float) (this.WIDTH * 1.0f / this.HEIGHT);
        this.definedArea = false;

    }//GEN-LAST:event_jButton1ActionPerformed

    public iDataCache getDataCache() {

        return cache;
    }

    public void setDataCache(iDataCache cache) {
        this.cache = cache;
    }

    private void checkEditImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkEditImageActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_checkEditImageActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonExecute;
    private javax.swing.JButton buttonFindHSHloc;
    private javax.swing.JCheckBox cbResize;
    private javax.swing.JCheckBox checkEditImage;
    private javax.swing.JComboBox comboCrop;
    private javax.swing.JComboBox comboSpheres;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel labelNewHeight;
    private javax.swing.JLabel labelNewWidth;
    private javax.swing.JLabel labelPx1;
    private javax.swing.JLabel labelPx2;
    private javax.swing.JLabel originalHeight;
    private javax.swing.JLabel originalWidth;
    private javax.swing.JRadioButton radioBLRGB;
    private javax.swing.JRadioButton radioBRGB;
    private javax.swing.JTextField textHSHfitterLocation;
    private javax.swing.JTextField textNewHeight;
    private javax.swing.JTextField textNewWidth;
    private javax.swing.JTextField textOutputFileName;
    // End of variables declaration//GEN-END:variables
}
