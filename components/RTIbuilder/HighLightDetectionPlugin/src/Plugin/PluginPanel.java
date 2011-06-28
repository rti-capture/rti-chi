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
import ModuleInterfaces.PluginMetaInfo;
import Plugin.helpers.ImageContainer;
import Plugin.helpers.Sphere;
import XMLcarrier.AreaInfo;
import XMLcarrier.Data;
import XMLcarrier.Event;
import XMLcarrier.Exceptions.UUIDNotFound;
import XMLcarrier.Exceptions.UnknownProcessID;
import XMLcarrier.Exceptions.XMLNotAvailable;
import XMLcarrier.Exceptions.XSDCantValidadeXML;
import XMLcarrier.ImageFile;
import XMLcarrier.Info;
import XMLcarrier.StageInfo;
import XMLcarrier.XMLHandler;
import XMLcarrier.Process;
import XMLcarrier.RawInfo;
import guicomponents.HighLightPanel;
import guicomponents.ImageThumbnailButton;
import guicomponents.ProgressBarPopup;
import guicomponents.SpherePanel;
import highlightdetect.HighlightDetect;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
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
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import operations.DefaultProjectImagesOpenTask;
import operations.OpenDataTask;

public class PluginPanel extends javax.swing.JPanel {

	//***XMLcarrier variables***//
	/**XMLCarrier path*/
	private StringBuffer XMLpath = null;
	/**XMLcarrier variable for data edition*/
	private XMLHandler carrier;
	//*** Interface variables ***//
	/**Sphere and image preview panel*/
	private SpherePanel sphere_preview_panel;
	/**HighLight panel*/
	private HighLightPanel highlight_panel;
	/**Current selected picture in the preview area**/
	private UUID current_selected_picture_on_preview;
	/**Current selected picture in the output area**/
	private UUID current_selected_picture_on_output;
	/**Flags if the output selected iamge is the blend image*/
	private boolean output_image_is_blend = false;
	/**Flags if the highlight process has been executed*/
	private boolean executed = false;
	/***Process information***/
	/**No errors*/
	private final int NO_ERRORS = 0;
	/**Indicates the existence of a sphere with no selected images*/
	private final int NO_IMAGES = 1;
	/**Flags an error on HighLight process**/
	private int ERROR = NO_ERRORS;
	/**HighLight process id*/
	private UUID HLTDprocessModuleId;
	//***Project information***//
	/**Plugin images*/
	private TreeMap<UUID, ImageContainer> Images;
	/**Plugin spheres**/
	private ArrayList<Sphere> SPHERES;

        public iDataCache cache;
        public PluginMetaInfo plugInfo;

	/**Creates new form GUIteste**/
	public PluginPanel() {
		initComponents();
	}

	/**Method responsible for image initialization*/
	public void start(StringBuffer XMLurl) throws ArgumentException, ModuleException, XMLcarrierException {

		//reset interface
		resetInterface();
		//set the panels
		sphere_preview_panel = new SpherePanel();
		highlight_panel = new HighLightPanel();
		//Turn the panel not editable
		sphere_preview_panel.setSphereEditable(false);
		XMLpath = XMLurl;

		//Info extracted from the carrier
		RawInfo InputData = null;

		//Instanciate a new XmlHandler from the given path
		carrier = new XMLHandler(XMLpath.toString());
		//Get all images
		try {
			carrier.loadXML();
		} catch (Exception e) {
			throw new XMLcarrierException("Error when opening the XMLcarrier", e);
		}

		//Get image section
		InputData = carrier.getComputedInfo("Images");

		//Get the images id string, and if not available throw a exception
		String imagesId = InputData.getAttribute("ID");

		if (imagesId == null) {
			throw new ArgumentException("Input not present, the image set is not defined!");
		}

		//Get image group id
		UUID imagegroup_id = UUID.fromString(imagesId);

		//Get the image width and height from XMlcarrier if present
		int width = -1;
		int height = -1;
		String whidthS = carrier.getProjectInfo().getParamterByName("Image width");
		String heightS = carrier.getProjectInfo().getParamterByName("Image height");
		if (whidthS != null && heightS != null) {
			width = (int) Float.parseFloat(whidthS);
			height = (int) Float.parseFloat(heightS);
		}

                String projectpath = carrier.getProjectInfo().getParamterByName("ProjectPath") + File.separator;
		try {
			//Get all images for process execution
			ArrayList<ImageFile> image_files = carrier.getImageList(imagegroup_id);

			//Load all images
			Images = new TreeMap<UUID, ImageContainer>();
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

		//Get sphere information
		InputData = carrier.getComputedInfo("Spheres");

		//Set the initial selected picture
		current_selected_picture_on_preview = Images.firstKey();
		//The spheres are areas inside a Data field
		ArrayList<Info> raw_spheres = InputData.getAllInnerInformation();

		//Test if the input is available
		if (raw_spheres.isEmpty()) {
			throw new ArgumentException("Input not present, there are no spheres in the XMLcarrier!");
		}

		//Make the spheres appear in the proper order
		int sphere_number = raw_spheres.size();
		System.out.println("Sphere number: "+sphere_number);
		TreeMap<Integer,Info> spheres = new TreeMap<Integer,Info>();
		for (Info sphere : raw_spheres) {
			System.out.println("Adding sphere number: "+sphere.getAttribute("Order"));
			int index = Integer.parseInt(sphere.getAttribute("Order"));
			spheres.put(index, sphere);
		}
		sphere_number = 0;
		for (int sphere_index: spheres.keySet()) {
			Info sphere = spheres.get(sphere_index);
			float[] sphere_coordinates = new float[3];
			sphere_coordinates[0] = Float.parseFloat(sphere.getAttribute("x"));
			sphere_coordinates[1] = Float.parseFloat(sphere.getAttribute("y"));
			sphere_coordinates[2] = Float.parseFloat(sphere.getAttribute("r"));

			//Create a new sphere and add it
			Sphere new_sphere = new Sphere(sphere_coordinates, UUID.fromString(sphere.getAttribute("ID")));
			SPHERES.add(new_sphere);
			//For the sphere, all the images are by default selected for the Highlight process
			for (ImageContainer imgContainer : Images.values()) {
				imgContainer.setSelected_for_the_Sphere(new_sphere.getId(), true);
			}
			//add a new scroll panel to the sphere preview tabed pane
			JScrollPane sphere_scroll_panel = new JScrollPane();
			sphere_scroll_panel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			sphere_scroll_panel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jtSPHERE_COORDINATES.addTab("Sphere " + (sphere_number+1), sphere_scroll_panel);
			jtSPHERE_COORDINATES.validate();
			sphere_number++;
		}
		//Set the advance button disable
		go_to_output_button.setVisible(false);

            // Deal with thumbnails (which sadly have to be done asynchronously)
            final PluginPanel thisW = this;
            //TreeMap<UUID, BufferedImage> imgMap = new TreeMap();

            boolean useCached = false;
            if (cache != null && cache.get("Cached Original Thumbs") != null) {
                useCached = true;
            }
            //lastCache != parentWindow.getDataCache().getID()) useCached = true;

            TreeMap<UUID, BufferedImage> imgMap = useCached
                    ? (TreeMap<UUID, BufferedImage>) cache.get("Cached Original Thumbs")
                    : new TreeMap();      

            //String projectpath = carrier.getProjectInfo().getParamterByName("ProjectPath") + File.separator;
            OpenDataTask tasker = new OpenDataTask(new DefaultProjectImagesOpenTask(true, useCached, projectpath) {

                PluginPanel parentW = null;

                {
                    parentW = thisW;
                }

                @Override
                public void taskOnDone() {
                    //synchronized (this) {
                    {
                        // Initialize thumbnail panel as a callback...

                        TreeMap<UUID, BufferedImage> m = this.getData();

                        System.out.println("Number of images read by experimental code: " + m.size());

                        System.out.flush();
                        {
                            System.out.println("Highlight Detection: Cache in use ID:" + parentW.cache.getID());
                            parentW.cache.remove("Cached Original Thumbs");
                            parentW.cache.put("Cached Original Thumbs", m);
                        }
                    }
                    parentW.setThumbnails(imgMap);

                    HLTDprocessModuleId = UUID.nameUUIDFromBytes(HighlightDetect.XML_VERSION_TAG.getBytes());
                try {
                    TreeMap<String, String> list = carrier.getOutputReferences(HLTDprocessModuleId.toString());
                    if ( list != null && !list.isEmpty()) {
                        //System.out.println(list.toString());
                        parentW.setResults();
                    }
                } catch (UnknownProcessID ex) {
                    // Do nothing, this is normal
                    System.out.println("Highlight Detection: Project does not have output yet.");
                }

                    parentW.validate();
                }
            }, carrier.getFileGroup(imagegroup_id), imgMap, carrier.getFileGroup(imagegroup_id).getList().size(), "Loading project images...", "Image being loaded: "//, pbp
                    );

            tasker.execute();

	}

	/**Reset interface variables*/
	private void resetInterface() {
		//Reset variables
		ERROR = NO_ERRORS;
		executed = false;
		//Remove thumbnails
		jpSThumbnails.removeAll();
		jpHLTThumbnails.removeAll();
		//remove results and spheres
		jtHLTD_RESULTS.removeAll();
		jtSPHERE_COORDINATES.removeAll();
		//Remove all spheres
		SPHERES = new ArrayList<Sphere>();
		//Make sure the panel that is shown is the first one
		CardLayout cl = (CardLayout) this.getLayout();
		cl.first(this);
		//Disable the next button
		go_to_output_button.setVisible(false);
	}

	public void setThumbnails(Map<UUID, BufferedImage> thumbnails) {
		//for all thumbnails
		for (UUID image_id : thumbnails.keySet()) {
			//Get the associated image container
			ImageContainer image_container = Images.get(image_id);
			//Load icon
			image_container.loadIcon(thumbnails.get(image_id));
			//Create a new thumbnail button
			ImageThumbnailButton button = new ImageThumbnailButton(image_container.getImageName(), image_container.getImageId());
			//Set icon.
			button.setIcon(thumbnails.get(image_id));
			//Add to panel
			jpSThumbnails.add(button);
			//Create a listener to the button
			button.setButtonListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					//Get source button
					JButton button = (JButton) (e.getSource());
					ImageThumbnailButton itb = (ImageThumbnailButton) button.getParent();
					//Get the image id for this button
					current_selected_picture_on_preview = itb.getImageId();
					//Get current Sphere
					int selected_sphere = jtSPHERE_COORDINATES.getSelectedIndex();
					//Get the Sphere
					Sphere sphere = SPHERES.get(selected_sphere);
					//Get spheres coordinates
					float[] coordinates = sphere.getSphereCoordinates();
					//Crop the picture around the picture
					BufferedImage image = Images.get(current_selected_picture_on_preview).getImageCropFromSphere(coordinates, 10);
					//Set the image location
					sphere_preview_panel.setImageLocation(Math.round(coordinates[0] - coordinates[2]) - 10, Math.round(coordinates[1] - coordinates[2]) - 10);
					sphere_preview_panel.setImage(image);
					sphere_preview_panel.repaint();
				}
			});
			//Set the checkbox selected
			button.getCheckBox().setSelected(true);
			button.setCheckBoxListener(new ActionListener() {
				//When clicked the check box defines the image has selected or not for the process
				public void actionPerformed(ActionEvent e) {
					int selected_sphere = jtSPHERE_COORDINATES.getSelectedIndex();
					if (selected_sphere != -1) {
						JCheckBox selection = (JCheckBox) e.getSource();
						ImageThumbnailButton isp = (ImageThumbnailButton) selection.getParent().getParent();
						Images.get(isp.getImageId()).setSelected_for_the_Sphere(SPHERES.get(selected_sphere).getId(), selection.isSelected());
					}
				}
			});
		}
		//click on the first button
		((ImageThumbnailButton) jpSThumbnails.getComponent(0)).getImageButton().doClick();
	}

	/**Load inputs to XMLcarrier and execute the HighLight process*/
	private int executeHighlightDetection() {

		try {
			//load XMLcarrier for timestamp proposes
			carrier.loadXML();
		} catch (XMLNotAvailable ex) {
			Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
		} catch (XSDCantValidadeXML ex) {
			Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
		}

		//Reset Error flag
		this.ERROR = NO_ERRORS;

		//Check for situations where there are no selected pictures for one sphere
		Iterator<Sphere> selected_sphere = SPHERES.iterator();
		boolean no_imgs = false;
		//For all the spheres, until nothing wrong is found
		while (selected_sphere.hasNext() && !(!no_imgs)) {

			Sphere sphere = selected_sphere.next();
			int imgs_in_sphere = 0;
			//check what is the number of selected pictures
			for (ImageContainer pic : Images.values()) {
				if (pic.is_selected_for_the_Sphere(sphere.getId())) {
					imgs_in_sphere++;
				}
			}
			//If equals to zero, set the flag
			if (imgs_in_sphere == 0) {
				no_imgs = true;
			}
		}
		//Emit a warnig
		if (no_imgs) {
			int answer = 0;
			answer = JOptionPane.showConfirmDialog(null, (String) "One or more spheres don't have selected images for the HighLight process. Do you whish to continue?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (answer == JOptionPane.NO_OPTION) {
				this.ERROR = NO_IMAGES;
				return this.ERROR;
			}
		}

		//Generate a id for HLTD process
		UUID HLTprocessID = UUID.randomUUID();
		//Generate a Stage info for the process
		StageInfo HLTDstageinfo = new StageInfo();
		//Generate the input and output ids
		UUID HLTDprocessInputID = UUID.randomUUID();
		UUID HLTDprocessOutputID = UUID.randomUUID();
		//Add the input and output ids
		HLTDstageinfo.addInputRef("HLi", HLTDprocessInputID.toString());
		HLTDstageinfo.addOutputRef("HLo", HLTDprocessOutputID.toString());
		//Create process and add it to the XMLcarrier
		Process HLTDprocess = new Process(HLTprocessID.toString(), "STOPPED", "HLTDETECTION", "", "", Integer.toString(this.plugInfo.getStage()), HLTDstageinfo);
		carrier.addProcess(HLTDprocess);
		//New HighLight Detection process
		HighlightDetect HLTD = new HighlightDetect(XMLpath);
		//Get HLTD module id
		HLTDprocessModuleId = HLTD.getId();
		//Set it on XMLcarrier
		carrier.setProcessComponentID(HLTprocessID.toString(), HLTDprocessModuleId.toString());


		//New Data for input
		Data HLTinput = new Data(HLTDprocessInputID);
		//Set process input for all the spheres
		Iterator<Sphere> it = SPHERES.iterator();
		while (it.hasNext()) {
			//Get sphere
			Sphere sphere = it.next();
			UUID areaImgsId = UUID.randomUUID();
			AreaInfo areaInfo = new AreaInfo(sphere.getId(), areaImgsId);
			//Define the area around the sphere
			Rectangle rec = Sphere.getRectanglefromSphere(sphere.getSphereCoordinates(), 0);
			ArrayList<Point2D.Float> coord = new ArrayList<Point2D.Float>();
			coord.add(new Point2D.Float((float) rec.x, (float) rec.y));
			coord.add(new Point2D.Float((float) rec.x, (float) (rec.y + rec.height)));
			coord.add(new Point2D.Float((float) (rec.x + rec.width), (float) (rec.y + rec.height)));
			coord.add(new Point2D.Float((float) (rec.x + rec.width), (float) rec.y));
			areaInfo.setBegin(new Point2D.Float((float) rec.x, (float) rec.y));
			areaInfo.setEnd(new Point2D.Float((float) rec.x + rec.width, (float) rec.y + rec.height));
			areaInfo.setCoords(coord);
			areaInfo.setShape("Rectangle");
			//Set sphere coordinates
			float xyr[] = sphere.getSphereCoordinates();
			areaInfo.addAreaAttribute("x", xyr[0] + "");
			areaInfo.addAreaAttribute("y", xyr[1] + "");
			areaInfo.addAreaAttribute("r", xyr[2] + "");
			//Add process options
			areaInfo.addAreaAttribute("threshold", Integer.toString(jsUserThreshold.getValue()));
			areaInfo.addAreaAttribute("projectName", carrier.getProjectInfo().getProjectName());
			areaInfo.addAreaAttribute("projectPath", carrier.getProjectInfo().getParamterByName("ProjectPath"));
			HLTinput.addAreaInfo(areaInfo);

			//Set the selected images for this sphere
			ArrayList<UUID> areaImgsIds = new ArrayList<UUID>();
			for (ImageContainer img : Images.values()) {
				if (img.is_selected_for_the_Sphere(sphere.getId())) {
					areaImgsIds.add(img.getImageId());
				}
			}
			try {
				carrier.addFileGroup(new XMLcarrier.FileGroup(areaImgsId, "Images for HighDetection", null, areaImgsIds));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//Add data
		carrier.addAreaData(HLTinput);

		//Write XML and execute.
		try {
			carrier.writeXML();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Thread t = new Thread(HLTD);
		t.start();
		try {
			t.join();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}

		return ERROR;
	}

	/**Displays the HLTD process results */
	private void setResults() {

		//If the user chose not to run the process
		if (this.ERROR == NO_IMAGES) {
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
			//Get data result
			Data HLTresultData = carrier.getDataByUUID(carrier.getOutputReferences(HLTDprocessModuleId.toString()).get("HLo"));
			//Get the compute information field to store the highlight values, if it doesn't existe create a new one.
			ArrayList<RawInfo> highlightsInfos = carrier.getComputedInfos("HighLights");
			//See if this is the first execution
			boolean first_execution = highlightsInfos.isEmpty();
			//Create or reset if isn't empty
			highlightsInfos = new ArrayList<RawInfo>();

			int sphere_number = 1;
			for (AreaInfo ai : HLTresultData.getAreas()) {
				//For all the spheres

				//Get the sphere/area id
				UUID id_aux = ai.getAreaId();
				//Create a new RawInfo to store the information about the highlights in this sphere.
				RawInfo SphereHighLights = new RawInfo("HighLights");
				//List to store the highlights
				ArrayList<Info> highlights = new ArrayList<Info>();
				SphereHighLights.addAttribute("SphereID", id_aux.toString());


				Sphere sphere = null;
				for (Sphere sa : SPHERES) {
					if (sa.getId().equals(id_aux)) {
						sphere = sa;
					}
				}

				UUID HLTdId = UUID.fromString(ai.getAttribute("Ref_HighLights"));
				Data HLd = carrier.getDataByUUID(HLTdId.toString());
				int numImgs = (Integer) HLd.getParameter("numBalls").getValue();

				//Get blend image
				UUID blendId = UUID.fromString(ai.getAttribute("BlendID"));
				ImageFile blend = carrier.getImageByUUID(blendId);

				ImageContainer blendImgC = new ImageContainer(UUID.fromString(blend.getUuid()), new File(blend.getUrl()));
				sphere.setBlend(blendImgC);

				//Set the highlight and create a Info camp to store the highlight infomation
				for (int j = 1; j <= numImgs; j++) {
					Info highlight = new Info("highlight");
					//Get data
					float[] center = {-1.0f, -1.0f, 0.0f};
					center[0] = ((Float) HLd.getParameter("x" + j).getValue());
					center[1] = ((Float) HLd.getParameter("y" + j).getValue());
					UUID imgId = (UUID) HLd.getParameter("img" + j).getValue();
					//Set on info
					highlight.addAttribute("ImageID", imgId.toString());
					highlight.addAttribute("x", Float.toString(center[0]));
					highlight.addAttribute("y", Float.toString(center[1]));
					//add it to the parent
					highlights.add(highlight);
					//Set info in the Image container
					Images.get(imgId).setHighlight(sphere.getId(), center);
				}

				//Add info to this sphere highlight info
				SphereHighLights.setNewInnerTags(highlights);
				//Add to the HighLight info
				highlightsInfos.add(SphereHighLights);

				//Set image for output display
				current_selected_picture_on_output = Images.lastKey();
				javax.swing.JScrollPane jsBallDetectionPreview = new javax.swing.JScrollPane();
				jsBallDetectionPreview.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				jsBallDetectionPreview.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				jtHLTD_RESULTS.addTab("Sphere " + sphere_number, jsBallDetectionPreview);
				jtHLTD_RESULTS.validate();
				sphere_number++;
			}

			//If first execution
			if (first_execution) {
				//Add info fields to the carrier
				carrier.addComputedInfo(highlightsInfos);
			} else {
				//Add the new information
				carrier.modifyComputedInfo(highlightsInfos);
			}

			//save and load xml
			carrier.writeXML();
			carrier.loadXML();
		} catch (Exception e) {
			e.printStackTrace();
		}

		setButtons();
		HighLightDetectionInterface parent = (HighLightDetectionInterface) this.getParent();
		parent.done();
	}

	public void setButtons() {

		//For all images create thumbnail buttons for result display.
		if (!executed) {
			for (UUID id : Images.keySet()) {
				//create button
				ImageThumbnailButton image_button = new ImageThumbnailButton(Images.get(id).getImageName(), id);
				//Show only a label on the button
				image_button.setLabelMode();
				//Set icon
				image_button.setIcon(Images.get(id).getIcon());
				//Add image to panel
				jpHLTThumbnails.add(image_button);
			}
		}
		//Create the thumbnail for the blend image
		//Create a ThumbnailButton from the first sphere
		ImageThumbnailButton blendButton = new ImageThumbnailButton("Blend", UUID.randomUUID());
		//Put the image type on the Thumbnail information camp
		blendButton.setInformation("Blend");
		//Set the button on label mode
		blendButton.setLabelMode();
		//Get the first blend icon and set the icon
                SPHERES.get(0).getBlend().setRelPath(carrier.getProjectInfo().getParamterByName("ProjectPath"));
		SPHERES.get(0).getBlend().loadIcon(true);
		blendButton.setIcon(SPHERES.get(0).getBlend().getIcon());
		//Add it to the panel
		jpHLTThumbnails.add(blendButton);

		//Define listeners for all buttons on result panel
		for (Component button : jpHLTThumbnails.getComponents()) {
			ImageThumbnailButton thumbnail_button = (ImageThumbnailButton) button;
			thumbnail_button.setButtonListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					//Get source button
					JButton button = (JButton) (e.getSource());
					ImageThumbnailButton itb = (ImageThumbnailButton) button.getParent();

					//Get the image for this button for the current selected area
					int selected_sphere = jtHLTD_RESULTS.getSelectedIndex();
					//Get the area
					float[] coordinates = SPHERES.get(selected_sphere).getSphereCoordinates();
					Rectangle area = Sphere.getRectanglefromSphere(coordinates, 10);
					//Discover what is the picture type and load it,setting the current type
					BufferedImage image = null;
					float[] hlt = null;
					if (itb.getInformation().equals("Blend")) {

						image = SPHERES.get(selected_sphere).getBlend().getImage(true);
						output_image_is_blend = true;
					} else {
						output_image_is_blend = false;

						//Get the image crop
						image = Images.get(itb.getImageId()).getImageCropFromSphere(coordinates, 10);
						hlt = Images.get(itb.getImageId()).getHighlight(SPHERES.get(selected_sphere).getId());
					}
					//Set current image id;
					current_selected_picture_on_output = itb.getImageId();
					//Set image location on panel
					highlight_panel.setImageLocation(area.x, area.y);
					//Set image
					highlight_panel.setImage(image);

					if (output_image_is_blend) {
						highlight_panel.HighLight_Not_Defined(false);
						highlight_panel.HighLight_Defined_By_User(false);
						highlight_panel.enableHighlightDraw(false);
					} else {
						highlight_panel.enableHighlightDraw(true);
						if (hlt == null) {
							//If not available, warn the user
							highlight_panel.HighLight_Not_Defined(true);
							highlight_panel.HighLight_Defined_By_User(false);
							//Set a visible highlight so the user can define one
						} else {
							//Set highlight if available
							highlight_panel.HighLight_Not_Defined(false);
							//warn if the highlight is user defined
							boolean is_user_defined = Images.get(current_selected_picture_on_output).Is_user_defined(SPHERES.get(selected_sphere).getId());
							highlight_panel.HighLight_Defined_By_User(is_user_defined);
							highlight_panel.setHighLight(hlt);
						}
					}
					highlight_panel.repaint();

				}
			});
		}
		//Set the go to output enable
		go_to_output_button.setVisible(true);
		if(jpHLTThumbnails.getComponents().length>0){
			ImageThumbnailButton thumbnail_button = (ImageThumbnailButton) jpHLTThumbnails.getComponents()[0];
			thumbnail_button.getImageButton().doClick();
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        input_option_panel = new javax.swing.JPanel();
        jbDetectHighlights = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jsUserThreshold = new javax.swing.JSlider();
        go_to_output_button = new javax.swing.JButton();
        jSplitPane2 = new javax.swing.JSplitPane();
        jtSPHERE_COORDINATES = new javax.swing.JTabbedPane();
        jSplitPane6 = new javax.swing.JSplitPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jpSThumbnails = new javax.swing.JPanel();
        jlSelectedSphere = new javax.swing.JLabel();
        jpHighlight = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        Redo_process_button = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jsDHLTImageScale = new javax.swing.JSlider();
        jb_redefine_highlight = new javax.swing.JButton();
        jSplitPane3 = new javax.swing.JSplitPane();
        jtHLTD_RESULTS = new javax.swing.JTabbedPane();
        jSplitPane7 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jpHLTThumbnails = new javax.swing.JPanel();
        jlSelectedHTBall = new javax.swing.JLabel();

        setLayout(new java.awt.CardLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        jbDetectHighlights.setText("Highlight detection");
        jbDetectHighlights.setMaximumSize(new java.awt.Dimension(107, 23));
        jbDetectHighlights.setMinimumSize(new java.awt.Dimension(107, 23));
        jbDetectHighlights.setPreferredSize(new java.awt.Dimension(107, 23));
        jbDetectHighlights.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDetectHighlightsActionPerformed(evt);
            }
        });

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("User Highlight Threshold"));

        jsUserThreshold.setMajorTickSpacing(25);
        jsUserThreshold.setMaximum(250);
        jsUserThreshold.setMinimum(10);
        jsUserThreshold.setMinorTickSpacing(5);
        jsUserThreshold.setPaintLabels(true);
        jsUserThreshold.setPaintTicks(true);
        jsUserThreshold.setToolTipText("Define user threshold for highlight detection");
        jsUserThreshold.setValue(180);

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jsUserThreshold, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 314, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(jsUserThreshold, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                .addContainerGap())
        );

        go_to_output_button.setText("Go to Output ->");
        go_to_output_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                go_to_output_buttonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout input_option_panelLayout = new org.jdesktop.layout.GroupLayout(input_option_panel);
        input_option_panel.setLayout(input_option_panelLayout);
        input_option_panelLayout.setHorizontalGroup(
            input_option_panelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(input_option_panelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 485, Short.MAX_VALUE)
                .add(input_option_panelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(go_to_output_button)
                    .add(jbDetectHighlights, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 190, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        input_option_panelLayout.setVerticalGroup(
            input_option_panelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(input_option_panelLayout.createSequentialGroup()
                .add(input_option_panelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, input_option_panelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(input_option_panelLayout.createSequentialGroup()
                        .add(19, 19, 19)
                        .add(jbDetectHighlights, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 39, Short.MAX_VALUE)
                        .add(go_to_output_button)))
                .addContainerGap())
        );

        jPanel1.add(input_option_panel, java.awt.BorderLayout.PAGE_END);

        jSplitPane2.setDividerLocation(500);

        jtSPHERE_COORDINATES.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jtSPHERE_COORDINATESStateChanged(evt);
            }
        });
        jSplitPane2.setLeftComponent(jtSPHERE_COORDINATES);

        jSplitPane6.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jpSThumbnails.setLayout(new java.awt.GridLayout(0, 6));
        jScrollPane3.setViewportView(jpSThumbnails);

        jSplitPane6.setRightComponent(jScrollPane3);

        jlSelectedSphere.setFont(new java.awt.Font("Verdana", 1, 14));
        jlSelectedSphere.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlSelectedSphere.setMaximumSize(new java.awt.Dimension(20, 20));
        jlSelectedSphere.setMinimumSize(new java.awt.Dimension(20, 20));
        jSplitPane6.setLeftComponent(jlSelectedSphere);

        jSplitPane2.setRightComponent(jSplitPane6);

        jPanel1.add(jSplitPane2, java.awt.BorderLayout.CENTER);

        add(jPanel1, "card2");

        jpHighlight.setLayout(new java.awt.BorderLayout());

        Redo_process_button.setText("<- Redo Process");
        Redo_process_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Redo_process_buttonPreviousPanelActionPerformed(evt);
            }
        });

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Image Scale"));

        jsDHLTImageScale.setMajorTickSpacing(25);
        jsDHLTImageScale.setMaximum(200);
        jsDHLTImageScale.setMinimum(10);
        jsDHLTImageScale.setMinorTickSpacing(5);
        jsDHLTImageScale.setPaintLabels(true);
        jsDHLTImageScale.setPaintTicks(true);
        jsDHLTImageScale.setValue(100);
        jsDHLTImageScale.setValueIsAdjusting(true);
        jsDHLTImageScale.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jsDHLTImageScaleStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel9Layout = new org.jdesktop.layout.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .add(jsDHLTImageScale, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel9Layout.createSequentialGroup()
                .add(jsDHLTImageScale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jb_redefine_highlight.setText("Redefine Highlight");
        jb_redefine_highlight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jb_redefine_highlightActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 571, Short.MAX_VALUE)
                        .add(Redo_process_button)
                        .add(7, 7, 7))
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(3, 3, 3)
                        .add(jb_redefine_highlight)
                        .addContainerGap())))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap(22, Short.MAX_VALUE)
                .add(jPanel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .add(29, 29, 29)
                .add(jb_redefine_highlight)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 38, Short.MAX_VALUE)
                .add(Redo_process_button, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(8, 8, 8))
        );

        jpHighlight.add(jPanel3, java.awt.BorderLayout.SOUTH);

        jSplitPane3.setDividerLocation(400);

        jtHLTD_RESULTS.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jtHLTD_RESULTSStateChanged(evt);
            }
        });
        jSplitPane3.setLeftComponent(jtHLTD_RESULTS);

        jSplitPane7.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jpHLTThumbnails.setLayout(new java.awt.GridLayout(0, 6));
        jScrollPane1.setViewportView(jpHLTThumbnails);

        jSplitPane7.setRightComponent(jScrollPane1);

        jlSelectedHTBall.setFont(new java.awt.Font("Verdana", 1, 14));
        jlSelectedHTBall.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlSelectedHTBall.setMaximumSize(new java.awt.Dimension(20, 20));
        jlSelectedHTBall.setMinimumSize(new java.awt.Dimension(20, 20));
        jSplitPane7.setLeftComponent(jlSelectedHTBall);

        jSplitPane3.setRightComponent(jSplitPane7);

        jpHighlight.add(jSplitPane3, java.awt.BorderLayout.CENTER);

        add(jpHighlight, "card6");
    }// </editor-fold>//GEN-END:initComponents

	  private void Redo_process_buttonPreviousPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Redo_process_buttonPreviousPanelActionPerformed
		  //Changes to the previous interface panel.
		  CardLayout cl = (CardLayout) this.getLayout();
		  cl.first(this);

}//GEN-LAST:event_Redo_process_buttonPreviousPanelActionPerformed

	private void jsDHLTImageScaleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jsDHLTImageScaleStateChanged
		//Updates the image size in the highlight result panel from the scale choosen by the user.
		this.highlight_panel.setImageScale((float) (jsDHLTImageScale.getValue()) / 100.0f);
		this.highlight_panel.repaint();
}//GEN-LAST:event_jsDHLTImageScaleStateChanged

	private void jbDetectHighlightsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDetectHighlightsActionPerformed

		final ProgressBarPopup pbp = new ProgressBarPopup();
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				pbp.createAndShowGUI("Executing HighLight Detection");//, "Sphere Detection process is being executed. Please wait.");
			}
		});

		ExecuteHighlightDetectionTask executeHighlightDetectionTask = new ExecuteHighlightDetectionTask(pbp, this);
		executeHighlightDetectionTask.execute();


		if (executed) {
			resetOutput();
		}
//		setResults();
//		executed = true;
//
//		//Set the carrier time stamp, to indicate action
//		carrier.incTimestamp();
//		try {
//			carrier.writeXML();
//		} catch (Exception ex) {
//			Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
//		}
//		CardLayout cl = (CardLayout) this.getLayout();
//		cl.next(this);
		// TODO add your handling code here:
	}//GEN-LAST:event_jbDetectHighlightsActionPerformed

	public class ExecuteHighlightDetectionTask extends SwingWorker<Void, Integer> {

		private ProgressBarPopup pbp = null;
		private Container cont = null;

		public ExecuteHighlightDetectionTask(ProgressBarPopup pbp, Container cont) {
			this.pbp = pbp;
			this.cont = cont;
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}

		@Override
		protected Void doInBackground() {
			executeHighlightDetection();
			return null;
		}

		@Override
		protected void done() {
			if (!isCancelled()) {
				pbp.close();
				setCursor(null);
				if (executed) {
					resetOutput();
				}
				setResults();
				executed = true;

				//Set the carrier time stamp, to indicate action
                                carrier.setTimestamp(plugInfo.getStage());
				carrier.incTimestamp();
				try {
					carrier.writeXML();
				} catch (Exception ex) {
					Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
				}
				CardLayout cl = (CardLayout) cont.getLayout();
				cl.next(cont);
			} else {
				System.out.println("Task cancelled");
			}

		}
	}

	private void jtSPHERE_COORDINATESStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jtSPHERE_COORDINATESStateChanged
		//When another sphere is selected, other image is needed and the preview panel must be updated
		//Get selected sphere
		int selected_sphere = jtSPHERE_COORDINATES.getSelectedIndex();
		if (selected_sphere != -1) {
			//Get the selected sphere
			Sphere sphere = SPHERES.get(selected_sphere);
			//Get spheres coordinates
			float[] coordinates = SPHERES.get(selected_sphere).getSphereCoordinates();
			//Crop the picture around the picture
			BufferedImage image = Images.get(current_selected_picture_on_preview).getImageCropFromSphere(coordinates, 10);
			//Set the image location
			sphere_preview_panel.setImageLocation(Math.round(coordinates[0] - coordinates[2]) - 10, Math.round(coordinates[1] - coordinates[2]) - 10);
			//Set image
			sphere_preview_panel.setImage(image);
			//Set sphere center and radius for the selected ball
			sphere_preview_panel.setCenter(sphere.getSphereCoordinates());
			//Set sphere preview panel on the current tab

			//Set the user defined/default selection in all the thumbnails for this sphere
			Component[] sphereThumbnails = jpSThumbnails.getComponents();
			for (Component thumbnail : sphereThumbnails) {
				//Cast to thumbnail
				ImageThumbnailButton sphere_thumbnail = (ImageThumbnailButton) thumbnail;
				//Get the image id and see if it is selected for the sphere
				UUID imageId = sphere_thumbnail.getImageId();
				boolean is_selected = Images.get(imageId).is_selected_for_the_Sphere(sphere.getId());
				//Set the thumbnail de/selected
				sphere_thumbnail.setSelected(is_selected);
			}

			((javax.swing.JScrollPane) (jtSPHERE_COORDINATES.getSelectedComponent())).setViewportView(sphere_preview_panel);
			((javax.swing.JScrollPane) (jtSPHERE_COORDINATES.getSelectedComponent())).validate();
			jlSelectedSphere.setText("Sphere " + (selected_sphere + 1));
			sphere_preview_panel.repaint();
		}
	}//GEN-LAST:event_jtSPHERE_COORDINATESStateChanged

	private void jtHLTD_RESULTSStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jtHLTD_RESULTSStateChanged
		//When another sphere is selected, other image is needed and the preview panel must be updated
		//Get selected sphere
		int selected_sphere = jtHLTD_RESULTS.getSelectedIndex();
		if (selected_sphere != -1) {
			//Get the selected sphere
			Sphere sphere = SPHERES.get(selected_sphere);
			if (!output_image_is_blend) {
				//Draw the highlight
				highlight_panel.enableHighlightDraw(true);
				//Get spheres coordinates
				float[] coordinates = SPHERES.get(selected_sphere).getSphereCoordinates();
				//Crop the picture around the picture
				BufferedImage image = Images.get(current_selected_picture_on_output).getImageCropFromSphere(coordinates, 10);
				//Set the image location
				highlight_panel.setImageLocation(Math.round(coordinates[0] - coordinates[2]) - 10, Math.round(coordinates[1] - coordinates[2]) - 10);
				//Set image
				highlight_panel.setImage(image);
				//Get highliht and set it on panel
				float[] highlight = Images.get(current_selected_picture_on_output).getHighlight(sphere.getId());
				if (highlight == null) {
					highlight_panel.HighLight_Not_Defined(true);
					highlight_panel.HighLight_Defined_By_User(false);
				} else {
					highlight_panel.HighLight_Not_Defined(false);
					//If is user defined warn the interface
					boolean is_user_defined = Images.get(current_selected_picture_on_output).Is_user_defined(sphere.getId());
					highlight_panel.HighLight_Defined_By_User(is_user_defined);
					highlight_panel.setHighLight(highlight);
				}

			} else {
				highlight_panel.HighLight_Not_Defined(false);
				highlight_panel.HighLight_Defined_By_User(false);
				highlight_panel.enableHighlightDraw(false);
				highlight_panel.setImage(sphere.getBlend().getImage());
			}

			//Set sphere preview panel on the current tab
			((javax.swing.JScrollPane) (jtHLTD_RESULTS.getSelectedComponent())).setViewportView(highlight_panel);
			((javax.swing.JScrollPane) (jtHLTD_RESULTS.getSelectedComponent())).validate();
			jlSelectedHTBall.setText("Sphere " + (selected_sphere + 1));
			highlight_panel.repaint();
		}
}//GEN-LAST:event_jtHLTD_RESULTSStateChanged

	private void go_to_output_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_go_to_output_buttonActionPerformed
		//Changes to the output interface panel.
		CardLayout cl = (CardLayout) this.getLayout();
		cl.last(this);
}//GEN-LAST:event_go_to_output_buttonActionPerformed

	private void jb_redefine_highlightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jb_redefine_highlightActionPerformed
		//Wich is the selected sphere
		int selected_sphere = jtHLTD_RESULTS.getSelectedIndex();
		if (selected_sphere != -1) {
			//Which is the selected image
			UUID selected_picture = current_selected_picture_on_output;
			//Get the sphere id
			UUID sphere_id = SPHERES.get(selected_sphere).getId();
			//Get all the highlight info fields in the XMLcarrier for this sphere
			RawInfo HighLight_Info = carrier.getComputedInfo("HighLights", "SphereID", sphere_id.toString());
			ArrayList<Info> highlights = HighLight_Info.getAllInnerInformation();
			//Get highlight to be edited
			Info new_highlight = null;
			for (Info highlight : highlights) {
				if (highlight.getAttribute("ImageID").equals(selected_picture.toString())) {
					new_highlight = highlight;
					break;
				}
			}
			//If the highlight is not defined
			if (new_highlight == null) {
				new_highlight = new Info("sphere");
				new_highlight.addAttribute("ImageID", selected_picture.toString());
			}

			//Edit/set highlight info
			float[] user_highlight = highlight_panel.getHighLight();
			new_highlight.addAttribute("x", Float.toString(user_highlight[0]));
			new_highlight.addAttribute("y", Float.toString(user_highlight[1]));

			//Update highlight field
			carrier.modifyComputedInfo(HighLight_Info);

			//Set the highlight value in the plugin variables
			Images.get(selected_picture).setHighlight(sphere_id, user_highlight);
			//Define as user defined
			Images.get(selected_picture).set_user_defined(sphere_id, true);

			//Regist the event in the log
			Event change_event = new Event();
			change_event.setText("In the sphere with the id " + sphere_id.toString() + " off the image that was the id " + selected_picture.toString() + " the value of the highlight was changed by the user to x: " + user_highlight[0] + " y: " + user_highlight[1]);
			carrier.registEvent(change_event);

			//Set the carrier time stamp, to indicate action
                        carrier.setTimestamp(plugInfo.getStage());
			carrier.incTimestamp();
			try {
				carrier.writeXML();
			} catch (Exception ex) {
				Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

	}//GEN-LAST:event_jb_redefine_highlightActionPerformed

	/**Method used to reset the output module in case of a new excution*/
	public void resetOutput() {
		//Reset error status
		this.ERROR = NO_ERRORS;

		//Remove the edge and median buttons
		//Get button number
		int number = jpHLTThumbnails.getComponents().length;
		jpHLTThumbnails.remove(number - 1);

		//For all the images reset highlights and values
		for (ImageContainer container : Images.values()) {
			container.resetHighlightValues();
		}

		//Remove result panels
		jtHLTD_RESULTS.removeAll();
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Redo_process_button;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton go_to_output_button;
    private javax.swing.JPanel input_option_panel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JSplitPane jSplitPane6;
    private javax.swing.JSplitPane jSplitPane7;
    private javax.swing.JButton jbDetectHighlights;
    private javax.swing.JButton jb_redefine_highlight;
    private javax.swing.JLabel jlSelectedHTBall;
    private javax.swing.JLabel jlSelectedSphere;
    private javax.swing.JPanel jpHLTThumbnails;
    private javax.swing.JPanel jpHighlight;
    private javax.swing.JPanel jpSThumbnails;
    private javax.swing.JSlider jsDHLTImageScale;
    private javax.swing.JSlider jsUserThreshold;
    private javax.swing.JTabbedPane jtHLTD_RESULTS;
    private javax.swing.JTabbedPane jtSPHERE_COORDINATES;
    // End of variables declaration//GEN-END:variables
}
