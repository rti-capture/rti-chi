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
public class Sphere {

	/**Area unique identifier*/
    private UUID id;
	/**Sphere detection coordinates for this area*/
	private float[] sphere = {-1.0f, -1.0f, -1.0f};
	/**Images associated with this area*/
    private ImageContainer blend;

	/**Create a new Sphere.
	 *@param sphere The sphere coordinates.
	 *@param sphere_id The sphere identifier.
	 */
    public  Sphere(float[] sphere, UUID sphere_id)
    {
		this.sphere[0] = sphere[0];
		this.sphere[1] = sphere[1];
		this.sphere[2] = sphere[2];
        this.id = sphere_id;
		this.blend = null;
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

	/**Return sphere coordinates for this area.
	@return a float array with the position and radius of the sphere.
	*/
	public float[] getSphereCoordinates() {
		return sphere;
	}

    /**Set sphere coordinates results for this area.
	@param  A float array with the position and radius of the sphere.
	*/
	public void setSphereCoordinates(float[] sphere) {
		this.sphere[0] = sphere[0];
		this.sphere[1] = sphere[1];
		this.sphere[2] = sphere[2];
	}


     /**Returns a rectangle area that contains the sphere.
	 *@param sphere the three coordinates of the sphere
	 *@param margin The margin to leave around the sphere in the crop.
	 *@return a Rectangle containing the sphere
	 */
	public static Rectangle getRectanglefromSphere(float[] sphere, int margin){
			return (new Rectangle(Math.round(sphere[0]-sphere[2]-margin),
								  Math.round(sphere[1]-sphere[2]-margin),
								  Math.round(sphere[2]+sphere[2]+margin),
								  Math.round(sphere[2]+sphere[2]+margin)));
	}
	
}
