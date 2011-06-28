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

//import LPhelper.ImageSelectPanel;
import DataCache.iDataCache;
import Exceptions.ArgumentException;
import Exceptions.ModuleException;
import Exceptions.XMLcarrierException;
import ModuleInterfaces.PluginMetaInfo;
import Plugin.helpers.ImageContainer;
import Plugin.helpers.Pair;
import Plugin.helpers.SelectedArea;
import XMLcarrier.AreaInfo;
import XMLcarrier.Data;
import XMLcarrier.Event;
import XMLcarrier.Exceptions.UnknownProcessID;
import XMLcarrier.Exceptions.XMLNotAvailable;
import XMLcarrier.Exceptions.XSDCantValidadeXML;
import XMLcarrier.Process;
import XMLcarrier.Exceptions.UUIDNotFound;
import XMLcarrier.FileGroup;
import XMLcarrier.ImageFile;
import XMLcarrier.Info;
import XMLcarrier.Parameter;
import XMLcarrier.RawInfo;
import XMLcarrier.StageInfo;
import XMLcarrier.XMLHandler;
import spheredetection.SphereDetection;
import guicomponents.AreaSelectionPanel;
import guicomponents.ImageThumbnailButton;
import guicomponents.ProgressBarPopup;
import guicomponents.SpherePanel;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import operations.DefaultProjectImagesOpenTask;
import operations.OpenDataTask;
import spheredetection.helpers.Area;

/**
 *
 * @author Pedro
 */
public class PluginPanel extends javax.swing.JPanel {

    /**---Plugin info---**/
    /**Plugin description tag*/
    public static final String XML_VERSION_TAG = "Ball Detection V1.0";
    /**---Project info---**/
    /**XMLCarrier path*/
    private StringBuffer XMLpath = null;
    /**Plugin images*/
    private TreeMap<UUID, ImageContainer> Images;
    /**Areas selected by the user for ball detection*/
    private ArrayList<SelectedArea> SELECTED_AREAS;
    /**Execution flag*/
    private boolean executed;
    /**---Interface variables---**/
    /**Panel for area selection*/
    private AreaSelectionPanel Area_Selection_Panel;
    /**Plugin sphere detection display panel*/
    private SpherePanel SphereDetection_Preview_Panel;
    /**Current selected picture*/
    private UUID current_selected_image;
    /**Current selected picture type*/
    private String current_selected_type;
    /**---Ball detection process variables---**/
    /**Error on Ball Detection process*/
    private int ERROR = 0;
    /**No errors on Ball Detection*/
    private final int NO_ERRORS = 0;
    /**No selected areas for Ball Detection process*/
    private final int USER_SELECT_AREAS = 1;
    /**No selected pictures on a selected area*/
    private final int NO_SELECTED_PICS = 2;
    /**Error on writing information on XMLcarrier*/
    private final int ERROR_ON_XMLCARRIER = 3;
    /**XMLcarrier variable for data edition*/
    private XMLHandler carrier;
    /**Ball detection current process UUID*/
    private UUID BDprocessID;
    /**Ball detection input field UUID*/
    private UUID BDprocessOutputID;
    /**Ball detection output field UUID*/
    private UUID BDprocessInputID;
    /**Ball detection process version UUID*/
    private UUID BDprocess_moduleID;

    public iDataCache cache;
    public PluginMetaInfo plugInfo;

    /**


    /** Creates new form GUIteste */
    public PluginPanel() {
        initComponents();
        executed = false;
        //Initalize preview and area selection panel
        Area_Selection_Panel = new AreaSelectionPanel();

        //On click
        Area_Selection_Panel.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selected = Area_Selection_Panel.getSelectedArea();
                if (!SELECTED_AREAS.isEmpty()) {
                    cbSelectedBallArea.setSelectedIndex(selected);
                }
            }
        });

        Area_Selection_Panel.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Image_Selection_MouseReleased(evt);
            }
        });
        this.jPanel2.add(Area_Selection_Panel, java.awt.BorderLayout.CENTER);

    }

    public void Image_Selection_MouseReleased(java.awt.event.MouseEvent evt) {
        Rectangle r = Area_Selection_Panel.getChangedSelection();
        if (r != null) {
            SELECTED_AREAS.get(Area_Selection_Panel.getSelectedArea()).setSelectionArea(r);
            SelectedArea area = SELECTED_AREAS.get(Area_Selection_Panel.getSelectedArea());

            //Calcule of luminace for each image;
            for (ImageContainer imgC : Images.values()) {
                imgC.calculateLuminance(area.getSelectionArea());
            }

            //Selects the minimum 10 or the 25%  best images to Ball Detection.
            Object[] img_array = Images.values().toArray();
            java.util.Arrays.sort(img_array);
            ArrayList<ImageContainer> arrayI = new ArrayList<ImageContainer>();

            int nimgs = Math.round(Images.size() * 0.25f);
            nimgs = (nimgs < 10) ? 10 : nimgs;

            for (int i = 0; i < img_array.length && i < nimgs; i++) {
                arrayI.add(i, (ImageContainer) img_array[i]);
            }
            //Sets the chosen images selected
            java.awt.Component[] c = jpOpenOptions.getComponents();
            for (int i = 0; i < c.length; i++) {
                if (c[i] instanceof ImageThumbnailButton) {
                    ImageThumbnailButton isp = (ImageThumbnailButton) c[i];
                    Images.get(isp.getImageId()).setSelected_for_the_Area(area.getId(), arrayI.contains(Images.get(isp.getImageId())));
                    isp.setSelected(arrayI.contains(Images.get(isp.getImageId())));
                }
            }
            // jbASNext.setEnabled(false);
        }
    }

    /**Method responsible for image initialization*/
    public void start(StringBuffer XMLurl) throws ArgumentException, ModuleException, XMLcarrierException {
        System.out.println("Ball Detect: start called");
        //Disable the ball detection action button.
        //jbDetectGlossyBall.setEnabled(false);
        XMLpath = XMLurl;

        //Instanciate a new XmlHandler from the given path
        carrier = new XMLHandler(XMLpath.toString());



        //Reset interface
        resetInterface();

        //Data extracted from carrier
        RawInfo images = null;
        try {

            carrier.loadXML();
        } catch (Exception e) {
            throw new XMLcarrierException("Error when opening the XMLcarrier", e);
        }

        //Get image section info
        images = carrier.getComputedInfo("Images");

        //Get id string for the images
        String imagesID = images.getAttribute("ID");

        //Test if the field is really available
        if (imagesID == null) {
            throw new ArgumentException("Input not present, the image set is not defined!");
        }

        //Get file_group id
        UUID imagegroup_id = UUID.fromString(imagesID);

        try {
            //Get all images for process execution
            ArrayList<ImageFile> image_files = carrier.getImageList(imagegroup_id);

            //Get the image width and height from XMlcarrier if present
            int width = -1;
            int height = -1;
            String whidthS = carrier.getProjectInfo().getParamterByName("Image width");
            String heightS = carrier.getProjectInfo().getParamterByName("Image height");
            if (whidthS != null && heightS != null) {
                width = (int) Float.parseFloat(whidthS);
                height = (int) Float.parseFloat(heightS);
            }
            //Load all images
            Images = new TreeMap<UUID, ImageContainer>();
            String projectpath = carrier.getProjectInfo().getParamterByName("ProjectPath")+File.separator;
            for (int i = 0; i < image_files.size(); i++) {
                UUID image_id = UUID.fromString(image_files.get(i).getUuid());
                File f = new File(projectpath+image_files.get(i).getUrl());
                //Create a container
                ImageContainer container = new ImageContainer(image_id, f);
                container.setHeight(height);
                container.setWidth(width);
                Images.put(image_id, container);


            }

        } catch (UUIDNotFound ex) {
            throw new XMLcarrierException("Error when extracting data from the XMLcarrier!", ex);
        }

        //Initialize the selected areas array
        SELECTED_AREAS = new ArrayList<SelectedArea>();
        //Initialize panel
        SphereDetection_Preview_Panel = new SpherePanel();
        executed = false;

        boolean useCache = false;


        // Handle thumbnails!
        final PluginPanel thisW = this;
        Map<UUID, BufferedImage> thumbs = (Map<UUID, BufferedImage>)this.cache.get("Cached Original Thumbs");
        TreeMap<UUID, BufferedImage> imgMap = new TreeMap();

        if (thumbs != null)
        {
            useCache = true;
            imgMap = new TreeMap(thumbs);
        }

        //if (thumbs == null)
        {
            // Empty cache, load images!
            ArrayList<FileGroup> fgl = carrier.getAllFileGroups();
            FileGroup fg = null;
            for(FileGroup fgInL : fgl)
            {
                if (fgInL.getUse().equals("Original image files"))
                    fg = fgInL;
            }

            String projectpath = carrier.getProjectInfo().getParamterByName("ProjectPath") + File.separator;
            System.out.println("Debug: BallDetect ProjectPath = "+projectpath);
            OpenDataTask tasker = new OpenDataTask(new DefaultProjectImagesOpenTask(true, useCache, projectpath) {

                PluginPanel parentW = null;

                {
                    parentW = thisW;
                }

                @Override
                public void taskOnDone() {
                    //synchronized (this) {
                    {
                        // Initialize thumbnail panel as a callback...

                        // Now, we must interpret the FileGroup's data and read the image files
                        //ArrayList<ImageFile> imgList = this.fGroup.getList();
                        //ArrayList<UUID> uuidList = this.fGroup.getRefList();

                        TreeMap<UUID, BufferedImage> m = this.getData();

                        System.out.println("Number of images read by experimental code: " + m.size());


                        //this.ProjectImages = imgMap;
                        //System.out.println("Final images: " + imgMapTransformed.toString());
                        System.out.flush();
                        //if (parentW.getDataCache()!= null)
                        {
                            System.out.println("Ball Detection: Cache in use ID:" + parentW.cache.getID());
                            parentW.cache.remove("Cached Original Thumbs");
                            parentW.cache.put("Cached Original Thumbs", m);
                        }
                    }
                    parentW.setThumbnails(imgMap);
                    parentW.jpSelectionThumbs.validate();
                    
                    // Restore picks and spheres and selected areas! ARGH
                    FileGroup pickedImgs = null;  //(FileGroup)parentW.cache.get("Images for BallDetection");
                    ArrayList<FileGroup> fgs = parentW.carrier.getAllFileGroups();

                    // Grab FileGroup information from XML, carefully ordered, so as to know which images correspond to each
                    // sphere.
                    ArrayList<FileGroup> sphereFGs = new ArrayList();
                    ArrayList<FileGroup> stageImgFGs = new ArrayList();
                    for (FileGroup fg : fgs)
                    {
                        if (fg.getUse().equals("Images for BallDetection")) sphereFGs.add(fg);
                        else if (fg.getUse().equals("Stage1 and Stage2 images")) stageImgFGs.add(fg);
                    }


                    parentW.BDprocess_moduleID = UUID.nameUUIDFromBytes(SphereDetection.XML_VERSION_TAG.getBytes());

                    String pid = "";
                    String procPID = "";
                    for (Process p : carrier.getAllProcesses()) {
                        if (p.getType().equals("BALLDETECTION") && !p.getStatus().equals("OLD")) {
                            pid = p.getComponentID();
                            procPID = p.getId();
                            if (parentW.BDprocessID == null) {
                                parentW.BDprocessID = UUID.fromString(p.getComponentID());
                                try {
                                    parentW.BDprocessInputID = carrier.getInput(parentW.BDprocessID.toString()).get(0).getId();
                                    UUID fgWanted = carrier.getInput(parentW.BDprocessID.toString()).get(0).getAreas().get(0).getFileGroupID();
                                    pickedImgs = carrier.getFileGroup(fgWanted);
                                } catch (UUIDNotFound ex) {
                                    Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (UnknownProcessID ex) {
                                    Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                try {
                                    parentW.BDprocessOutputID = carrier.getOutput(parentW.BDprocessID.toString()).get(0).getId();
                                } catch (UUIDNotFound ex) {
                                    Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (UnknownProcessID ex) {
                                    Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }

                    // Get Area data
                    ArrayList<Data> dataL = new ArrayList();
                    try {
                        dataL = carrier.getInputByProcessID(procPID); //carrier.getAllData();
                    } catch (UUIDNotFound ex) {
                        Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (UnknownProcessID ex) {
                        // It is normal and just so people don't think there is a BIG problem when/if they look at the logs and see exceptions
                        // where there probably shouldn't be any... no more logging it.
                        //Logger.getLogger(PluginPanel.class.getName()).log(Level.INFO, null, ex);
                    }
                    /*
                    try {
                    dataL = carrier.getInput(pid); //new ArrayList(); //carrier.getAllData();
                    System.out.println("Got input: "+dataL.toString());
                    } catch (UUIDNotFound ex) {
                    Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                    } catch (UnknownProcessID ex) {
                    Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                    }*/

                    ArrayList<SelectedArea> areaL = new ArrayList();
                    SELECTED_AREAS = areaL;

                    boolean settingsDone = false;

                    ArrayList< Pair<SelectedArea,UUID> > selAreaFGuuid = new ArrayList();

                    // Process Input data
                    for (Data d : dataL)
                    {
                        //if (d.getId().toString().equals(procPID))
                        //{

                        if (d.getName().equals("BallDetectionInput"))
                        {
                            //Area a = new Area(d);

                            ArrayList<XMLcarrier.AreaInfo> aL = d.getAreas();
                            for (AreaInfo A : aL)
                            {
                                TreeMap<String,String> attribs = A.getAllAttributes();
                                System.out.println("dumping areainfo attrib...");
                                System.out.println(attribs.toString());
                                System.out.flush();
                                //String uuid = A.getAttribute("UUID");
                                String algorithm = A.getAttribute("algorithm");
                                String binarize = A.getAttribute("binarize");
                                String hough = A.getAttribute("hough");


                                Rectangle r = new Rectangle();
                                r.setRect(A.getBegin().getX(), A.getBegin().getY(), A.getEnd().getX() - A.getBegin().getX(), A.getEnd().getY() - A.getBegin().getY());
                                SelectedArea s = new SelectedArea(r);
                                parentW.Area_Selection_Panel.setSelectionArea(r);
                                parentW.Area_Selection_Panel.setSelected(0);
                                parentW.jbDetectGlossySphere.setEnabled(true);
                                //parentW.


                                s.setAlgorithm(Integer.valueOf(algorithm));
                                s.setBinarize(Boolean.valueOf(binarize));
                                //s.setId(UUID.fromString(uuid));
                                s.setHough(Boolean.valueOf(hough));
                                s.setId(A.getAreaId());

                                if (!settingsDone)
                                {
                                    LP_CONF_USE_BINARIZE.setSelected(Boolean.valueOf(binarize));
                                    LP_CONF_USE_RED_HOUGH_CHECKBOX.setSelected(Boolean.valueOf(hough));
                                    LPAlgorithmSelectionComboBox.setSelectedIndex(Integer.valueOf(algorithm));
                                    settingsDone = true;
                                }

                                areaL.add(s);
                                selAreaFGuuid.add(new Pair(s, A.getFileGroupID()));

                                parentW.cbSelectedBallArea.addItem(cbSelectedBallArea.getItemCount() + 1);
                                parentW.cbSelectedBallArea.setSelectedIndex(0);
                                //A.getBegin()
                            }
                            //areaL.add(auxDataToSelectedArea(d));
                            System.out.println("Found BallDetectionInput: "+d.toString());
                        }
                        
                        //}
                    }


                    // Process output data
                    boolean outputExists = false;
                    // Process Output data
                    for (SelectedArea s : areaL)
                    {
                        //parentW.BDprocess_moduleID;
                        try {
                            //Data d = carrier.getOutput()
                            ArrayList<Data> dL = carrier.getOutput(pid);

                            outputExists = true;
                            for (Data d : dL) {
                                if ( d.getAreaByID(s.getId())!=null) {
                                    //Area a = new Area(d);

                                    AreaInfo A = d.getAreaByID(s.getId());

                                    //ArrayList<XMLcarrier.AreaInfo> aL = d.getAreas();
                                    //for (AreaInfo A : aL) {
                                        TreeMap<String, String> attribs = A.getAllAttributes();
                                        System.out.println("dumping areainfo attrib...");
                                        System.out.println(attribs.toString());
                                        System.out.flush();

                                        String EdgeID = A.getAttribute("EdgeID");
                                        String MedianID = A.getAttribute("MedianID");
                                        String R = A.getAttribute("r");
                                        String X = A.getAttribute("x");
                                        String Y = A.getAttribute("y");

                                        // Set stuff... if still required.
                                        float[] detectR = new float[3];
                                        detectR[0] = Float.valueOf(X);
                                        detectR[1] = Float.valueOf(Y);
                                        detectR[2] = Float.valueOf(R);
                                        s.setSphereDetection(detectR);
                                    System.out.println("Found BallDetectionInput: " + d.toString());
                                }
                            }

                        } catch (UnknownProcessID ex) {
                            Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (UUIDNotFound ex) {
                            Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    //parentW.setThumbnails(imgMap);
                    //parentW.jpSelectionThumbs.validate();

                    // Handle checkboxes and crap
                    for (SelectedArea sA : areaL )
                    {
                        UUID saUUID; // = selAreaFGuuid.get(sA);
                        int i=0;
                        while (i < selAreaFGuuid.size() && selAreaFGuuid.get(i).first != sA)
                            i++;

                        saUUID = selAreaFGuuid.get(i).last;

                        FileGroup fg = carrier.getFileGroup(saUUID);
                        for (UUID imgId : fg.getRefList())
                        {
                            Images.get(imgId).setSelected_for_the_Area(sA.getId(), true);
                        }
                    }

                    // Set the image checkboxes
                    /*
                    if (pickedImgs != null)
                    {
                        java.awt.Component[] c = parentW.jpSelectionThumbs.getComponents(); //jpOpenOptions.getComponents();
                        System.out.println("Thumbs component array: " + c.length);
                        for (int i = 0; i < c.length; i++) {
                            if (c[i] instanceof ImageThumbnailButton) {
                                ImageThumbnailButton isp = (ImageThumbnailButton) c[i];
                                if (pickedImgs.getRefList().contains(isp.getImageId()))
                                {
                                    isp.setSelected(true);
                                    isp.validate();
                                }
                                //if(sphereFGs.  isp.getImageId())
                                //Images.get(isp.getImageId()).setSelected_for_the_Area(area.getId(), arrayI.contains(Images.get(isp.getImageId())));
                                //isp.setSelected(arrayI.contains(Images.get(isp.getImageId())));
                            }
                        }
                    }*/

                    

                    //
                    //for(int selected = 0; selected < SELECTED_AREAS.size(); selected++)
                    //{
                    int selected = cbSelectedBallArea.getSelectedIndex();
                    if (!SELECTED_AREAS.isEmpty()) {
                        java.awt.Component[] c = jpSelectionThumbs.getComponents();
                        for (int i = 0; i < c.length; i++) {
                            if (c[i] instanceof ImageThumbnailButton) {
                                ImageThumbnailButton ispanel = (ImageThumbnailButton) c[i];
                                ispanel.setSelected(Images.get(ispanel.getImageId()).is_selected_for_the_Area(SELECTED_AREAS.get(selected).getId()));
                            }
                        }
                    }

                    //}

                    if (outputExists) parentW.setResults(true);

                    parentW.validate();
                }
            }, fg, imgMap, fg.getList().size(), "Loading project images...", "Image being loaded: "//, pbp
                    );

            tasker.execute();

        }
        //else
        //{
            // Not empty!
        //    setThumbnails(thumbs);
        //}
    }


    private void loadFromCarrier(PluginPanel parentW)
    {

    }

    private Pair<Integer,Integer> parseCoord(String s)
    {
        int x = 0, y = 0;

        Scanner scanSp = new Scanner(s);
        scanSp.useDelimiter(" ");
        if (scanSp.hasNext())
        {
            String tx = scanSp.next();
            Scanner scanColon = new Scanner(tx);
            scanColon.useDelimiter(":");
            scanColon.next();
            x = Integer.decode(scanColon.next());

            String ty = scanSp.next();
            scanColon = new Scanner(ty);
            scanColon.useDelimiter(":");
            scanColon.next();
            y = Integer.decode(scanColon.next());
        }
        return new Pair(x,y);
    }

    @Deprecated
    private SelectedArea auxDataToSelectedArea(Data d)
    {
        Rectangle r = new Rectangle();



        Parameter beginCoord = d.getParameter("BEGIN");
        Parameter coords = d.getParameter("COORDS");
        Parameter endCoord = d.getParameter("END");
        Parameter areaShape = d.getParameter("SHAPE");
        Parameter uuid = d.getParameter("UUID");
        Parameter algorithm = d.getParameter("algorithm");
        Parameter binarize = d.getParameter("binarize");
        Parameter fileGrpID = d.getParameter("fileGrpID");
        Parameter hough = d.getParameter("hough");
        Parameter projectName = d.getParameter("projectName");
        Parameter projectPath = d.getParameter("projectPath");

        Pair<Integer,Integer> begin, end, temp;

        begin = parseCoord(beginCoord.getValueString());
        end = parseCoord(endCoord.getValueString());

        Scanner scanSemiCol = new Scanner(coords.getValueString());

        scanSemiCol.useDelimiter(";");

        if (scanSemiCol.hasNext())
        {
            temp = parseCoord(scanSemiCol.next());
            r.add(temp.first, temp.last);
            temp = parseCoord(scanSemiCol.next());
            r.add(temp.first, temp.last);
            temp = parseCoord(scanSemiCol.next());
            r.add(temp.first, temp.last);
            temp = parseCoord(scanSemiCol.next());
            r.add(temp.first, temp.last);
        }

        SelectedArea a = new SelectedArea(r);

        a.setAlgorithm((Integer)algorithm.getValue());
        a.setBinarize((Boolean)binarize.getValue());
        a.setId((UUID)uuid.getValue());
        a.setHough((Boolean)hough.getValue());

        // MORE? Maybe... ImageContainers empty
        

        return a;
    }

    public void setThumbnails(Map<UUID, BufferedImage> thumbnails) {
        System.out.println("Ball Detect: setThumbnails called");

        //Remove elements that may be on panel.
        //jpSelectionThumbs.removeAll();
        System.out.println(thumbnails.toString());
        System.out.println(Images.toString());

        //for all thumbnails set thumbnails and set the listeners
        for (UUID image_id : thumbnails.keySet()) {
            //Get the associated image container
            ImageContainer image_container = Images.get(image_id);
            //Load icon
            image_container.loadIcon(thumbnails.get(image_id));
            //Create a new thumbnail button
            ImageThumbnailButton button = new ImageThumbnailButton(image_container.getImage_name(), image_container.getImage_id());
            //Set icon.
            button.setIcon(thumbnails.get(image_id));
            //Add to panel
            jpSelectionThumbs.add(button);
            //Create a listener to the button
            button.setButtonListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    //Get source button
                    JButton button = (JButton) (e.getSource());
                    ImageThumbnailButton itb = (ImageThumbnailButton) button.getParent();
                    //Get the image for this button
                    Area_Selection_Panel.setImage(Images.get(itb.getImageId()).getImage());
                    Area_Selection_Panel.repaint();

                }
            });
            button.setCheckBoxListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    int selected_area = cbSelectedBallArea.getSelectedIndex();
                    if (selected_area != -1) {
                        JCheckBox selection = (JCheckBox) e.getSource();
                        ImageThumbnailButton isp = (ImageThumbnailButton) selection.getParent().getParent();
                        Images.get(isp.getImageId()).setSelected_for_the_Area(SELECTED_AREAS.get(selected_area).getId(), selection.isSelected());
                    }
                }
            });

        }
        //Get a button to do a click, to show a image on panel
        ImageThumbnailButton button = (ImageThumbnailButton) jpSelectionThumbs.getComponent(0);
        button.getImageButton().doClick();
    }

    /**Reset interface variables*/
    private void resetInterface() {
        //Reset variables
        //Remove thumbnails
        jpSelectionThumbs.removeAll();
        jpSDThumbnails.removeAll();
        //Remove selected areas
        cbSelectedBallArea.removeAllItems();
        //Remove possible results
        jtBALL_DETECTION_RESULTS.removeAll();
        //Reset the Area selection panel
        Area_Selection_Panel.reset();
        Area_Selection_Panel.repaint();
        //Set the black algorithm
        LPAlgorithmSelectionComboBox.setSelectedIndex(0);
        //Make sure the panel that is shown is the first one
        CardLayout cl = (CardLayout) this.getLayout();
        cl.first(this);
        //Disable the next button
        jbSDNext.setVisible(false);
        //Disable the detect button
        jbDetectGlossySphere.setEnabled(false);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        inputPanel = new javax.swing.JPanel();
        jpOpenOptions = new javax.swing.JPanel();
        jbDetectGlossySphere = new javax.swing.JButton();
        LPOOptionPanel = new javax.swing.JPanel();
        LPGlossyBallLabel = new javax.swing.JLabel();
        LPAlgorithmSelectionComboBox = new javax.swing.JComboBox();
        LP_CONF_USE_BINARIZE = new javax.swing.JCheckBox();
        LP_CONF_USE_RED_HOUGH_CHECKBOX = new javax.swing.JCheckBox();
        jPanel14 = new javax.swing.JPanel();
        cbSelectedBallArea = new javax.swing.JComboBox();
        jbAddSelectionArea = new javax.swing.JButton();
        jbDeleteSelectionArea = new javax.swing.JButton();
        jbSDNext = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jSplitPane4 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jpSelectionThumbs = new javax.swing.JPanel();
        jlSelectedArea = new javax.swing.JLabel();
        outputPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jsX = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jsY = new javax.swing.JSpinner();
        jsRadius = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jsDBImageScale = new javax.swing.JSlider();
        jbSDBack = new javax.swing.JButton();
        jbSetNewCenter = new javax.swing.JButton();
        delete_sphere_button = new javax.swing.JButton();
        jSplitPane2 = new javax.swing.JSplitPane();
        jtBALL_DETECTION_RESULTS = new javax.swing.JTabbedPane();
        jSplitPane6 = new javax.swing.JSplitPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jpSDThumbnails = new javax.swing.JPanel();
        jlSelectedDetectedBall = new javax.swing.JLabel();

        setLayout(new java.awt.CardLayout());

        inputPanel.setLayout(new java.awt.BorderLayout());

        jbDetectGlossySphere.setText("Detect Spheres");
        jbDetectGlossySphere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDetectGlossySphereActionPerformed(evt);
            }
        });

        LPOOptionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Process Configuration"));
        LPOOptionPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        LPGlossyBallLabel.setText("Glossy Ball");
        LPOOptionPanel.add(LPGlossyBallLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 30, -1, 20));

        LPAlgorithmSelectionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Black", "Red" }));
        LPAlgorithmSelectionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LPAlgorithmSelectionComboBoxActionPerformed(evt);
            }
        });
        LPOOptionPanel.add(LPAlgorithmSelectionComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 30, 90, -1));

        LP_CONF_USE_BINARIZE.setText("Binarize before Hough Transform (slower)");
        LP_CONF_USE_BINARIZE.setToolTipText("Use binarization algorithm before ball detection (slower)");
        LP_CONF_USE_BINARIZE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LP_CONF_USE_BINARIZEActionPerformed(evt);
            }
        });
        LPOOptionPanel.add(LP_CONF_USE_BINARIZE, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, -1, 20));

        LP_CONF_USE_RED_HOUGH_CHECKBOX.setText("Use Hough Transform (slower)");
        LP_CONF_USE_RED_HOUGH_CHECKBOX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LP_CONF_USE_RED_HOUGH_CHECKBOXActionPerformed(evt);
            }
        });
        LPOOptionPanel.add(LP_CONF_USE_RED_HOUGH_CHECKBOX, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, -1, 20));

        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Pre-selected areas", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));

        cbSelectedBallArea.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSelectedBallAreaActionPerformed(evt);
            }
        });

        jbAddSelectionArea.setText("Add Area");
        jbAddSelectionArea.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAddSelectionAreaActionPerformed(evt);
            }
        });

        jbDeleteSelectionArea.setText("Delete Area");
        jbDeleteSelectionArea.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDeleteSelectionAreaActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel14Layout = new org.jdesktop.layout.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel14Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, cbSelectedBallArea, 0, 110, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jbDeleteSelectionArea, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jbAddSelectionArea, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .add(cbSelectedBallArea, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 54, Short.MAX_VALUE)
                .add(jbAddSelectionArea)
                .add(12, 12, 12)
                .add(jbDeleteSelectionArea)
                .addContainerGap())
        );

        jbSDNext.setText("Go to Output ->");
        jbSDNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSDNextPreviousPanelActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jpOpenOptionsLayout = new org.jdesktop.layout.GroupLayout(jpOpenOptions);
        jpOpenOptions.setLayout(jpOpenOptionsLayout);
        jpOpenOptionsLayout.setHorizontalGroup(
            jpOpenOptionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jpOpenOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(LPOOptionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 512, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 229, Short.MAX_VALUE)
                .add(jpOpenOptionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jbDetectGlossySphere, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 190, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jbSDNext))
                .addContainerGap())
        );
        jpOpenOptionsLayout.setVerticalGroup(
            jpOpenOptionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jpOpenOptionsLayout.createSequentialGroup()
                .add(jpOpenOptionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jpOpenOptionsLayout.createSequentialGroup()
                        .add(22, 22, 22)
                        .add(jbDetectGlossySphere, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 110, Short.MAX_VALUE)
                        .add(jbSDNext))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jpOpenOptionsLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jpOpenOptionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, LPOOptionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
                            .add(jPanel14, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );

        inputPanel.add(jpOpenOptions, java.awt.BorderLayout.PAGE_END);

        jSplitPane1.setDividerLocation(500);

        jPanel2.setLayout(new java.awt.BorderLayout());
        jSplitPane1.setLeftComponent(jPanel2);

        jSplitPane4.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jpSelectionThumbs.setLayout(new java.awt.GridLayout(0, 6));
        jScrollPane2.setViewportView(jpSelectionThumbs);

        jSplitPane4.setBottomComponent(jScrollPane2);

        jlSelectedArea.setFont(new java.awt.Font("Dialog", 1, 14));
        jlSelectedArea.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlSelectedArea.setText("Selected Area 1");
        jSplitPane4.setLeftComponent(jlSelectedArea);

        jSplitPane1.setRightComponent(jSplitPane4);

        inputPanel.add(jSplitPane1, java.awt.BorderLayout.CENTER);

        add(inputPanel, "card2");

        outputPanel.setLayout(new java.awt.BorderLayout());

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Ball Center and Radius"));

        jsX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jsXStateChanged(evt);
            }
        });

        jLabel3.setText("X");

        jLabel4.setText("Y");

        jsY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jsYStateChanged(evt);
            }
        });

        jsRadius.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jsRadiusStateChanged(evt);
            }
        });

        jLabel5.setText("Radius");

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jsX, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jsY, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel5)
                    .add(jsRadius, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(123, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(jLabel4)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jsX, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jsY, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jsRadius, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(52, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Image Scale"));

        jsDBImageScale.setMajorTickSpacing(25);
        jsDBImageScale.setMaximum(200);
        jsDBImageScale.setMinimum(10);
        jsDBImageScale.setMinorTickSpacing(5);
        jsDBImageScale.setPaintLabels(true);
        jsDBImageScale.setPaintTicks(true);
        jsDBImageScale.setValue(100);
        jsDBImageScale.setValueIsAdjusting(true);
        jsDBImageScale.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jsDBImageScaleStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel7Layout.createSequentialGroup()
                .add(jsDBImageScale, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jsDBImageScale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        jbSDBack.setText("<- Redo Process");
        jbSDBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSDBackPreviousPanelActionPerformed(evt);
            }
        });

        jbSetNewCenter.setText("Set New Center");
        jbSetNewCenter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSetNewCenterActionPerformed(evt);
            }
        });

        delete_sphere_button.setText("Delete Sphere");
        delete_sphere_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delete_sphere_buttonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 218, Short.MAX_VALUE)
                        .add(jbSDBack)
                        .add(10, 10, 10))
                    .add(jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, delete_sphere_button, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jbSetNewCenter, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE))
                        .add(209, 209, 209))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(30, 30, 30)
                .add(jbSetNewCenter)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(delete_sphere_button)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 48, Short.MAX_VALUE)
                .add(jbSDBack)
                .add(11, 11, 11))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 116, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .add(21, 21, 21))
        );

        outputPanel.add(jPanel4, java.awt.BorderLayout.PAGE_END);

        jSplitPane2.setDividerLocation(500);

        jtBALL_DETECTION_RESULTS.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jtBALL_DETECTION_RESULTSStateChanged(evt);
            }
        });
        jSplitPane2.setLeftComponent(jtBALL_DETECTION_RESULTS);

        jSplitPane6.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jpSDThumbnails.setLayout(new java.awt.GridLayout(0, 6));
        jScrollPane3.setViewportView(jpSDThumbnails);

        jSplitPane6.setRightComponent(jScrollPane3);

        jlSelectedDetectedBall.setFont(new java.awt.Font("Verdana", 1, 14));
        jlSelectedDetectedBall.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlSelectedDetectedBall.setMaximumSize(new java.awt.Dimension(20, 20));
        jlSelectedDetectedBall.setMinimumSize(new java.awt.Dimension(20, 20));
        jSplitPane6.setLeftComponent(jlSelectedDetectedBall);

        jSplitPane2.setRightComponent(jSplitPane6);

        outputPanel.add(jSplitPane2, java.awt.BorderLayout.CENTER);

        add(outputPanel, "card3");
    }// </editor-fold>//GEN-END:initComponents

	private void LPAlgorithmSelectionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LPAlgorithmSelectionComboBoxActionPerformed
            //When the user changes the used algorithm, this action updates the value for the selected value and set the checkboxs.
            //enable/disable, selected/not selected as the algorithm demands.
            int sel = cbSelectedBallArea.getSelectedIndex();
            int SELECTED_ALGORITHM = this.LPAlgorithmSelectionComboBox.getSelectedIndex();
            if (sel != -1) {
                SELECTED_AREAS.get(sel).setAlgorithm(LPAlgorithmSelectionComboBox.getSelectedIndex());
                if (SELECTED_ALGORITHM == 0) {
                    SELECTED_AREAS.get(sel).setHough(true);
                }
                jlSelectedArea.setText("Selected Area " + (sel + 1));
            }
            this.LP_CONF_USE_RED_HOUGH_CHECKBOX.setEnabled((SELECTED_ALGORITHM != 0));
            if (SELECTED_ALGORITHM == 0) {
                this.LP_CONF_USE_RED_HOUGH_CHECKBOX.setSelected(true);
            }
            this.LP_CONF_USE_BINARIZE.setEnabled((SELECTED_ALGORITHM == 0 || this.LP_CONF_USE_RED_HOUGH_CHECKBOX.isSelected()));
}//GEN-LAST:event_LPAlgorithmSelectionComboBoxActionPerformed

	private void LP_CONF_USE_BINARIZEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LP_CONF_USE_BINARIZEActionPerformed
            //Updates the value for the selected area.
            int sel = cbSelectedBallArea.getSelectedIndex();
            if (sel != -1) {
                SELECTED_AREAS.get(sel).setBinarize(LP_CONF_USE_BINARIZE.isSelected());
            }
}//GEN-LAST:event_LP_CONF_USE_BINARIZEActionPerformed

	private void LP_CONF_USE_RED_HOUGH_CHECKBOXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LP_CONF_USE_RED_HOUGH_CHECKBOXActionPerformed
            //Updates the value for the selected area and enables/disables the binarize checkbox.
            int sel = cbSelectedBallArea.getSelectedIndex();
            if (sel != -1) {
                SELECTED_AREAS.get(sel).setHough(LP_CONF_USE_RED_HOUGH_CHECKBOX.isSelected());
                if (!LP_CONF_USE_RED_HOUGH_CHECKBOX.isSelected()) {
                    SELECTED_AREAS.get(sel).setBinarize(false);
                }
            }
            this.LP_CONF_USE_BINARIZE.setEnabled(LP_CONF_USE_RED_HOUGH_CHECKBOX.isSelected());
            if (!LP_CONF_USE_RED_HOUGH_CHECKBOX.isSelected()) {
                this.LP_CONF_USE_BINARIZE.setSelected(false);
            }
}//GEN-LAST:event_LP_CONF_USE_RED_HOUGH_CHECKBOXActionPerformed

	private void cbSelectedBallAreaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSelectedBallAreaActionPerformed

            //Sets the ball detection properties when we change a selected area, in the comboBox or the panel.
            int selected = cbSelectedBallArea.getSelectedIndex();
            if (selected != -1) {
                LP_CONF_USE_RED_HOUGH_CHECKBOX.setSelected(SELECTED_AREAS.get(selected).getHough());
                LP_CONF_USE_BINARIZE.setSelected(SELECTED_AREAS.get(selected).getBinarize());
                LPAlgorithmSelectionComboBox.setSelectedIndex(SELECTED_AREAS.get(selected).getAlgorithm());
                //Sets selected all the images chosen in this tab.
                Area_Selection_Panel.setSelected(selected);
                if (!SELECTED_AREAS.isEmpty()) {
                    java.awt.Component[] c = jpSelectionThumbs.getComponents();
                    for (int i = 0; i < c.length; i++) {
                        if (c[i] instanceof ImageThumbnailButton) {
                            ImageThumbnailButton ispanel = (ImageThumbnailButton) c[i];
                            ispanel.setSelected(Images.get(ispanel.getImageId()).is_selected_for_the_Area(SELECTED_AREAS.get(selected).getId()));
                        }
                    }
                }
            }
	}//GEN-LAST:event_cbSelectedBallAreaActionPerformed

	private void jbAddSelectionAreaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAddSelectionAreaActionPerformed
            //Defines a new area where a ball detection will be atempted, calculating the best 25% images in terms of luminace with a minimum of 10 to this process.
            Rectangle rect_area = Area_Selection_Panel.confirmSelectionArea();

            if (rect_area != null) {

                SelectedArea area = new SelectedArea(rect_area);
                //Calcule of luminace for each image;
                for (ImageContainer imgC : Images.values()) {
                    imgC.calculateLuminance(area.getSelectionArea());
                }

                //Selects the minimum 10 or the 25%  best images to Ball Detection.
                Object[] img_array = Images.values().toArray();
                java.util.Arrays.sort(img_array);
                ArrayList<ImageContainer> arrayI = new ArrayList<ImageContainer>();

                int nimgs = Math.round(Images.size() * 0.25f);
                nimgs = (nimgs < 10) ? 10 : nimgs;

                for (int i = 0; i < img_array.length && i < nimgs; i++) {
                    arrayI.add(i, (ImageContainer) img_array[i]);
                }

                //Adds this new area to the set.
                SELECTED_AREAS.add(area);
                //Sets the defualt options in ball detection options.
                cbSelectedBallArea.addItem(cbSelectedBallArea.getItemCount() + 1);
                cbSelectedBallArea.setSelectedIndex(cbSelectedBallArea.getItemCount() - 1);
                LP_CONF_USE_BINARIZE.setSelected(false);
                LP_CONF_USE_RED_HOUGH_CHECKBOX.setSelected(true);
                LPAlgorithmSelectionComboBox.setSelectedIndex(0);

                //Selects the best images.
                java.awt.Component[] c = jpSelectionThumbs.getComponents();
                for (int i = 0; i < c.length; i++) {
                    if (c[i] instanceof ImageThumbnailButton) {
                        ImageThumbnailButton isp = (ImageThumbnailButton) c[i];
                        Images.get(isp.getImageId()).setSelected_for_the_Area(area.getId(), arrayI.contains(Images.get(isp.getImageId())));
                        isp.setSelected(arrayI.contains(Images.get(isp.getImageId())));
                    }
                }
                jbDetectGlossySphere.setEnabled(true);
            }
	}//GEN-LAST:event_jbAddSelectionAreaActionPerformed

	private void jbDeleteSelectionAreaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDeleteSelectionAreaActionPerformed
            //Update of Selected areas upon a deletion of a area.  If the area 2 is eliminated the 3 will turn to 2 and take is place, same to the following.
            int selected = cbSelectedBallArea.getSelectedIndex();
            if (selected != -1) {

                Area_Selection_Panel.deleteSelection(selected);
                int itemCount = cbSelectedBallArea.getItemCount() - 1;
                cbSelectedBallArea.removeAllItems();

                int i = 0;
                for (; i < itemCount; i++) {
                    cbSelectedBallArea.insertItemAt(i + 1, i);
                }
                cbSelectedBallArea.setSelectedIndex(i - 1);
                SELECTED_AREAS.remove(selected);
                //When a change is made, you cannot advance in the program only through ball detection.
                //jbASNext.setEnabled(false);
            }
            if (cbSelectedBallArea.getItemCount() == 0) {
                jlSelectedArea.setText("No selected areas");
                jbDetectGlossySphere.setEnabled(false);
            }
}//GEN-LAST:event_jbDeleteSelectionAreaActionPerformed

	private void jsXStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jsXStateChanged
            //Changes the sphere x center cordinate from ball detection results form the selected area and in the panel.
            int selected_ball = jtBALL_DETECTION_RESULTS.getSelectedIndex();
            if (selected_ball != -1) {
                //New ball center to shown on panel
                float[] new_center = {((Integer) jsX.getValue()).floatValue(), ((Integer) jsY.getValue()).floatValue(), ((Integer) jsRadius.getValue()).floatValue()};
                this.SphereDetection_Preview_Panel.setCenter(new_center);
                this.SphereDetection_Preview_Panel.repaint();
            }
}//GEN-LAST:event_jsXStateChanged

	private void jsYStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jsYStateChanged
            //Changes the sphere y center cordinate from ball detection results in the panel, to confirm, set new ball center is needed.
            int selected_ball = jtBALL_DETECTION_RESULTS.getSelectedIndex();
            if (selected_ball != -1) {
                //New ball center to shown on panel
                float[] new_center = {((Integer) jsX.getValue()).floatValue(), ((Integer) jsY.getValue()).floatValue(), ((Integer) jsRadius.getValue()).floatValue()};
                this.SphereDetection_Preview_Panel.setCenter(new_center);
                this.SphereDetection_Preview_Panel.repaint();
            }
	}//GEN-LAST:event_jsYStateChanged

	private void jsRadiusStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jsRadiusStateChanged
            //Changes the sphere radius from ball detection results form the selected area and in the panel.
            int selected_ball = jtBALL_DETECTION_RESULTS.getSelectedIndex();
            if (selected_ball != -1) {
                //New ball center to shown on panel
                float[] new_center = {((Integer) jsX.getValue()).floatValue(), ((Integer) jsY.getValue()).floatValue(), ((Integer) jsRadius.getValue()).floatValue()};
                this.SphereDetection_Preview_Panel.setCenter(new_center);
                this.SphereDetection_Preview_Panel.repaint();
            }
}//GEN-LAST:event_jsRadiusStateChanged

	private void jsDBImageScaleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jsDBImageScaleStateChanged
            //Updates the image size on ball detection result panel with the new scale.
            SphereDetection_Preview_Panel.setImageScale((float) (jsDBImageScale.getValue()) / 100.0f);
            SphereDetection_Preview_Panel.repaint();
	}//GEN-LAST:event_jsDBImageScaleStateChanged

	private void jbSDBackPreviousPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSDBackPreviousPanelActionPerformed
            //Changes to the previous interface panel.
            CardLayout cl = (CardLayout) this.getLayout();
            cl.first(this);
}//GEN-LAST:event_jbSDBackPreviousPanelActionPerformed

	private void jbSetNewCenterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSetNewCenterActionPerformed
            //Set a new ball detection center
            int selected_ball = jtBALL_DETECTION_RESULTS.getSelectedIndex();
            if (selected_ball != -1) {
                //On the selected area store the new center, getting theinfo form the panel
                SelectedArea selected_area = SELECTED_AREAS.get(selected_ball);
                selected_area.setSphereDetection(SphereDetection_Preview_Panel.getCenter());

                //Get the spheres field on XMLCarrier
                RawInfo spheres_field = carrier.getComputedInfo("Spheres");
                //Get all thge spheres
                ArrayList<Info> spheres = spheres_field.getAllInnerInformation();

                //For all the spheres, select the one to be changed
                Info new_sphere = null;
                for (Info sphere : spheres) {
                    if (sphere.getAttribute("ID").equals(selected_area.getId().toString())) {
                        new_sphere = sphere;
                        break;
                    }
                }
                //Set the new data
                float x = selected_area.getSphereDetection()[0];
                float y = selected_area.getSphereDetection()[1];
                float r = selected_area.getSphereDetection()[2];

                new_sphere.addAttribute("x", Float.toString(x));
                new_sphere.addAttribute("y", Float.toString(y));
                new_sphere.addAttribute("r", Float.toString(r));

                carrier.modifyComputedInfo(spheres_field);

                //Regist event in the log area
                Event change_event = new Event();
                change_event.setText("In the sphere with the id " + selected_area.getId() + " the value was changed by the user to x: " + x + " y: " + y + " r: " + r);
                carrier.registEvent(change_event);

                //Set the carrier time stamp, to indicate action
                carrier.incTimestamp();

                //Write the xml
                try {
                    carrier.writeXML();
                } catch (Exception ex) {
                    Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
                }

                //Update the interface sphere coordinates
                this.jsX.setValue(new Integer(Math.round(x)));
                this.jsY.setValue(new Integer(Math.round(y)));
                this.jsRadius.setValue(new Integer(Math.round(r)));



            }
	}//GEN-LAST:event_jbSetNewCenterActionPerformed

	private void jbDetectGlossySphereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDetectGlossySphereActionPerformed
            //Execute Sphere Detection
            final ProgressBarPopup pbp = new ProgressBarPopup();
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    pbp.createAndShowGUI("Executing Sphere Detection");//, "Sphere Detection process is being executed. Please wait.");
                }
            });

            ExecuteSphereDetectionTask executeSphereDetectionTask = new ExecuteSphereDetectionTask(pbp);
            executeSphereDetectionTask.execute();

}//GEN-LAST:event_jbDetectGlossySphereActionPerformed

    public class ExecuteSphereDetectionTask extends SwingWorker<Void, Integer> {

        private ProgressBarPopup pbp = null;
        int error = NO_ERRORS;

        public ExecuteSphereDetectionTask(ProgressBarPopup pbp) {
            this.pbp = pbp;
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }

        @Override
        protected Void doInBackground() {
            error = executeSphereDetection();
            System.out.println("Error code on ExecuteSphereDetectionTask: "+error);
            return null;
        }

        @Override
        protected void done() {
            if (!isCancelled()) {
                pbp.close();
                setCursor(null);
                //Reset result if needed
                if (executed) {
                    resetOutput();
                }
                //Set results on the interface and XMLcarrier
                setResults();
            } else {
                System.out.println("Task cancelled");
            }

        }
    }

	private void jtBALL_DETECTION_RESULTSStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jtBALL_DETECTION_RESULTSStateChanged
            //When the another area is selected, the display panel must be updated
            // Get current tab
            int selected_sphere = jtBALL_DETECTION_RESULTS.getSelectedIndex();
            //If some tab is selected
            if (selected_sphere != -1) {
                //Set title on button panel according to the current selected area/sphere
                jlSelectedDetectedBall.setText(jtBALL_DETECTION_RESULTS.getTitleAt(selected_sphere));
                //Get the new image for the panel according to the type that was on display
                BufferedImage image = null;
                //Get the area
                Rectangle area = SELECTED_AREAS.get(selected_sphere).getSelectionArea();
                //If Edge
                if (current_selected_type.equals("Edge")) {
                    image = SELECTED_AREAS.get(selected_sphere).getEdge().getImage(true);
                } //Median
                else if (current_selected_type.equals("Median")) {
                    image = SELECTED_AREAS.get(selected_sphere).getMedian().getImage(true);
                } //Normal image
                else {
                    //Get the image crop
                    image = Images.get(current_selected_image).getImageCrop(area);
                }
                //Set image location on panel
                SphereDetection_Preview_Panel.setImageLocation(area.x, area.y);
                //Set image
                SphereDetection_Preview_Panel.setImage(image);
                //Set ball center and radius for the selected ball
                SphereDetection_Preview_Panel.setCenter(SELECTED_AREAS.get(selected_sphere).getSphereDetection());

                //Set sphere preview panel on the current tab
                ((javax.swing.JScrollPane) (jtBALL_DETECTION_RESULTS.getSelectedComponent())).setViewportView(SphereDetection_Preview_Panel);
                ((javax.swing.JScrollPane) (jtBALL_DETECTION_RESULTS.getSelectedComponent())).validate();
                SphereDetection_Preview_Panel.repaint();

                //Update the interface sphere coordinates
                jsX.setValue(new Integer(Math.round(SELECTED_AREAS.get(selected_sphere).getSphereDetection()[0])));
                jsY.setValue(new Integer(Math.round(SELECTED_AREAS.get(selected_sphere).getSphereDetection()[1])));
                jsRadius.setValue(new Integer(Math.round(SELECTED_AREAS.get(selected_sphere).getSphereDetection()[2])));
            }
	}//GEN-LAST:event_jtBALL_DETECTION_RESULTSStateChanged

	private void jbSDNextPreviousPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSDNextPreviousPanelActionPerformed
            //Change to result panel
            CardLayout cl = (CardLayout) this.getLayout();
            cl.last(this);

}//GEN-LAST:event_jbSDNextPreviousPanelActionPerformed

	private void delete_sphere_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delete_sphere_buttonActionPerformed
            int selected_sphere = jtBALL_DETECTION_RESULTS.getSelectedIndex();
            //If some tab is selected
            if (selected_sphere != -1 && jtBALL_DETECTION_RESULTS.getTabCount() != 1) {
                //Set other tab selected
                if (selected_sphere == 0) {
                    jtBALL_DETECTION_RESULTS.setSelectedIndex(1);
                } else {
                    jtBALL_DETECTION_RESULTS.setSelectedIndex(0);
                }


                SelectedArea sphere_to_remove = SELECTED_AREAS.get(selected_sphere);
                //Get sphere field from the carrier
                RawInfo sphere_info = carrier.getComputedInfo("Spheres");
                ArrayList<Info> spheres = sphere_info.getAllInnerInformation();
                ArrayList<Info> new_spheres = new ArrayList<Info>();
                //Get all the spheres except the one to be eliminated
                for (Info sphere : spheres) {
                    if (!sphere.getAttribute("ID").equals(sphere_to_remove.getId().toString())) {
                        new_spheres.add(sphere);
                    }
                }
                //Set the information on the carrier
                sphere_info.setNewInnerTags(new_spheres);
                carrier.modifyComputedInfo(sphere_info);

                //Set the carrier time stamp, to indicate action
                carrier.incTimestamp();

                //Regist event in the log area
                Event change_event = new Event();
                change_event.setText("The sphere with the id " + sphere_to_remove.getId() + " has been removed");
                carrier.registEvent(change_event);

                try {
                    carrier.writeXML();
                } catch (Exception ex) {
                    Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
                }

                //Disable the button if only one sphere is left
                if (jtBALL_DETECTION_RESULTS.getTabCount() == 1) {
                    delete_sphere_button.setEnabled(false);
                }

                //Remove the selected area
                cbSelectedBallArea.setSelectedIndex(selected_sphere);
                jbDeleteSelectionArea.doClick();

                //Remove the sphere form the panel
                jtBALL_DETECTION_RESULTS.remove(selected_sphere);
            }
	}//GEN-LAST:event_delete_sphere_buttonActionPerformed

    /**Sets information on XMLcarrier and executes the BallDetection*/
    private int executeSphereDetection() {
        try {
            //load XMLcarrier for timestamp proposes
            carrier.loadXML();
        } catch (XMLNotAvailable ex) {
            Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XSDCantValidadeXML ex) {
            Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Reset ERROR flag
        ERROR = NO_ERRORS;

        //If there is no selected areas.
        if (SELECTED_AREAS.size() == 0) {
            ERROR = USER_SELECT_AREAS;
            return ERROR;
        }

        //If there is a selected area with no pictures selected for that area.
        for (SelectedArea area : SELECTED_AREAS) {
            UUID area_id = area.getId();
            int selected_pics = 0;
            for (ImageContainer imgC : Images.values()) {
                if (imgC.is_selected_for_the_Area(area_id)) {
                    selected_pics++;
                }
            }
            if (selected_pics == 0) {
                ERROR = NO_SELECTED_PICS;
                return ERROR;
            }
        }

        // Alright, the process is a go!
        // Set all older processes to "OLD" status
        ArrayList<Process> pList = carrier.getAllProcesses();
        for (Process p : pList)
        {
            if (p.getType().equals("BALLDETECTION"))
                p.setStatus("OLD");
        }
        carrier.setProcesses(pList);


        //

        //Generate a random UUID for the ball detection process
        BDprocessID = UUID.randomUUID();
        //Generate the ball detection process field for process stage info
        StageInfo BDstageinfo = new StageInfo();
        //Add input and output fields
        BDprocessInputID = UUID.randomUUID();
        BDprocessOutputID = UUID.randomUUID();
        BDstageinfo.addInputRef("BDi", BDprocessInputID.toString());
        BDstageinfo.addOutputRef("BDo", BDprocessOutputID.toString());
        //Generate ball detecion process field and add it to XMLcarier
        Process BDprocess = new Process(BDprocessID.toString(), "STOPPED", "BALLDETECTION", "", "", Integer.toString(plugInfo.getStage()), BDstageinfo);
        carrier.addProcess(BDprocess);
        //New ball detection process
        SphereDetection ball_detection = new SphereDetection(XMLpath);
        //Get process module id
        BDprocess_moduleID = ball_detection.getId();
        //Set process on XMLcarrier
        carrier.setProcessComponentID(BDprocessID.toString(), BDprocess_moduleID.toString());
        //New data input
        Data BDinput = new Data(BDprocessInputID);

        //For all areas
        Iterator<SelectedArea> selectedAreaId = SELECTED_AREAS.iterator();
        while (selectedAreaId.hasNext()) {

            SelectedArea Selected_ball = selectedAreaId.next();
            //Generate a new area info field for XMLcarrier
            UUID areaImgsId = UUID.randomUUID();
            AreaInfo areaInfo = new AreaInfo(Selected_ball.getId(), areaImgsId);

            //Set area information
            //Set selected area, beginning and end points, coordinates and the shape of the areas.
            Rectangle rec = Selected_ball.getSelectionArea();
            ArrayList<Point2D.Float> coord = new ArrayList<Point2D.Float>();
            coord.add(new Point2D.Float((float) rec.x, (float) rec.y));
            coord.add(new Point2D.Float((float) rec.x, (float) (rec.y + rec.height)));
            coord.add(new Point2D.Float((float) (rec.x + rec.width), (float) (rec.y + rec.height)));
            coord.add(new Point2D.Float((float) (rec.x + rec.width), (float) rec.y));
            areaInfo.setBegin(new Point2D.Float((float) rec.x, (float) rec.y));
            areaInfo.setEnd(new Point2D.Float((float) rec.x + rec.width, (float) rec.y + rec.height));
            areaInfo.setCoords(coord);
            areaInfo.setShape("Rectangle");
            //Set ball detection algorithm inputs
            areaInfo.addAreaAttribute("algorithm", Selected_ball.getAlgorithm() + "");
            areaInfo.addAreaAttribute("hough", Selected_ball.getHough() + "");
            areaInfo.addAreaAttribute("binarize", Selected_ball.getBinarize() + "");
            //Set data to save process images
            areaInfo.addAreaAttribute("projectName", carrier.getProjectInfo().getProjectName());
            areaInfo.addAreaAttribute("projectPath", carrier.getProjectInfo().getParamterByName("ProjectPath"));
            //Add information to the ball detection input
            BDinput.addAreaInfo(areaInfo);

            //Set the images to be used in this area.
            ArrayList<UUID> areaImgsIds = new ArrayList<UUID>();
            for (ImageContainer img : Images.values()) {
                if (img.is_selected_for_the_Area(Selected_ball.getId())) {
                    areaImgsIds.add(img.getImage_id());
                }
            }
            try {
                //Add image file group to this area data
                carrier.addFileGroup(new XMLcarrier.FileGroup(areaImgsId, "Images for BallDetection", null, areaImgsIds));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //Add input to process
        BDinput.setName("BallDetectionInput");
        carrier.addAreaData(BDinput);
        //Write XMLcarrier
        try {
            carrier.writeXML();
        } catch (Exception e) {
            e.printStackTrace();
            ERROR = ERROR_ON_XMLCARRIER;
        }

        //Execute process
        Thread t = new Thread(ball_detection);
        t.start();
        try {
            t.join();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        return ERROR;
    }

    private void setResults()
    {
        setResults(false);
    }

    /**Retrives the Ball Detection output information from the  XMLcarrier and sets the interface panels*/
    private void setResults(boolean openingExisting) {

        if (this.ERROR == USER_SELECT_AREAS) {
            javax.swing.JOptionPane.showMessageDialog(null, "Select a area to for sphere detection.", "No selected areas", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (this.ERROR == NO_SELECTED_PICS) {
            javax.swing.JOptionPane.showMessageDialog(null, "There are no selected pictures for one of the areas.", "No selected pictures", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (this.ERROR == ERROR_ON_XMLCARRIER) {
            javax.swing.JOptionPane.showMessageDialog(null, "An error as occurred in output writing.", "Process Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Get a new carrier instance, to refresh the information
        carrier = new XMLHandler(XMLpath.toString());

        try {
            //load xml
            carrier.loadXML();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            //Get output and store the sphere data on the project description
            //get output
            Data BDresult = carrier.getDataByUUID(carrier.getOutputReferences(BDprocess_moduleID.toString()).get("BDo"));

            //Area number
            int area_number = 0;
            current_selected_image = Images.firstKey();
            current_selected_type = "image";

            //See if the spheres are already defined
            RawInfo Spheres = carrier.getComputedInfo("Spheres");
            boolean first_execution = (Spheres.getAllInnerInformation().isEmpty());

            //Create a field to store the spheres if none is already there
            if (first_execution) {
                Spheres = new RawInfo("Spheres");
            }
            //Create a ArrayList to store the new Spheres.
            ArrayList<Info> new_spheres = new ArrayList<Info>();

            //for all areas of the output, store the information from ball detection output
            for (AreaInfo ai : BDresult.getAreas()) {
                //get circle
                float sphere_coordinates[] = {-1.0f, -1.0f, -1.0f};
                sphere_coordinates[0] = Float.valueOf(ai.getAttribute("x"));
                sphere_coordinates[1] = Float.valueOf(ai.getAttribute("y"));
                sphere_coordinates[2] = Float.valueOf(ai.getAttribute("r"));
                //Get edge and median ids
                UUID edgeId = UUID.fromString(ai.getAttribute("EdgeID"));
                UUID medianId = UUID.fromString(ai.getAttribute("MedianID"));
                //Get edge and median images
                ImageFile median = carrier.getImageByUUID(medianId);
                ImageFile edge = carrier.getImageByUUID(edgeId);
                //Create a new image container for both the images and store their UUIDs
                String projectpath = carrier.getProjectInfo().getParamterByName("ProjectPath")+File.separator;
                //ImageContainer medianImgC = new ImageContainer(UUID.randomUUID(), new File(median.getUrl()));
                //ImageContainer edgeImgC = new ImageContainer(UUID.randomUUID(), new File(edge.getUrl()));
                ImageContainer medianImgC = new ImageContainer(UUID.randomUUID(), new File(carrier.getProjectInfo().getAssemblyFilesDirectory() + File.separator + (new File(median.getUrl())).getName()));
                ImageContainer edgeImgC = new ImageContainer(UUID.randomUUID(), new File(carrier.getProjectInfo().getAssemblyFilesDirectory() + File.separator + (new File(edge.getUrl())).getName()));
                medianImgC.setRelPath(projectpath);
                edgeImgC.setRelPath(projectpath);
                medianImgC.setImageId(UUID.fromString(median.getUuid()));
                edgeImgC.setImageId(UUID.fromString(edge.getUuid()));
                //Add images to the selected area
                UUID id_aux = ai.getAreaId();

                int sphere_index = 0;
                for (SelectedArea sa : SELECTED_AREAS) {
                    if (sa.getId().toString().equals(id_aux.toString())) {
                        sa.setSphereDetection(sphere_coordinates);
                        sa.setEdge(edgeImgC);
                        sa.setMedian(medianImgC);

                        Info sphere = new Info("sphere");
                        sphere.addAttribute("Order", Integer.toString(sphere_index));
                        sphere.addAttribute("ID", id_aux.toString());
                        sphere.addAttribute("x", Float.toString(sphere_coordinates[0]));
                        sphere.addAttribute("y", Float.toString(sphere_coordinates[1]));
                        sphere.addAttribute("r", Float.toString(sphere_coordinates[2]));
                        new_spheres.add(sphere);


                    }
                    sphere_index++;
                }




                //Add a new scroll panel to the ball detection result tabed panel.
                javax.swing.JScrollPane jsBallDetectionPreview = new javax.swing.JScrollPane();
                jsBallDetectionPreview.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                jsBallDetectionPreview.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                jtBALL_DETECTION_RESULTS.addTab("Sphere " + (area_number + 1), jsBallDetectionPreview);
                jtBALL_DETECTION_RESULTS.validate();

                //Update the interface sphere coordinates
                jsX.setValue(Math.round(sphere_coordinates[0]));
                jsY.setValue(Math.round(sphere_coordinates[1]));
                jsRadius.setValue(Math.round(sphere_coordinates[2]));
                area_number++;
            }

            Spheres.setNewInnerTags(new_spheres);
            if (first_execution) {
                carrier.addComputedInfo(Spheres);
            } else {
                carrier.modifyComputedInfo(Spheres);
            }

            if (!openingExisting)
            {
                //change carrier time stamp to sign the action
                // setTimestamp(2) was present to eliminate a crashing bug. NOTE
                // that this meant this plugin had to be on stage 2
                // Now, with plugInfo, it is possible to set on any stage.
                carrier.setTimestamp(this.plugInfo.getStage());
                carrier.incTimestamp();
                //save and load xml
                carrier.writeXML();
                carrier.loadXML();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //set buttons and listeners
        setButtons();
        setPanelListener();

        //The process is considered executed
        executed = true;

        //Initialize panel for image display trough a fake invocation
        jtBALL_DETECTION_RESULTSStateChanged(null);

        BallDetectionInterface aplication_interface = (BallDetectionInterface) this.getParent();
        aplication_interface.done();

        CardLayout cl = (CardLayout) this.getLayout();
        cl.last(this);
        jbSDNext.setVisible(true);
    }

    /**Set the interface result panel thumbnails */
    private void setButtons() {

        //If this is not the first execution
        if (!executed) {
            //For all images create thumbnail buttons for result display.
            for (UUID id : Images.keySet()) {
                //create button
                ImageThumbnailButton image_button = new ImageThumbnailButton(Images.get(id).getImage_name(), id);
                //Show only a label on the button
                image_button.setLabelMode();
                //Set icon
                image_button.setIcon(Images.get(id).getIcon());
                //Add image to panel
                jpSDThumbnails.add(image_button);
            }
        }

        String projectpath = this.carrier.getProjectInfo().getParamterByName("ProjectPath");

        //Set a edge thumbnail button and listener
        //create button
        ImageThumbnailButton edge_image_button = new ImageThumbnailButton("Edge", UUID.nameUUIDFromBytes("Edge".getBytes()));
        //Set image type, using the button information field
        edge_image_button.setInformation("Edge");
        //Show only a label on the button
        edge_image_button.setLabelMode();
        //Load the icon on the first area edge image
        SELECTED_AREAS.get(0).getEdge().loadIcon(projectpath);
        //Set icon
        edge_image_button.setIcon(SELECTED_AREAS.get(0).getEdge().getIcon());
        //Add image to panel
        jpSDThumbnails.add(edge_image_button);

        //Set a median thumbnail and listener
        //create button
        ImageThumbnailButton median_image_button = new ImageThumbnailButton("Median", UUID.nameUUIDFromBytes("Median".getBytes()));
        //Set image type, using the button information field
        median_image_button.setInformation("Median");
        //Show only a label on the button
        median_image_button.setLabelMode();
        //Load the icon on the first area median
        SELECTED_AREAS.get(0).getMedian().loadIcon(projectpath);
        //Set icon
        median_image_button.setIcon(SELECTED_AREAS.get(0).getMedian().getIcon());
        //Add image to panel
        jpSDThumbnails.add(median_image_button);

        //Define listeners for all buttons on result panel
        for (Component button : jpSDThumbnails.getComponents()) {
            ImageThumbnailButton thumbnail_button = (ImageThumbnailButton) button;
            thumbnail_button.setButtonListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    //Get source button
                    JButton button = (JButton) (e.getSource());
                    ImageThumbnailButton itb = (ImageThumbnailButton) button.getParent();

                    //Get the image for this button for the current selected area
                    int selected_area = jtBALL_DETECTION_RESULTS.getSelectedIndex();
                    //Get the area
                    Rectangle area = SELECTED_AREAS.get(selected_area).getSelectionArea();

                    //Discover what is the picture type and load it,setting the current type
                    BufferedImage image = null;
                    if (itb.getInformation().equals("Edge")) {
                        image = SELECTED_AREAS.get(selected_area).getEdge().getImage(true);
                        current_selected_type = "Edge";
                    } else if (itb.getInformation().equals("Median")) {
                        image = SELECTED_AREAS.get(selected_area).getMedian().getImage(true);
                        current_selected_type = "Median";
                    } else {
                        //Get the image crop
                        image = Images.get(itb.getImageId()).getImageCrop(area);
                        current_selected_type = "Image";
                    }
                    //Set current image id;
                    current_selected_image = itb.getImageId();
                    //Set image location on panel
                    SphereDetection_Preview_Panel.setImageLocation(area.x, area.y);
                    //Set image
                    SphereDetection_Preview_Panel.setImage(image);
                    //The ball selected in this moment, known through the result tabed panel
                    int current_selected_area = jtBALL_DETECTION_RESULTS.getSelectedIndex();
                    //Set ball center and radius for the selected ball
                    SphereDetection_Preview_Panel.setCenter(SELECTED_AREAS.get(current_selected_area).getSphereDetection());
                    SphereDetection_Preview_Panel.repaint();

                    //Update the interface sphere coordinates
                    jsX.setValue(new Integer(Math.round(SELECTED_AREAS.get(current_selected_area).getSphereDetection()[0])));
                    jsY.setValue(new Integer(Math.round(SELECTED_AREAS.get(current_selected_area).getSphereDetection()[1])));
                    jsRadius.setValue(new Integer(Math.round(SELECTED_AREAS.get(current_selected_area).getSphereDetection()[2])));
                }
            });
        }

        edge_image_button.getImageButton().doClick();
        //Allow sphere deletion if there is more than one
        delete_sphere_button.setEnabled(jtBALL_DETECTION_RESULTS.getTabCount() > 1);

    }

    /**Sets a listener on result preview panel, to update the interface on real time on user action*/
    private void setPanelListener() {
        SphereDetection_Preview_Panel.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                //Get the current sphere location on the panel
                float[] sphere_detection = SphereDetection_Preview_Panel.getCenter();
                //Update the interface
                jsX.setValue(Math.round(sphere_detection[0]));
                jsY.setValue(Math.round(sphere_detection[1]));
                jsRadius.setValue(Math.round(sphere_detection[2]));
            }
        });
    }

    /**Method used to reset the output module in case of a new excution*/
    public void resetOutput() {
        //Reset error status
        ERROR = NO_ERRORS;

        //Remove the edge and median buttons
        //Get button number
        int number = jpSDThumbnails.getComponents().length;
        jpSDThumbnails.remove(number - 1);
        jpSDThumbnails.remove(number - 2);

        //Remove result panels
        jtBALL_DETECTION_RESULTS.removeAll();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox LPAlgorithmSelectionComboBox;
    private javax.swing.JLabel LPGlossyBallLabel;
    private javax.swing.JPanel LPOOptionPanel;
    private javax.swing.JCheckBox LP_CONF_USE_BINARIZE;
    private javax.swing.JCheckBox LP_CONF_USE_RED_HOUGH_CHECKBOX;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox cbSelectedBallArea;
    private javax.swing.JButton delete_sphere_button;
    private javax.swing.JPanel inputPanel;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane4;
    private javax.swing.JSplitPane jSplitPane6;
    private javax.swing.JButton jbAddSelectionArea;
    private javax.swing.JButton jbDeleteSelectionArea;
    private javax.swing.JButton jbDetectGlossySphere;
    private javax.swing.JButton jbSDBack;
    private javax.swing.JButton jbSDNext;
    private javax.swing.JButton jbSetNewCenter;
    private javax.swing.JLabel jlSelectedArea;
    private javax.swing.JLabel jlSelectedDetectedBall;
    private javax.swing.JPanel jpOpenOptions;
    private javax.swing.JPanel jpSDThumbnails;
    private javax.swing.JPanel jpSelectionThumbs;
    private javax.swing.JSlider jsDBImageScale;
    private javax.swing.JSpinner jsRadius;
    private javax.swing.JSpinner jsX;
    private javax.swing.JSpinner jsY;
    private javax.swing.JTabbedPane jtBALL_DETECTION_RESULTS;
    private javax.swing.JPanel outputPanel;
    // End of variables declaration//GEN-END:variables
}
