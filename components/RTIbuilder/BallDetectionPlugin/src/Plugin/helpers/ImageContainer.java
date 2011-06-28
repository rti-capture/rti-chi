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
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
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
 * @author Pedro
 */
/**
 *
 * Class responsible for storing the project images information in the plugin.
 *
 */

public class ImageContainer implements Comparable{

	  /**Image id*/
	  private UUID image_id;
	  /**Image name*/
	  private String image_name;
	  /**Image File*/
	  private File image_file;
	  /**Icon used for image preview buttons*/
	  private ImageIcon icon;
	  /**Black image for luminance calculation*/
	  private BufferedImage black_image;
	  /**Image luminance*/
	  private float luminance;
	  /**Image width*/
	  private int width  = -1;
	  /**Image height*/
	  private int height = -1;
	  /**Image selection per selected area. Flags if the image is selected for the process in each seleceted area*/
	  private TreeMap<UUID,Boolean> Selection_in_Areas;

          private String relativePath = "";

	  /**
	  * Creates a new image container.
	  *@param image_id Image identifier
	  *@param image_file Image file
	  */
	  public ImageContainer(UUID image_id, File image_file) {
			this.image_id = image_id;
			this.image_file = image_file;
			this.image_name =  image_file.getName();
	  }

	  /**Get image icon
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
			black_image = convertToGrayscale((BufferedImage) image);
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
				  black_image = convertToGrayscale(imgL);
				  
			} catch (MalformedURLException ex) {
				  Logger.getLogger(ImageContainer.class.getName()).log(Level.SEVERE, null, ex);
			}
			
	  }

	  /**Loads the image icon from the container image*/
	  public void loadIcon(String relative) {
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
				  black_image = convertToGrayscale(imgL);

			} catch (MalformedURLException ex) {
				  Logger.getLogger(ImageContainer.class.getName()).log(Level.SEVERE, null, ex);
			}

	  }

	  /**Get image file
	  *@return the correspondent file associated with this picture.
	  */
	  public File getImage_file() {
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
	  public UUID getImage_id() {
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
	  public String getImage_name() {
			return image_name;
	  }

	  /**Set the image name
	  * @param image_name the image name.
	  */
	  public void setImage_name(String image_name) {
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
                if(!useRel) return this.getImage();

			try {
                            File newPath = new File(relativePath + File.separator + image_file.getPath());
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

          public void setRelPath(String rp)
          {
              this.relativePath = rp;
          }

	  /**Get a section of the container picture.
	  *@param area the area to be retrived.
	  *@return a buffered image.
	  *@see BufferedImage
	  */
	  public BufferedImage getImageCrop(Rectangle area) {

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


	  /**Calculates the avarage image luminance in the given area, so the best ones can be chosen.
	  */
	  public void calculateLuminance(Rectangle r){

			//get image size if not known yet
			if(width==-1){
				  BufferedImage image = this.getImage();
				  this.width =  image.getWidth();
				  this.height =  image.getHeight();
			}
			//Scale between the thumbnail and the image
			float scale  = (float)black_image.getWidth()/(float)this.width;

			
			ParameterBlock params = new ParameterBlock();
			//Put the rectangle in thumbnail coordinates.
			float resized_x = r.x * scale;
			float resized_y = r.y * scale;
			float resized_w = r.width * scale;
			resized_w = ((resized_x + resized_w) > icon.getIconWidth()) ? icon.getIconWidth() - resized_x : resized_w;
			float resized_h = r.height * scale;
			resized_h = ((resized_h + resized_y) > icon.getIconHeight()) ? icon.getIconHeight() - resized_y : resized_h;
			resized_w = (float) Math.floor(resized_w);
			resized_h = (float) Math.floor(resized_h);

			params.addSource(black_image);
			params.add((float) resized_x);
			params.add((float) resized_y);
			params.add(resized_w);
			params.add(resized_h);
			params.add(Interpolation.getInstance(Interpolation.INTERP_NEAREST));
			BufferedImage crop_black = JAI.create("crop", params).getAsBufferedImage();
			int blackHeight = crop_black.getHeight(),
					blackWidht = crop_black.getWidth();

			for (int h = 0; h < blackHeight; h++) {
				  for (int w = 0; w < blackWidht; w++) {
						float c[] = {0};
						crop_black.getRaster().getPixel(w, h, c);
						luminance += c[0];
				  }
			}
			luminance /= (blackHeight * blackWidht);
	  }

	  /**Get the average image luminance in the last given area.
	   *@return a float with the image luminace.
	  */
	  public float getLuminance(){
			return this.luminance;
	  }


	  /**Select for the given area, the image for processing.
	   * @param id The area where the image has been selected.
	   * @param is_selected true if the area is selected, false otherwise
	   */
	  public void setSelected_for_the_Area(UUID id,boolean is_selected){
			if(Selection_in_Areas==null){
				  Selection_in_Areas = new TreeMap<UUID, Boolean>();
			}
			Selection_in_Areas.put(id, is_selected);
	  }

	  /**Returns true if the image is selected in the given area.
	   * @param id The area where the test will occur.
	   * @return true if is selected, false otherwise.
	   */
	  public boolean is_selected_for_the_Area(UUID id){
			if(Selection_in_Areas==null){
				return false;
			}
		   return (Selection_in_Areas.containsKey(id)? Selection_in_Areas.get(id)==Boolean.TRUE : false);
	  }

	/**Comparator to assure order by luminance.
	*/
	public int compareTo(Object arg0) {
	  if (arg0 instanceof ImageContainer) {
			ImageContainer imgC = (ImageContainer) arg0;
			if (this.luminance == imgC.getLuminance()) {
				  return 0;
			}
			if (this.luminance > imgC.getLuminance()) {
				  return -1;
			}
			if (this.luminance < imgC.getLuminance()) {
				  return 1;
			}
	  }
	  return 0;
}


    public static BufferedImage convertToGrayscale(BufferedImage source) {
        BufferedImageOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        return op.filter(source, null);
    }
}
