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

package spheredetection;

import Plugin.helpers.ImageContainer;
import spheredetection.imagehelpers.*;
//import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import XMLcarrier.*;
import spheredetection.helpers.Area;
import spheredetection.helpers.HoughTransform;
import java.awt.image.BufferedImage;
//import java.awt.image.renderable.ParameterBlock;
import java.io.File;
//import java.net.URL;
//import java.util.TreeMap;
import java.util.UUID;
//import java.util.logging.Level;
//import javax.media.jai.JAI;
//import javax.media.jai.PlanarImage;

public class SphereDetectionThread implements java.util.concurrent.Callable {

	private ArrayList<ImageContainer> WORKING_IMG_SET;
	private ArrayList<BufferedImage> WORKING_IMG_SET_BUFFER;
	private boolean SAVE_SUBIMAGE = true;
	private Area area;
        private String projectpath;

	public SphereDetectionThread(Area area, String projectpath)
	{
		this.area = area;
                this.projectpath=projectpath;
		this.WORKING_IMG_SET = loadWorkingSet(area);
	}

	private ArrayList<ImageContainer> loadWorkingSet(Area area) {
		ArrayList<ImageContainer> imgs = new ArrayList<ImageContainer>();

		for (ImageFile s : area.getImages()) {
			ImageContainer imgContainer = new ImageContainer(UUID.fromString(s.getUuid()),new File(projectpath+s.getUrl()));
			imgs.add(imgContainer);
		}

		return imgs;
	}

	public Area call() throws Exception {

		BufferedImage imgStage1 = null, imgMedianFilter = null;
		WORKING_IMG_SET_BUFFER = new ArrayList<BufferedImage>();



        for(BufferedImage s : area.getWorkingImages().values()) WORKING_IMG_SET_BUFFER.add(s);

		/*Iterator<ImageContainer> itImgCont = this.WORKING_IMG_SET.iterator();
		Rectangle rec = new Rectangle(	Math.round(area.getX()),
										Math.round(area.getY()),
										Math.round(area.getW()),
										Math.round(area.getH())
									 );
										

		while (itImgCont.hasNext()) {
			ImageContainer imgC = itImgCont.next();
			BufferedImage imgP = area.getWorkingImages().get(imgC.getId());
			MemoryHelper.clearMemory();
			WORKING_IMG_SET_BUFFER.add(ImageContainer.getImageCropFromImage(imgP,rec));
		}*/

		if (area.getAlgorithm() == 0) {
			// logger.info("Sequence for black ball detection");
			//System.out.println("Sequence for black ball detection");
			imgStage1 = BLACK_STAGE1();
		} else {
			// logger.info("Sequence for red ball detection");
			//System.out.println("Sequence for red ball detection");
			imgStage1 = RED_STAGE1();
		}

		if (imgStage1 != null) {
			imgMedianFilter = ImageFilters.medianFilter(imgStage1, 15);

			//imgContainerResult.get(0).setImgWorking(imgMedianFilter);

			// logger.info("Calculated median filter size 8  of previous image");
			SubImageSaveHelper.SaveImage(imgMedianFilter, "median"+area.getAreaNumber()+".jpg", area.getProjectName(), area.getPath(), "BALLDETECT", SAVE_SUBIMAGE);
			String pathImg =  area.getPath() + File.separator + area.getProjectName() + "_" + "median"+area.getAreaNumber()+".jpg";
//			String pathImg = area.getPath()+File.separator+"subimages"+File.separator+area.getProjectName()+File.separator+"BALLDETECT"+File.separator+"median"+area.getAreaNumber()+".jpg";
			area.setMedian(new ImageFile("image/JPG", pathImg, UUID.randomUUID().toString(), ""));
		} 
		if (imgMedianFilter != null) {
			if (area.isHough()) {
				if(area.getAlgorithm() == 1) {
					imgMedianFilter = ImageProcessing.invGray(imgMedianFilter);
					System.gc();
				}
				HoughTransform(imgMedianFilter);
			} else {
				Lableling(imgMedianFilter);
			}
		}
		
		imgStage1 = null; 
		imgMedianFilter = null;		
		System.gc();
		
		return area;

	}

	public BufferedImage BLACK_STAGE1() {

		try {
			BufferedImage imgReturn = ImageFilters.medianBlend(WORKING_IMG_SET_BUFFER);

			// logger.info("Computed Median of all selected images");
//			SubImageSaveHelper.SaveImage(imgReturn, "median.jpg", CurrentSelectedFolder.getAbsolutePath(), "BALLDETECT", "STAGE1", SAVE_SUBIMAGE);
//			MemoryHelper.clearMemory();
			return imgReturn;
		} catch (Exception e) {
			e.printStackTrace();
		}
//		MemoryHelper.clearMemory();
		return null;
	}

	public BufferedImage RED_STAGE1() {
		try {
			BufferedImage imgReturn = new BufferedImage(WORKING_IMG_SET_BUFFER.get(0).getWidth(), WORKING_IMG_SET_BUFFER.get(0).getHeight(), BufferedImage.TYPE_BYTE_GRAY);
			Iterator<BufferedImage> itImg = WORKING_IMG_SET_BUFFER.iterator();
			while (itImg.hasNext()) {
				ImageProcessing.blendFilterGray(imgReturn, ImageProcessing.selectRed(itImg.next()));
			}

			
			//logger.info("Computed blend of all selected images");
			//SubImageSaveHelper.SaveImage(imgReturn, "median.jpg", CurrentSelectedFolder.getAbsolutePath(), "BALLDETECT", "STAGE1", SAVE_SUBIMAGE);
			//String pathImg = CurrentSelectedFolder.getAbsolutePath()+File.separator+"subimages"+File.separator+"BALLDETECT"+File.separator+"edge.jpg";
			//area.setEdge(new ImageFile("image/JPG", pathImg, UUID.randomUUID().toString(), ""));
//			MemoryHelper.clearMemory();
			return imgReturn;
		} catch (Exception e) {
			e.printStackTrace();
		}
//		MemoryHelper.clearMemory();
		return null;
	}

	public BufferedImage HoughTransform(BufferedImage imgMedian) {


		int BALL_ESTIMATION[] = {-1, -1, -1};


		BufferedImage imgVar = ImageFilters.normalizeFilter(imgMedian);

		BufferedImage imgEdge = ImageFilters.sobelFilter(imgVar);

		//BufferedImage imgEdge2 = ImageFilters.sobelMagFilter(imgVar);

		//imgContainerResult.get(1).setImgWorking(imgEdge2);
		// logger.info("Hough Transform: applied sobel filter");
		SubImageSaveHelper.SaveImage(imgEdge, "edge"+area.getAreaNumber()+".jpg", area.getProjectName(), area.getPath(), "BALLDETECT", SAVE_SUBIMAGE);
		String pathImg =  area.getPath() + File.separator + area.getProjectName() + "_" + "edge"+area.getAreaNumber()+".jpg";
//		String pathImg = area.getPath()+File.separator+"subimages"+File.separator+area.getProjectName()+File.separator+"BALLDETECT"+File.separator+"edge"+area.getAreaNumber()+".jpg";
		area.setEdge(new ImageFile("image/JPG", pathImg, UUID.randomUUID().toString(), ""));

		int min = Math.min(imgEdge.getWidth(), imgEdge.getHeight());

		float max_r = (BALL_ESTIMATION[2] <= 0) ? Math.round((float) min * 0.45f) : Math.round((float) BALL_ESTIMATION[2] * 1.20f);
		float min_r = (BALL_ESTIMATION[2] <= 0) ? Math.round((float) min * 0.15f) : Math.round((float) BALL_ESTIMATION[2] * 0.80f);



		try {
//			MemoryHelper.clearMemory();
			int[] BALL_DETECTION_XYR_INT = new int[3];
			HoughTransform HT = new HoughTransform(imgEdge, (int) min_r, (int) max_r);

//			System.out.println("BIN:"+area.isBinarize());
			BALL_DETECTION_XYR_INT = HT.searchCircle(area.isBinarize());
//			System.out.println("SAIDA do HOUGH -> ("+BALL_DETECTION_XYR_INT[0]+","+BALL_DETECTION_XYR_INT[1]+","+BALL_DETECTION_XYR_INT[2]+")");

			area.getBallDetection()[0] = (float) BALL_DETECTION_XYR_INT[0];
			area.getBallDetection()[1] = (float) BALL_DETECTION_XYR_INT[1];
			area.getBallDetection()[2] = (float) BALL_DETECTION_XYR_INT[2];

//			MemoryHelper.clearMemory();
		} catch (Exception e) {
			e.printStackTrace();
		}



//		MemoryHelper.clearMemory();
		return imgEdge;
	}

	public BufferedImage Lableling(BufferedImage imgStage1) {


		area.setBallDetection(ImageLabeling.findBall(imgStage1, 85));
		//imgContainerResult.get(1).setImgWorking(imgStage1);
		//imgContainerResult.get(1).setName("Red filter");
		//MemoryHelper.clearMemory();
		//  logger.info("Used labeling algorithm to compute geometric center and radius");
		SubImageSaveHelper.SaveImage(imgStage1, "edge"+area.getAreaNumber()+".jpg", area.getProjectName(), area.getPath(), "BALLDETECT", SAVE_SUBIMAGE);
//		String pathImg = area.getPath()+File.separator+"subimages"+File.separator+area.getProjectName()+File.separator+"BALLDETECT"+File.separator+"edge"+area.getAreaNumber()+".jpg";
		String pathImg =  area.getPath() + File.separator + area.getProjectName() + "_" + "edge"+area.getAreaNumber()+".jpg";
		area.setEdge(new ImageFile("image/JPG", pathImg, UUID.randomUUID().toString(), ""));
//		MemoryHelper.clearMemory();
		return imgStage1;

	}

}
