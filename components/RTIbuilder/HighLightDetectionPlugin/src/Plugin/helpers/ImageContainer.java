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

package Plugin.helpers;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.net.MalformedURLException;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.swing.ImageIcon;

/**
 *
 * Class responsible for storing the information for all the images in the plugin.
 *
 */
public class ImageContainer{

	  /**Image id*/
	  private UUID image_id;
	  /**Image name*/
	  private String image_name;
	  /**Image File*/
	  private File image_file;
	  /**Icon used for image preview buttons*/
	  private ImageIcon icon;
	  /**Image width*/
	  private int width  = -1;
	  /**Image height*/
	  private int height = -1;
	  /**Image selection per selected area. Flags if the image is selected for the process in each seleceted area*/
	  private TreeMap<UUID,Boolean> Selection_in_Sphere;
	  /**Image detected highlight*/
	  TreeMap<UUID,float[]> highlights = new TreeMap<UUID, float[]>();
	  /**User defined highligths per sphere*/
	  TreeMap<UUID,Boolean> is_user_defined = new TreeMap<UUID, Boolean>();

          private String relative = "";

	  /** Creates a new image container.
	  *@param image_id Image identifier
	  *@param image_file Image file
	  */
	  public ImageContainer(UUID image_id, File image_file) {
			this.image_id = image_id;
			this.image_file = image_file;
			this.image_name =  image_file.getName();
	  }

          public void setRelPath(String relPath)
          {
              this.relative = relPath;
          }

	  /**Get the image icon
	  * @return a swing icon.
	  */
	  public ImageIcon getIcon() {
			return icon;
	  }

	  /**Loads the image icon
	  * @param image the icon image.
	  */
	  public void loadIcon(Image image) {
			this.icon =  new ImageIcon(image);
	  }

	   /**Loads the image icon from the container image*/
	  public void loadIcon() {
		    PlanarImage image =  null;
			try {
				  image = JAI.create("url", image_file.toURI().toURL());
				  System.setProperty("com.sun.media.jai.disableMediaLib", "true");
				  float scaleIcon = 50.0f / (Math.max(image.getHeight(), image.getWidth()));
				  ParameterBlock params = new ParameterBlock();
				  params.addSource(image);
				  params.add(scaleIcon);
				  params.add(scaleIcon);
				  params.add(0.0f);
				  params.add(0.0f);
				  params.add(Interpolation.getInstance(Interpolation.INTERP_NEAREST));
				  BufferedImage imgL = JAI.create("scale", params).getAsBufferedImage();
				  this.icon = new ImageIcon(imgL);
				  
			} catch (MalformedURLException ex) {
				  Logger.getLogger(ImageContainer.class.getName()).log(Level.SEVERE, null, ex);
			}
			
	  }

	   /**Loads the image icon from the container image*/
	  public void loadIcon(boolean useRel) {
              if (useRel == false) {loadIcon(); return;}
		    PlanarImage image =  null;
			try {
                            File newPath = new File(relative + File.separator + image_file.getPath());
				  image = JAI.create("url", newPath.toURI().toURL());
				  System.setProperty("com.sun.media.jai.disableMediaLib", "true");
				  float scaleIcon = 50.0f / (Math.max(image.getHeight(), image.getWidth()));
				  ParameterBlock params = new ParameterBlock();
				  params.addSource(image);
				  params.add(scaleIcon);
				  params.add(scaleIcon);
				  params.add(0.0f);
				  params.add(0.0f);
				  params.add(Interpolation.getInstance(Interpolation.INTERP_NEAREST));
				  BufferedImage imgL = JAI.create("scale", params).getAsBufferedImage();
				  this.icon = new ImageIcon(imgL);

			} catch (MalformedURLException ex) {
				  Logger.getLogger(ImageContainer.class.getName()).log(Level.SEVERE, null, ex);
			}

	  }

	  /**Get image file
	  *@return the correspondent file associated with this picture.
	  */
	  public File getImageFile() {
			return image_file;
	  }

	  /**Set image file*/
	  public void setImageFile(File image_file) {
			this.image_file = image_file;
	  }

	  /**Get image id
	  *@return image unique identifier.
	  *@see UUID
	  */
	  public UUID getImageId() {
			return image_id;
	  }

	  /**Set the image id
	  *@param image_id  the image unique identifier.
	  *@see UUID
	  */
	  public void setImageId(UUID image_id) {
			this.image_id = image_id;
	  }

	  /**Get image name
	  * @return the string image name.
	  */
	  public String getImageName() {
			return image_name;
	  }

	  /**Set the image name
	  * @param image_name the image name.
	  */
	  public void setImageName(String image_name) {
			this.image_name = image_name;
	  }

	  /**Get the image height
	  *@return a int with the image height.
	  */
	  public int getHeight() {
			return height;
	  }

	  /**Set the image height
	  *@param height the image height.
	  */
	  public void setHeight(int height) {
			this.height = height;
	  }

	  /**Get the image width
	  *@return a int with the image width.
	  */
	  public int getWidth() {
			return width;
	  }
	  /**Set the image width
	  * @param width  the image widht.
	  */
	  public void setWidth(int width) {
			this.width = width;
	  }

	  /**Get a buffered image representation of the picture.
	  *@return a buffered image.
	  *@see BufferedImage
	  */
	  public BufferedImage getImage() {
			try {
				  if(this.width==-1)
				  {
						BufferedImage image = JAI.create("url", image_file.toURI().toURL()).getAsBufferedImage();
						this.width  = image.getWidth();
						this.height = image.getHeight();
						return image;
				  }
				  return JAI.create("url", image_file.toURI().toURL()).getAsBufferedImage();
			} catch (MalformedURLException ex) {
				  Logger.getLogger(ImageContainer.class.getName()).log(Level.SEVERE, null, ex);
			}
			return null;
	  }

	  public BufferedImage getImage(boolean useRel) {
              if(!useRel) {return getImage();}
			try {
                            File newPath = new File(relative + File.separator + image_file.getPath());
				  if(this.width==-1)
				  {
						BufferedImage image = JAI.create("url", newPath.toURI().toURL()).getAsBufferedImage();
						this.width  = image.getWidth();
						this.height = image.getHeight();
						return image;
				  }
				  return JAI.create("url", newPath.toURI().toURL()).getAsBufferedImage();
			} catch (MalformedURLException ex) {
				  Logger.getLogger(ImageContainer.class.getName()).log(Level.SEVERE, null, ex);
			}
			return null;
	  }

	  /**Get a section of the container picture.
	  *@param area the area to be retrived.
	  *@param margin the margin around the sphere to be included in the picture
	  *@return a buffered image.
	  *@see BufferedImage
	  */
	  public BufferedImage getImageCropFromSphere(float[] sphere, int margin) {

			//Image crop to be returned
			BufferedImage image_crop = null;

			try {
				  //Get image
				  PlanarImage image = JAI.create("url", image_file.toURI().toURL());
				  //If not set, set the
				  if(this.width==-1)
				  {
						this.width  = image.getWidth();
						this.height = image.getHeight();
				  }

				  //Define area
				  //To make sure that crop is inside the image
				  int x = Math.round(sphere[0]-sphere[2]-margin);
				  x = x<0 ? 0 : x;
				  int y = Math.round(sphere[1]-sphere[2]-margin);
				  y = y<0 ? 0 : y;
				  int w = Math.round(sphere[2]+sphere[2]+margin);
				  w = w>this.width ? this.width : w;
				  int h = Math.round(sphere[2]+sphere[2]+margin);
				  h = h>this.height ? this.height : h;

				  Rectangle area =  new Rectangle(x,y,w,h);
				  //Set crop parameters
				  ParameterBlock params = new ParameterBlock();
				  params.addSource(image);
				  params.add((float) area.x );
				  params.add((float) area.y );
				  params.add((float) area.width );
				  params.add((float) area.height );
				  //crop image
				  image_crop = JAI.create("crop", params).getAsBufferedImage();

			} catch (MalformedURLException ex) {
				  Logger.getLogger(ImageContainer.class.getName()).log(Level.SEVERE, null, ex);
			}
			return image_crop;
	  }

	  /**Select/Deselect for the given sphere, the image for processing.
	   * @param id The area sphere the image has been selected.
	   * @param is_selected select the image if true, otherwise deselect.
	   */
	  public void setSelected_for_the_Sphere(UUID id,boolean is_selected){
			if(Selection_in_Sphere==null){
				  Selection_in_Sphere = new TreeMap<UUID, Boolean>();
			}
			Selection_in_Sphere.put(id, is_selected);
	  }

	  /**Returns true if the image is selected for the sphere.
	   * @param id The area where the test will occur.
	   * @return true if is selected, false otherwise.
	   */
	  public boolean is_selected_for_the_Sphere(UUID id){
			if(Selection_in_Sphere==null){
				return false;
			}
		   return (Selection_in_Sphere.containsKey(id)? Selection_in_Sphere.get(id)==Boolean.TRUE : false);
	  }



	/**Get the highlight for this image on the given sphere.
	 * @param sphere the sphere's id where the highlight is located.
	 * @return a two size array with the location of the highlight.
	 */
	public float[] getHighlight(UUID sphere) {
			float[] HLT = new float[2];
			if(highlights.containsKey(sphere)){
				  HLT[0] = highlights.get(sphere)[0];
				  HLT[1] = highlights.get(sphere)[1];
				  return HLT;
			}
			return null;
	}

	 /**Set the highlight for this image.
	 *@param sphere the sphere's id where the highlight is located.
	 *@param highlight  a two size array with the location of the highlight.
	 */
	public void setHighlight(UUID sphere, float[] highlight) {
			float[] HLT = new float[2];
			HLT[0] = highlight[0];
			HLT[1] = highlight[1];
			highlights.put(sphere, HLT);
	}

	/**Returns true if the highlight was defined by the user in the selected sphere.
	 *@param sphere the sphere where the user may have defined the highlight in this image.
	 *@return true if the user was defined the highlight, false otherwise.
	 */
	public boolean Is_user_defined(UUID sphere) {
	   return (is_user_defined.containsKey(sphere)&&is_user_defined.get(sphere));
	}

	/**Sets the highlight as user defined or not for the given sphere.
	 *@param sphere the sphere where the user may have defined the highlight in this image.
	 *@param is_user_defined true to set the highlight value as user defined, false otherwise.
	 */
	public void set_user_defined(UUID sphere, boolean is_user_defined) {
	   this.is_user_defined.put(sphere, is_user_defined);
	}

	/**Reset all the highlight values and user defined flags*/
	public void resetHighlightValues(){
	   highlights = new TreeMap<UUID, float[]>();
	   is_user_defined = new TreeMap<UUID, Boolean>();
	}

}
