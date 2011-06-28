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


package spheredetection.helpers;

import XMLcarrier.ImageFile;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.UUID;

public class Area {
	
	private UUID areaId;
	private Point2D beginPoint;
	private int areaNumber;
	private float x,y,w,h;
	private int algorithm;
	private boolean hough;
	private boolean binarize;
	private float BallDetection[] = {-1.0f, -1.0f, -1.0f};
	private String projectName;
	private ImageFile median;
	private ImageFile edge;
	private String projectPath;
	private ArrayList<ImageFile> images;
	private TreeMap<UUID, BufferedImage> workingImages;

	public Area() {
	}

	
	public Area(UUID id, Point2D point, int areaNumber, String projectName, String path, float x, float y, float w, float h, int algorithm, boolean hough, boolean binarize, ArrayList<ImageFile> images, TreeMap<UUID, BufferedImage> wi) {
		this.areaId = id;
		this.beginPoint = point;
		this.areaNumber = areaNumber;
		this.projectName = projectName;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.algorithm = algorithm;
		this.hough = hough;
		this.binarize = binarize;
		this.images = images;
		this.workingImages = wi;
		this.projectPath = path;
	}

	public Point2D getBeginPoint() {
		return beginPoint;
	}

	public void setBeginPoint(Point2D beginPoint) {
		this.beginPoint = beginPoint;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}
	
	

	public int getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(int algorithm) {
		this.algorithm = algorithm;
	}

	public boolean isBinarize() {
		return binarize;
	}

	public void setBinarize(boolean binarize) {
		this.binarize = binarize;
	}

	public boolean isHough() {
		return hough;
	}

	public void setHough(boolean hough) {
		this.hough = hough;
	}

	public ArrayList<ImageFile> getImages() {
		return images;
	}

	public void setImgsURL(ArrayList<ImageFile> images) {
		this.images = images;
	}

	public float getH() {
		return h;
	}

	public void setH(float h) {
		this.h = h;
	}

	public float getW() {
		return w;
	}

	public void setW(float w) {
		this.w = w;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public UUID getAreaId() {
		return areaId;
	}

	public void setAreaId(UUID areaId) {
		this.areaId = areaId;
	}

	public float[] getBallDetection() {
		return BallDetection;
	}

	public void setBallDetection(float[] BALL_DETECTION) {
		this.BallDetection = BALL_DETECTION;
	}

	public ImageFile getEdge() {
		return edge;
	}

	public void setEdge(ImageFile edge) {
		this.edge = edge;
	}

	public ImageFile getMedian() {
		return median;
	}

	public void setMedian(ImageFile median) {
		this.median = median;
	}

	public String getPath() {
		return projectPath;
	}

	public void setPath(String path) {
		this.projectPath = path;
	}

	public int getAreaNumber() {
		return areaNumber;
	}

	public void setAreaNumber(int areaNumber) {
		this.areaNumber = areaNumber;
	}

	public TreeMap<UUID, BufferedImage> getWorkingImages() {
		return workingImages;
	}

	public void setWorkingImages(TreeMap<UUID, BufferedImage> workingImages) {
		this.workingImages = workingImages;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	
	

	
}
