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
import Exceptions.ArgumentException;
import Exceptions.ModuleException;
import Exceptions.XMLcarrierException;
import ModuleInterfaces.RTIBuilderInterface;
import ModuleInterfaces.UserInteractionInterface;
import Plugin.helpers.FileManipulation;
import Plugin.helpers.ImageContainer;
import Plugin.helpers.InputReader;
import Plugin.helpers.Pair;
import Plugin.helpers.RowEditableTableModel;
import XMLcarrier.Event;
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
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.UUID;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import operations.DefaultProjectImagesOpenTask;
import operations.OpenDataTask;

/**
 *
 * @author rcosta
 */
public class PluginPanelModelController {


    ///***Plugin variables***\\\
    /**The properties table model.<br>
     * This is based on a a specific model that allows the author and project name to be changed, and new properties can be added.<p>
     * @see RowEditableTableModel
     */
    private RowEditableTableModel properTable_model;
    /** User choice: 0 = load folder ; 1 = load lp file; 2 = load dome lp. */
    int user_open_choice;

    /**A reader class with methods to read images from a folder or file*/
    private InputReader reader;
    /**The XML carrier that contains all the information relative to the project.<br>
     * This carrier is used to store this information and comunicate with other services<p>
    @see XMLHandler
     */
    private XMLHandler carrier = null;
    /**The project Images file group, to avoid orphaned file groups*/
    private FileGroup ImagesFileGroup = null;

    ///****Project variables****\\\
    /**XMLCarrier path*/
    private StringBuffer XMLpath = null;
    /** Image size. Width and height*/
    private int FULL_IMG_SIZE[] = new int[2];
    /** Unique project identifier.*/
    private UUID ID;
    /** Points to the current selected image folder*/
    private File current_selected_folder;
    /** Holds current Image Files in Current Folder. This is a set of ImageContainer maped by their unique identifier.
    @see lptracker.helpers.ImageContainer*/
    private TreeMap<UUID, ImageContainer> ProjectImages;
    /** Project creation date.*/
    private String CREATION_DATE;
    /** Output Filename defined by project name*/
    private String OUT_Filename = "LPtracker_development";
    /**Current selected picture*/
    private ImageThumbnailButton selected_image;

    private UUID lastCache = null;

    ///***Folder options***\\\
    private String OriginalCapturesDirectory = "original-captures";
    private String AssemblyFilesDirectory = "assembly-files";
    private String JpegExportsDirectory = "jpeg-exports";
    private String FinishedFilesDirectory = "finished-files";
    private String CroppedDir = "cropped-files";


    public PluginPanelModelController(IPluginPanelView parent, StringBuffer XMLurl, int user_option) throws ArgumentException, ModuleException, XMLcarrierException
    // <editor-fold defaultstate="collapsed" desc="Beta code">
    {
        // set user choice as opening a folder;
        user_open_choice = user_option;

        final IPluginPanelView parentWindow = parent;

        //Set the project file group null
        ImagesFileGroup = null;
        XMLpath = XMLurl;
        //Instanciate a new XmlHandler from the given path
        carrier = new XMLHandler(XMLpath.toString());
        try {
            //Load XMLcarrier
            carrier.loadXML();
        } catch (XMLNotAvailable ex) {
            Logger.getLogger(PluginPanelView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XSDCantValidadeXML ex) {
            Logger.getLogger(PluginPanelView.class.getName()).log(Level.SEVERE, null, ex);
        }

        /*current_selected_folder = new File(carrier.getProjectInfo().getParamterByName("ProjectPath"));
        if (!current_selected_folder.exists())
        {
            current_selected_folder = null;
        }*/

        File tempf = new File(XMLpath.toString());
        //if (current_selected_folder == null)
        //{
            current_selected_folder = tempf.getParentFile();
        //}


        if (lastCache == null)
        {
            lastCache = UUID.randomUUID();
        }

        // Check if we are opening an existing Project File.
        // If we are, that is most likely because there are images in the
        // passed XML file.
        // In that case, use the alternate initialization code path
        if (!carrier.getImageList().isEmpty())
        {
            // Image list ISN'T EMPTY
            // Thus, we must load them!
            System.out.println("There are images in the xml file, must load them...");

            ArrayList<FileGroup> groups = carrier.getAllFileGroups();

            FileGroup oldFG = ImagesFileGroup;


            // Get the relevant file group
            for(FileGroup fg : groups)
            {
                if (fg.getUse().equalsIgnoreCase("Original image files"))
                {
                    this.ImagesFileGroup = fg;
                }
            }

            boolean needImageRefresh = true;
            if (oldFG!=null)
                needImageRefresh = !ImagesFileGroup.getId().equals(oldFG.getId());

            // Now, we must interpret the FileGroup's data and read the image files
            ArrayList<ImageFile> imgList = ImagesFileGroup.getList();
            //ArrayList<UUID> uuidList = ImagesFileGroup.getRefList();

            // -----
            boolean useCached = false;
            if (parentWindow.getDataCache()!=null &&
                parentWindow.getDataCache().get("Cached Original Thumbs")!=null &&
                lastCache != parentWindow.getDataCache().getID()) useCached = true;

            if (parentWindow.getDataCache()!=null) lastCache = parentWindow.getDataCache().getID();

            // EXPERIMENTAL CODE
            if(imgList.size()>0)// && needImageRefresh)
            {
                TreeMap<UUID,BufferedImage> imgMap2 = useCached ?
                    (TreeMap<UUID,BufferedImage>)parentWindow.getDataCache().get("Cached Original Thumbs")
                    : new TreeMap();
                //DefaultProjectImagesOpenTask task = new DefaultProjectImagesOpenTask(true);

                // --- EXPERIENCE -- FAILED
/*
                final ProgressBarPopup pbp = new ProgressBarPopup(0,ImagesFileGroup.getRefList().size());

                Runnable pbpR = new Runnable() {

                    public void run() {
                        pbp.createAndShowGUI("Loading project images...");
                    }
                };

                SwingUtilities.invokeLater(pbpR);
*/
                // ---

                final String tmppath = this.carrier.getProjectInfo().getParamterByName("ProjectPath") + File.separator;//this.current_selected_folder;
                OpenDataTask tasker = new OpenDataTask(new DefaultProjectImagesOpenTask(true, useCached)
                {
                    IPluginPanelView parentW = null;
                    String projectPath = tmppath+File.separator;


                    {
                        parentW = parentWindow;
                        this.relativePathModifier = tmppath;
                    }

                    @Override
                    public void taskOnDone() {
                        synchronized(this) {
                        // Initialize thumbnail panel as a callback...

                        // Now, we must interpret the FileGroup's data and read the image files
                        ArrayList<ImageFile> imgList = this.fGroup.getList();
                        //ArrayList<UUID> uuidList = this.fGroup.getRefList();

                        TreeMap<UUID,BufferedImage> m = this.getData();

                        System.out.println("Number of images read by experimental code: " + m.size());

                        System.out.println("Finished execution of experimental");
                        //needImageRefresh = false;

                        TreeMap<UUID, ImageContainer> imgMapTransformed = new TreeMap();

                        int counter = 0;
                        //for (UUID imageID : uuidList) {
                        for (ImageFile file : imgList) {
                            UUID imageID = UUID.fromString(file.getUuid());
                            BufferedImage img = m.get(imageID); //m.get(imageID);
                            // Convert BufferedImage list into ImageContainer list
                            int iw = this.getWidth(); // Must pass original sizes, not thumb sizes //img.getWidth();
                            int ih = this.getHeight(); //img.getHeight();
                            String ipath = imgList.get(counter).getUrl();
                            System.out.println("Converting to ImageContainer("+counter+"): "+imageID+", "+ipath);
                            File iFile = new File(projectPath+ipath);// new File(projectPath+ipath);
                            ImageContainer imgC = new ImageContainer(imageID, iFile, img, ih, iw);


                            imgMapTransformed.put(imageID, imgC);

                            //public ImageContainer(UUID image_id, File image_file, BufferedImage thumb, int height, int width) {
                            counter++;
                        }

                        //this.ProjectImages = imgMap;
                        System.out.println("Final images: " + imgMapTransformed.toString());
                        System.out.flush();
                        parentW.setProjectImages(imgMapTransformed);
                        //if (parentW.getDataCache()!= null)
                        {
                        System.out.println("PluginPanelModelController: Cache in use ID:"+parentW.getDataCache().getID());
                        parentW.getDataCache().remove("Cached Original Thumbs");
                        parentW.getDataCache().put("Cached Original Thumbs", m);
                        }
                    }
                        parentW.initPreviewMenu(false);
                    }
                }
                , ImagesFileGroup, imgMap2, ImagesFileGroup.getList().size() ,"Loading project images...", "Image being loaded: "//, pbp
                );

                tasker.execute();

                System.out.println("Carrying on...");
                needImageRefresh = false;
            }
            // -----





            if(imgList.size()>0 && needImageRefresh)
            {
                // Initialize a progress bar pop-up
                final ProgressBarPopup pbp = new ProgressBarPopup(0, imgList.size());

                Runnable pbpR = new Runnable() {

                    public void run() {
                        pbp.createAndShowGUI("Loading images");
                    }
                };

                // Launch it
                //SwingUtilities.invokeLater(pbpR);
                Thread auxThread = new Thread(pbpR);
                auxThread.start();

                TreeMap<UUID,ImageContainer> imgMap = new TreeMap();

                int i = 0;
                for (ImageFile imgF : imgList)
                {
                    ImageContainer imgContainer = null;
                    UUID id = UUID.fromString(imgF.getUuid());//uuidList.get(i);

                    File imageFile = new File(imgF.getUrl());
                    if (imageFile!=null && imageFile.isFile())
                    {
                        imgContainer = new ImageContainer(id,imageFile);
                        imgMap.put(id, imgContainer);
                        /*if (current_selected_folder == null)
                        {
                            current_selected_folder = imageFile.getParentFile().getParentFile();
                        }*/
                    }

                    i++;
                    pbp.setProgress(i);
                }

                pbp.close();
                try {
                    auxThread.join(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PluginPanelModelController.class.getName()).log(Level.SEVERE, null, ex);
                }

                this.ProjectImages = imgMap;
                //current_selected_folder = ;

            }

        }

        //Load folder options
        LoadOptionsFile();

        //Create a new input reader
        reader = new InputReader(parent);

        properTable_model = new RowEditableTableModel(5, 2, 3, 0, new int[][]{{3, 0}, {4, 0}});

        HeaderInfo header = carrier.getProjectInfo();

        //Filling the properties table. Image sizes are not yet known, so not filled
        properTable_model.setValueAt("Project UUID", 0, 0);
        properTable_model.setValueAt(header.getUuid(), 0, 1);
        properTable_model.setValueAt("Image width", 1, 0);
        //properTable_model.setValueAt(Float.toString(width), 1, 1);
        properTable_model.setValueAt("Image height", 2, 0);
        //properTable_model.setValueAt(Float.toString(height), 2, 1);
        properTable_model.setValueAt("Author", 3, 0);
        properTable_model.setValueAt(header.getAuthor(), 3, 1);
        properTable_model.setValueAt("Project name", 4, 0);
        properTable_model.setValueAt(header.getProjectName(), 4, 1);


    }
    // </editor-fold>

    void initPreviewMenuModel(IPluginPanelView parent, boolean addLPinfo)
    // <editor-fold defaultstate="collapsed" desc="Beta code">
    {

        System.out.println("Clean project codepath");

        float height = ProjectImages.get(ProjectImages.firstKey()).getHeight();
        float width = ProjectImages.get(ProjectImages.firstKey()).getWidth();
        FULL_IMG_SIZE[0] = (int) width;
        FULL_IMG_SIZE[1] = (int) height;

        String user = System.getProperties().getProperty("user.name");
        //ID = UUID.randomUUID();
        //File xmlcarrier = new File(XMLpath.toString());
        //String filename = FileManipulation.getSimpleName(xmlcarrier);
        //String project_name = filename;

        //Filling the properties table.
        //properTable_model.setValueAt("Project UUID", 0, 0);
        //properTable_model.setValueAt(ID.toString(), 0, 1);
        properTable_model.setValueAt("Image width", 1, 0);
        properTable_model.setValueAt(Float.toString(width), 1, 1);
        properTable_model.setValueAt("Image height", 2, 0);
        properTable_model.setValueAt(Float.toString(height), 2, 1);
        //properTable_model.setValueAt("Author", 3, 0);
        properTable_model.setValueAt(user, 3, 1);
        //properTable_model.setValueAt("Project name", 4, 0);
        //properTable_model.setValueAt(project_name, 4, 1);

        initProcessPipeline(parent, addLPinfo);

    }
    // </editor-fold>

    /*
    void initPreviewMenuModel(IPluginPanelView parent, XMLHandler carrier)
    {
        System.out.println("Alternate codepath reached!");
    }*/

    void initProcessPipeline(IPluginPanelView oParent, boolean addLPinfo)
    // <editor-fold defaultstate="collapsed" desc="Beta code">
    {
        //Write header
        try {
            carrier.loadXML();
        } catch (XMLNotAvailable ex) {
            Logger.getLogger(PluginPanelView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XSDCantValidadeXML ex) {
            Logger.getLogger(PluginPanelView.class.getName()).log(Level.SEVERE, null, ex);
        }
        int timeStamp = carrier.getTimestamp();
        writeHeader();

        File xmlcarrier = new File(XMLpath.toString().trim());
        StringBuffer newXMLpath = new StringBuffer(carrier.getProjectInfo().getParamterByName("ProjectPath").trim() + File.separator + xmlcarrier.getName());
        this.XMLpath = newXMLpath;
        carrier.setXmlPath(XMLpath.toString());

        //Set the xml carrier in the correct folder
        UserInteractionInterface parent = (UserInteractionInterface) oParent.getParent();
        if (parent instanceof PreviewDomeLPInterface) {
            PreviewDomeLPInterface pdlp = (PreviewDomeLPInterface) parent;
            pdlp.parent.setXMLcarrier(XMLpath);
        }
        if (parent instanceof PreviewFolderInterface) {
            PreviewFolderInterface pdlp = (PreviewFolderInterface) parent;
            pdlp.parent.setXMLcarrier(XMLpath);
        }
        if (parent instanceof PreviewLPInterface) {
            PreviewLPInterface pdlp = (PreviewLPInterface) parent;
            pdlp.parent.setXMLcarrier(XMLpath);

        }

        //Add in the XMLcarrier a ComputedData entry for the project images
        RawInfo images = new RawInfo("Images");

        //First file group
        if (ImagesFileGroup == null) {
            UUID fileG_id = UUID.randomUUID();
            XMLcarrier.FileGroup fileG = new XMLcarrier.FileGroup(fileG_id, "Original image files");
            try {
                for (ImageContainer img : ProjectImages.values()) {
                    //fileG.addImageFile(new ImageFile("mimetype", img.getImageFile().getAbsolutePath(), img.getImageId().toString(), ""));
                    fileG.addImageFile(new ImageFile("mimetype", this.JpegExportsDirectory + File.separator + img.getImageFile().getName(), img.getImageId().toString(), ""));
                    fileG.addRef(img.getImageId());
                }
                carrier.addFileGroup(fileG);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ImagesFileGroup = fileG;

            images.addAttribute("ID", fileG_id.toString());
            carrier.addComputedInfo(images);
        } else {
            ArrayList<ImageFile> imageFiles = new ArrayList<ImageFile>();
            for (ImageContainer img : ProjectImages.values()) {
                imageFiles.add(new ImageFile("mimetype", this.JpegExportsDirectory + File.separator + img.getImageFile().getName(), img.getImageId().toString(), ""));
            }
            try {
                carrier.alterFileGrp(ImagesFileGroup.getId(), imageFiles);
            } catch (UUIDNotFound ex) {
                Logger.getLogger(PluginPanelView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        //If the user has chosen to open a lp
        // This probably should have a guard against opening project fiçe and adding again... have to check how to do it
        if ((user_open_choice == IPluginPanelView.OPEN_FILE_LP || user_open_choice == IPluginPanelView.OPEN_DOME_LP) && addLPinfo)
                //&& carrier.getComputedInfo("LightDirections").getAttribute("ImageID") == null) {
        {
            RawInfo light_directions = new RawInfo("LightDirections");
            //For all images store this information  in the Compute data field
            for (ImageContainer image : ProjectImages.values()) {
                //Get the light direction values and store them
                float[] ld_values = image.getLp();
                Info ldirection = new Info("lightdirection");
                ldirection.addAttribute("ImageID", image.getImageId().toString());
                ldirection.addAttribute("x", Float.toString(ld_values[0]));
                ldirection.addAttribute("y", Float.toString(ld_values[1]));
                ldirection.addAttribute("z", Float.toString(ld_values[2]));
                light_directions.addInnerTag(ldirection);
            }
            carrier.addComputedInfo(light_directions);
        }

        //Write header
        writeHeader();
        carrier.setTimestamp(timeStamp);
        try {
            carrier.writeXML();
        } catch (Exception ex) {
            Logger.getLogger(PluginPanelView.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    // </editor-fold>

    /**Set header info*/
    void writeHeader() 
    // <editor-fold defaultstate="collapsed" desc="Beta code, Tested">
    {
        try {
            carrier.writeXML();
        } catch (Exception ex) {
            Logger.getLogger(PluginPanelView.class.getName()).log(Level.SEVERE, null, ex);
        }

        OUT_Filename = (String) properTable_model.getValueAt(4, 1);
        HeaderInfo header = new HeaderInfo();
        header.setAuthor((String) properTable_model.getValueAt(3, 1));
        CREATION_DATE = carrier.generateDate();
        header.setCreationDate(CREATION_DATE);
        header.setHost(carrier.generateHost());
        header.setLastModDate(carrier.generateDate());
        header.setMemoryAvailable(carrier.generateMemInfo());
        header.setOperatingSystem(carrier.generateOSVersion());
        header.setProcessorInfo(carrier.generateProcessorInfo());
        //change
        header.setProjectName(OUT_Filename);
        header.setUserInfo(carrier.generateUserInfo());
        header.setUuid((String) properTable_model.getValueAt(0, 1));
        for (int z = 5; z < properTable_model.getRowCount(); z++) {
            header.addParameter((String) properTable_model.getValueAt(z, 0), (String) properTable_model.getValueAt(z, 1));
        }
        header.addParameter((String) properTable_model.getValueAt(1, 0), (String) properTable_model.getValueAt(1, 1));
        header.addParameter((String) properTable_model.getValueAt(2, 0), (String) properTable_model.getValueAt(2, 1));
        //change
        header.addParameter("ProjectPath", current_selected_folder.getAbsolutePath());
        //Set folders
        header.addOriginalCaptureDirectory(OriginalCapturesDirectory);
        header.addJpegExportsDirectory(JpegExportsDirectory);
        header.addAssemblyFilesDirectory(AssemblyFilesDirectory);
        header.addFinishedFilesDirectory(FinishedFilesDirectory);
        header.addCroppedDir(CroppedDir);

//			if(chosen_dng!=-1)
//					  header.addParameter("DNG image", Integer.toString(chosen_dng));
        //Set the timestamp, where, if not defined, the getTimestamp method returns 0,
        System.out.println("TimeStamp on preview header" + carrier.getTimestamp());
        int timeStamp = carrier.getTimestamp();
        //carrier.setTimestamp(timeStamp);
        carrier.setHeaderInfo(header);
        carrier.setTimestamp(timeStamp);

        try {
            carrier.writeXML();
        } catch (Exception ex) {
            Logger.getLogger(PluginPanelView.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
    // </editor-fold>

    boolean LoadOptionsFile() 
    // <editor-fold defaultstate="collapsed" desc="Original code">
    {
        try {
            java.io.InputStream propIn = new java.io.FileInputStream(new File("Plugins" + File.separator + "Preview" + File.separator + "preview.conf"));
            java.util.Properties loader = new Properties();
            loader.loadFromXML(propIn);
            boolean integrity = true;
            String result = null;
            result = loader.getProperty("originalCaptures");
            integrity = result != null && integrity;
            if (integrity) {
                OriginalCapturesDirectory = loader.getProperty("originalCaptures");
            }
            result = loader.getProperty("assemblyFiles");
            integrity = result != null && integrity;
            if (integrity) {
                AssemblyFilesDirectory = loader.getProperty("assemblyFiles");
            }
            result = loader.getProperty("jpegExports");
            integrity = result != null && integrity;
            if (integrity) {
                JpegExportsDirectory = loader.getProperty("jpegExports");
            }
            result = loader.getProperty("finishedFiles");
            integrity = result != null && integrity;
            if (integrity) {
                FinishedFilesDirectory = loader.getProperty("finishedFiles");
            }
            result = loader.getProperty("croppedDir");
            integrity = result != null && integrity;
            if (integrity) {
                CroppedDir = loader.getProperty("croppedDir");
            }
            propIn.close();
            if (!integrity) {
                SaveOptionsFile();
                LoadOptionsFile();
            }
            return true;
        } catch (java.io.FileNotFoundException fne) {
            javax.swing.JOptionPane.showMessageDialog(null, "File not found, default are assumed!", "Properties file not found.", javax.swing.JOptionPane.WARNING_MESSAGE);
            SaveOptionsFile();
        } catch (java.io.IOException ioe) {
            javax.swing.JOptionPane.showMessageDialog(null, "", "Error on properties files.", javax.swing.JOptionPane.WARNING_MESSAGE);
        }
        return false;
    }
    // </editor-fold>

    
    boolean SaveOptionsFile()
    // <editor-fold defaultstate="collapsed" desc="Original code">
    {
        try {
            java.io.OutputStream propOut = new java.io.FileOutputStream(new File("Plugins" + File.separator + "Preview" + File.separator + "preview.conf"));
            java.util.Properties loader = new Properties();

            loader.setProperty("originalCaptures", OriginalCapturesDirectory);
            loader.setProperty("assemblyFiles", AssemblyFilesDirectory);
            loader.setProperty("jpegExports", JpegExportsDirectory);
            loader.setProperty("finishedFiles", FinishedFilesDirectory);
            loader.setProperty("croppedDir", CroppedDir);
            loader.storeToXML(propOut, "Preview Plugin v1.0");
            propOut.flush();
            propOut.close();
            return true;
        } catch (java.io.FileNotFoundException fne) {
            javax.swing.JOptionPane.showMessageDialog(null, "File in error", "Unabled to save the propertie file", javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (java.io.IOException ioe) {
            javax.swing.JOptionPane.showMessageDialog(null, "File in error", "Unabled to save the propertie file", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    // </editor-fold>

    /**
     * Counts numbers of lines in file.
     *
     * @param file
     * @return
     */
    public int numberLinesFile(File file)
    // <editor-fold defaultstate="collapsed" desc="Final code">
    {
        int count = -1;
        if (file.exists()) {
            FileReader fr = null;
            count = 0;
            try {
                fr = new FileReader(file);
                LineNumberReader ln = new LineNumberReader(fr);
                while (ln.readLine() != null) {
                    count++;
                }
                ln.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PluginPanelView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    fr.close();
                } catch (IOException ex) {
                    Logger.getLogger(PluginPanelView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } else {
            System.out.println("File does not exists!");
        }

        return count;
    }
    // </editor-fold>

    /**
     * Adds/finds a new editable line.
     *
     * @return A Pair object with a Boolean denoting whether or not it found a
     * free row (created one if not), and a row index of the editable line. May
     * return null if there are no project images.
     */
    public Pair<Boolean,Integer> addNewEditableRow()
    // <editor-fold defaultstate="collapsed" desc="Beta code, Tested">
    {
        boolean free_row = false;
        //  Opens a new editable line . Inserts a new row if needed.
        if (ProjectImages != null && !ProjectImages.isEmpty()) {
            int prop_rows = 0;
            int table_rows = properTable_model.getRowCount();
            free_row = false;
            while (prop_rows < table_rows && !free_row) {
                if (properTable_model.getValueAt(prop_rows, 0).equals("") || properTable_model.getValueAt(prop_rows, 0).equals("<Propertie>")) {
                    free_row = true;
                }
                prop_rows++;
            }

            if (!free_row)
            {
                properTable_model.addRow(new String[]{"<Property>", "<Value>"});
            }
            else
            {
                prop_rows--;
            }
            return new Pair(free_row,prop_rows);
        }
        return null;
    }
    // </editor-fold>

    /**
     * Remove row from property table model
     *
     * Row must be valid (prints stack trace if it is not)
     *
     * @param row Row index to remove
     */
    public void removeRow(int row)
    // <editor-fold defaultstate="collapsed" desc="Final code">
    {
        try {
            this.properTable_model.removeRow(row);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    // </editor-fold>

    /**
     * Returns the user choice upon creating the data model
     * 
     * @return Integer representing the choice
     */
    public int getOpenChoice()
    // <editor-fold defaultstate="collapsed" desc="Final code">
    {
        return this.user_open_choice;
    }
    // </editor-fold>

    /**
     * Gets value at cell indicated from property table model
     *
     * @param row
     * @param column
     * @return
     */
    public Object getValueAt(int row, int column)
    // <editor-fold defaultstate="collapsed" desc="Final code">
    {
        return this.properTable_model.getValueAt(row, column);
    }
    // </editor-fold>

    /**
     * Get the InputReader belonging to the data model
     *
     * @return
     */
    public InputReader getReader()
    // <editor-fold defaultstate="collapsed" desc="Final code">
    {
        return reader;
    }
    // </editor-fold>

    /**
     * Return whether or not there is an image currently selected
     *
     * @return True if there is an image selected
     */
    public boolean hasSelectedImage()
    // <editor-fold defaultstate="collapsed" desc="Final code">
    {
        return ( selected_image != null );
    }
    // </editor-fold>

    /**
     * Returns the selected image.
     *
     * Returns null if there is none.
     *
     * @return
     */
    public ImageThumbnailButton getSelectedImage()
    // <editor-fold defaultstate="collapsed" desc="Final code">
    {
        return selected_image;
    }
    // </editor-fold>

    /**
     * Remove the selected image from the project (selected via thumbnails)
     *
     * @param reason Reason provided for the removal.
     */
    public void removeSelectedImage(String reason)
    // <editor-fold defaultstate="collapsed" desc="Beta code, Tested">
    {
        // Log removal event
        String[] removed_element = {selected_image.getPictureName(), reason};
        Event e = new Event(CREATION_DATE, "", UUID.randomUUID(), Level.INFO.toString(), "User", "Removed picture with id: " + selected_image.getImageId().toString() + " with name " + removed_element[0] + " for the reason: " + removed_element[1]);
        carrier.registEvent(e);

        // Remove image from project images
        // Start by seeking image... and omgscarycode behind this. May touch later!
        RawInfo images_info = carrier.getComputedInfo("Images");
        UUID images_id = UUID.fromString(images_info.getAttribute("ID"));
        try {
            ArrayList<ImageFile> images_list = carrier.getImageList(images_id);
            ImageFile image_to_remove = null;
            for (ImageFile imgF : images_list) {
                UUID image_id = UUID.fromString(imgF.getUuid());
                if (image_id.equals(selected_image.getImageId())) {
                    image_to_remove = imgF;
                    break;
                }
            }
            images_list.remove(image_to_remove);
            carrier.alterFileGrp(images_id, images_list);
            carrier.writeXML();
        } catch (UUIDNotFound ex) {
            Logger.getLogger(PluginPanelView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PluginPanelView.class.getName()).log(Level.SEVERE, null, ex);
        }

        ProjectImages.remove(selected_image.getImageId());



    }
    // </editor-fold>

    /**
     * Get an iterator for the Project Images' data structure.
     * 
     * @return
     */
    public Iterator<ImageContainer> getProjectImagesIterator()
    // <editor-fold defaultstate="collapsed" desc="Final code">
    {
        return ProjectImages.values().iterator();
    }
    // </editor-fold>

    public BufferedImage getBufferedImageByID(UUID id)
    {
        return ProjectImages.get(id).getImage(); // this.current_selected_folder.getAbsolutePath()+File.separator
    }

    RowEditableTableModel getTableModel()
    {
        return this.properTable_model;
    }

    XMLHandler getXMLHandler()
    {
        return this.carrier;
    }

    File getCurrentSelectedFolder()
    {
        return this.current_selected_folder;
    }

    void setCurrentSelectedFolder(File folder)
    {
        this.current_selected_folder = folder;
    }

    TreeMap<UUID, ImageContainer> getProjectImages()
    {
        return this.ProjectImages;
    }

    synchronized void setProjectImages(TreeMap<UUID, ImageContainer> images)
    {
        this.ProjectImages = images;
    }

    void setSelectedImage(ImageThumbnailButton image)
    {
        this.selected_image = image;
    }

    String getImageExportsDir()
    {
        return this.JpegExportsDirectory;
    }
}
