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
package RTIbuilder.helpers;

import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.net.MalformedURLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.swing.ImageIcon;

/**
 *
 * Class responsible for storing the project images information in the plugin.
 *
 */
public class ImageContainer {

    /**Image id*/
    private UUID image_id;
    /**Image name*/
    private String image_name;
    /**Image File*/
    private File image_file;
    /**Icon used for image preview buttons*/
    private ImageIcon icon;
    /**Thumbnail picture for image preview*/
    private BufferedImage thumbnail;
    /**Image width*/
    private int width = -1;
    /**Image height*/
    private int height = -1;
    /**LP(light positions) values*/
    private float lp[];

    /**
     * Creates a new image container, loading a thumbnail and the image size.
     *@param image_id Image identifier
     *@param image_file Image file
     */
    public ImageContainer(UUID image_id, File image_file) {
        this.lp = new float[3];
        this.image_id = image_id;
        this.image_file = image_file;
        this.image_name = image_file.getName();

        PlanarImage image = null;
        try {
            image = JAI.create("url", image_file.toURI().toURL());
            System.setProperty("com.sun.media.jai.disableMediaLib", "true");
            width = image.getWidth();
            height = image.getHeight();
            float scaleIcon = 50.0f / (Math.max(height, width));
            ParameterBlock params = new ParameterBlock();
            params.addSource(image);
            params.add(scaleIcon);
            params.add(scaleIcon);
            params.add(0.0f);
            params.add(0.0f);
            params.add(Interpolation.getInstance(Interpolation.INTERP_NEAREST));
            thumbnail = JAI.create("scale", params).getAsBufferedImage();
        } catch (MalformedURLException ex) {
            Logger.getLogger(ImageContainer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**Get the image icon.<br>
     *If null, the container tries to load it.
     *@return a swing icon.
     */
    public ImageIcon getIcon() {
        if (icon == null) {
            loadIcon();
        }
        return icon;
    }

    /**Loads the image icon from the container thumbnail image*/
    public void loadIcon() {
        this.icon = new ImageIcon(thumbnail);
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

    /**Get a buffered image representation of the picture.
     *@return a buffered image.
     *@see BufferedImage
     */
    public BufferedImage getImage() {

        try {
            if (this.width == -1) {
                BufferedImage image = JAI.create("url", image_file.toURI().toURL()).getAsBufferedImage();
                this.width = image.getWidth();
                this.height = image.getHeight();
                return image;
            }
            return JAI.create("url", image_file.toURI().toURL()).getAsBufferedImage();
        } catch (MalformedURLException ex) {
            Logger.getLogger(ImageContainer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**Get the thumbnail image.
     *@return a buffered image with the thumbnail.
     *@see BufferedImage
     */
    public BufferedImage getThumbnailImage() {
        return thumbnail;
    }

    /**Get the image height
     *@return an int with the image height.
     */
    public int getHeight() {
        return height;
    }

    /**Get the image Width.
     *@return an int with the image widht.
     */
    public int getWidth() {
        return width;
    }

    public float[] getLp() {
        return lp;
    }

    public void setLp(float[] lp) {
        this.lp = lp;
    }
}
