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

package highlightdetect.helpers;

import XMLcarrier.ImageFile;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.TreeMap;
import java.util.UUID;

public class Sphere {
	
	private UUID ballId; 
	private Point2D beginPoint;
	private TreeMap<UUID,ImageFile> images = null;
    private TreeMap<UUID,Float[]> HL_DETECTION;
    private float BALL_DETECTION[] = {-1.0f, -1.0f, -1.0f};
    private int USER_THRESHOLD;
	private String projectPath;
	private String projectName;
	private TreeMap<UUID, BufferedImage> workingImages;
	private BufferedImage blend;

	public Sphere(UUID id, Point2D point, String projectName, String projectPath, float x, float y, float r, int USER_THRESHOLD, TreeMap<UUID,ImageFile> images, TreeMap<UUID, BufferedImage> wi) {
		this.ballId = id;
		this.beginPoint = point;
		this.images = images;
		this.projectPath = projectPath;
		this.projectName = projectName;
		this.BALL_DETECTION[0] = x;
		this.BALL_DETECTION[1] = y;
		this.BALL_DETECTION[2] = r;
		this.USER_THRESHOLD = USER_THRESHOLD;
		this.HL_DETECTION = new TreeMap<UUID,Float[]>();
		this.workingImages = wi;
		this.blend = new BufferedImage(Math.round(r*2), Math.round(r*2), BufferedImage.TYPE_BYTE_GRAY);
	}

	
	public float[] getBALL_DETECTION() {
		return BALL_DETECTION;
	}

	public void setBALL_DETECTION(float[] BALL_DETECTION) {
		this.BALL_DETECTION = BALL_DETECTION;
	}

	public int getUSER_THRESHOLD() {
		return USER_THRESHOLD;
	}

	public void setUSER_THRESHOLD(int USER_THRESHOLD) {
		this.USER_THRESHOLD = USER_THRESHOLD;
	}

	public TreeMap<UUID, Float[]> getHL_DETECTION() {
		return HL_DETECTION;
	}

	public void setHL_DETECTION(TreeMap<UUID, Float[]> HL_DETECTION) {
		this.HL_DETECTION = HL_DETECTION;
	}

	public TreeMap<UUID, ImageFile> getImages() {
		return images;
	}

	public void setImages(TreeMap<UUID, ImageFile> images) {
		this.images = images;
	}

	public UUID getBallId() {
		return ballId;
	}

	public void setBallId(UUID ballId) {
		this.ballId = ballId;
	}

	public TreeMap<UUID, BufferedImage> getWorkingImages() {
		return workingImages;
	}

	public void setWorkingImages(TreeMap<UUID, BufferedImage> workingImages) {
		this.workingImages = workingImages;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public BufferedImage getBlend() {
		return blend;
	}

	public void setBlend(BufferedImage blend) {
		this.blend = blend;
	}

	public Point2D getBeginPoint() {
		return beginPoint;
	}

	public void setBeginPoint(Point2D beginPoint) {
		this.beginPoint = beginPoint;
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
