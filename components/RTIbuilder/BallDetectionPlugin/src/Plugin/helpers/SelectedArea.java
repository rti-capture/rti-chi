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

import java.awt.Rectangle;
import java.util.UUID;

/**This is the  representation  of an area used for the ball detection process.
 It includes algorithm options chosen to this area and other information.*/
public class SelectedArea {

	/**Area unique identifier*/
    private UUID id;
	/**Selected algorithm to use in ball detection*/
    private int ALGORITHM;
	/**Flags the use or not of the binarize process in ball detection*/
    private boolean LP_CONF_USE_BINARIZE;
	/**Flags the usge or not of red hough in ball detection*/
    private boolean LP_USE_RED_HOUGH_CHECKBOX;
	/**Ball detection coordinates for this area*/
	private float[] spheredetection = {-1.0f, -1.0f, -1.0f};
	/**The area selection*/
    private Rectangle selection;
	/**Images associated with this area*/
    private ImageContainer blend;
    private ImageContainer edge;
    private ImageContainer median;


	/**Create a new selected area.
	 @param sel The rectangle area.
	 */
    public  SelectedArea(Rectangle sel)
    {
        this.id = UUID.randomUUID();
        this.selection = sel;
        this.ALGORITHM = 0;
        this.LP_CONF_USE_BINARIZE = false;
        this.LP_USE_RED_HOUGH_CHECKBOX = true;
		this.blend = null;
		this.median = null;
		this.edge = null;
    }

	/**Set the algorithm to use in ball detection.
	 @param alg The selected algorithm.
	 */
    public void setAlgorithm(int alg)
    {
        this.ALGORITHM = alg;
    }
    
	/**Get the algorithm to use in ball detection.
	@return The selected algorithm.
	*/
    public int getAlgorithm(){
        return this.ALGORITHM;
    }
      
	/**Determines to use or not of the binarize process in ball detection.
	@return true to use, false otherwise.
	*/
    public boolean getBinarize(){
    
         return this.LP_CONF_USE_BINARIZE;
    }

     /**Set the usage or not of  the binarize process in ball detection.
	 @param bin  True to use, false otherwise.
	 */
	public void setBinarize(boolean bin){

         this.LP_CONF_USE_BINARIZE = bin;
    }

	/**Determines to use or not of red hough in ball detection.
	@return true to use, false otherwise.
	*/
    public boolean getHough(){

         return this.LP_USE_RED_HOUGH_CHECKBOX;
    }

	/**Set the usage or not of red hough in ball detection.
	 @param hough  True to use, false otherwise.
	 */
    public void setHough (boolean hough){

         this.LP_USE_RED_HOUGH_CHECKBOX= hough;
    }


    /**Get area of the selection.
	@return A integer with the selection area.
	*/
    public int getArea(){
        return (this.selection.x*this.selection.y);
    
    }

	/**Get selected area.
	@return A rectangle with the selection area.
	*/
    public Rectangle getSelectionArea()
    {
        return this.selection;
    }

	/**Set the selection area.
	 @param r The area rectangle .
	 */
	public void setSelectionArea(Rectangle r)
    {
        this.selection =  r;
    }

  	/**Get blend image.
	@return a image container for blend.
	@see ImageContainer
	*/
    public ImageContainer getBlend() {
        return blend;
    }

    /**Set blend image.
	 @param blend  Blend image container .
	 */
    public void setBlend(ImageContainer blend) {
        this.blend = blend;
    }

	/**Get edge image.
	@return a image container for edge.
	@see ImageContainer
	*/
    public ImageContainer getEdge() {
        return edge;
    }

	/**Set edge image.
	@param edge   Edge image container .
	*/
    public void setEdge(ImageContainer edge) {
        this.edge = edge;
    }

    /**Get median image.
	@return a image container for median.
	@see ImageContainer
	*/
    public ImageContainer getMedian() {
        return median;
    }

	/**Set median image.
	@param median  Median image container .
	*/
    public void setMedian(ImageContainer median) {
        this.median = median;
    }
    
    /**Get unique area identifer.
	@return the area UUID.
	@see UUID
	*/
	public UUID getId() {
		return id;
	}

	/**Set the unique area identifer.
	@param id  The area UUID.
	@see UUID
	*/
	public void setId(UUID id) {
		this.id = id;
	}

	/**Return Sphere Detection results for this area.
	@return a float array with the position and radius of the sphere.
	*/
	public float[] getSphereDetection() {
		return spheredetection;
	}

    /**Set Sphere Detection results for this area.
	@param  A float array with the position and radius of the sphere.
	*/
	public void setSphereDetection(float[] spheredetection) {
		this.spheredetection[0] = spheredetection[0];
		this.spheredetection[1] = spheredetection[1];
		this.spheredetection[2] = spheredetection[2];
	}
	
	
	
	
    
}
