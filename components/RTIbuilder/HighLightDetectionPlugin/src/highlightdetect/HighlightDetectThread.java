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

package highlightdetect;

import Plugin.helpers.ImageContainer;
import XMLcarrier.ImageFile;
import highlightdetect.helpers.Sphere;
import highlightdetect.imagehelpers.ImageLabeling;
import highlightdetect.imagehelpers.ImageProcessing;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
//import java.awt.image.renderable.ParameterBlock;
import java.io.File;
//import java.net.MalformedURLException;
//import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
//import javax.media.jai.JAI;
//import javax.media.jai.PlanarImage;

public class HighlightDetectThread implements java.util.concurrent.Callable {

    private ArrayList<ImageContainer> WORKING_IMG_SET;
	private Sphere ball;
//	private float scale = 1.0f;
	
    public HighlightDetectThread(Sphere b){
		this.ball = b;
        this.WORKING_IMG_SET = loadWorkingSet(b);
    }

	
	
	private ArrayList<ImageContainer> loadWorkingSet(Sphere b) {
		
		ArrayList<ImageContainer> imgs = new ArrayList<ImageContainer>();

		for (ImageFile s : b.getImages().values()) 
		{
			ImageContainer imgContainer = new ImageContainer(UUID.fromString(s.getUuid()),new File(s.getUrl()));
			imgs.add(imgContainer);
		}

		return imgs;
	}
	
	
	
    @SuppressWarnings("unchecked")
	public Sphere call() throws Exception {


        ArrayList<FutureTask> arrFT = new ArrayList<FutureTask>();
        ExecutorService es = Executors.newCachedThreadPool();

        Iterator<ImageContainer> itImg = WORKING_IMG_SET.iterator();

        while (itImg.hasNext()) {

            arrFT.add(new FutureTask(new HLTDetect(itImg.next())));
        }

        Iterator<FutureTask> iFT = arrFT.iterator();

        while (iFT.hasNext()) {
            es.submit(iFT.next());
        }

        iFT = arrFT.iterator();

        while (iFT.hasNext()) {
            try {
                iFT.next().get();
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
		
		itImg = null;
		WORKING_IMG_SET = null;
		System.gc();
//		System.out.println("\tBD: Ball "+ ball.getBallId() + " done!");
		return ball;
    }
	
	

    @SuppressWarnings("unchecked")
	private class HLTDetect implements java.util.concurrent.Callable {

        private ImageContainer imgContainer;
        private float HLT[] = {-1.0f, -1.0f};

        public HLTDetect(ImageContainer imgContainer) {
            this.imgContainer = imgContainer;
        }

        public Object call() throws Exception {
            try {
				UUID idImg = imgContainer.getImageId();
//				BufferedImage imgP = ball.getWorkingImages().get(idImg);
//				Rectangle areaBD = ImageContainer.getRectangle(ball.getBALL_DETECTION());
				
				BufferedImage imgBuffer = ball.getWorkingImages().get(idImg);
				
//				long freeMemory8 = LPhelper.MemoryHelper.freeMemory();
//				long startTime8 = System.currentTimeMillis();
//				BufferedImage imgBuffer = imgP;
				
				
//				long freeMemory = LPhelper.MemoryHelper.freeMemory();
//				long startTime = System.currentTimeMillis();
                imgBuffer = ImageProcessing.convertToGrayscale(imgBuffer);
				
				
				//PlanarImage imgBufferP = ImageContainer.getImageCropFromPlanarImage2(imgP, areaBD);
				
//				long freeMemory2 = LPhelper.MemoryHelper.freeMemory();
//				long startTime2 = System.currentTimeMillis();
//				imgBuffer = imgBuffer.getSubimage(areaBD.x, areaBD.y, areaBD.width, areaBD.height);
				//BufferedImage imgBuffer = imgBufferP.getAsBufferedImage();
				
//				long freeMemory3 = LPhelper.MemoryHelper.freeMemory();
//				long startTime3 = System.currentTimeMillis();
                
//				long freeMemory4 = LPhelper.MemoryHelper.freeMemory();
//				long startTime4 = System.currentTimeMillis();
				ImageProcessing.blendFilterGray(ball.getBlend(),imgBuffer);
                
//				long freeMemory5 = LPhelper.MemoryHelper.freeMemory();
//				long startTime5 = System.currentTimeMillis();
				imgBuffer = ImageProcessing.clearOutside(imgBuffer,ball.getBALL_DETECTION());
                
//				long freeMemory6 = LPhelper.MemoryHelper.freeMemory();
//				long startTime6 = System.currentTimeMillis();
                this.HLT = ImageLabeling.findTh(imgBuffer, 230);
                
                if (HLT[0] == -1 && HLT[1] == -1) 
				{
                    this.HLT = ImageLabeling.findTh(imgBuffer, ball.getUSER_THRESHOLD());
                    //System.out.println(LogHelper.printArray(HLT));
                    if (HLT[0] != -1 && HLT[1] != -1) 
					{
						Float hlt_aux[] = {HLT[0], HLT[1]};
						ball.getHL_DETECTION().put(idImg, hlt_aux);
                    }
                } 
				else 
				{
					Float hlt_aux[] = {HLT[0], HLT[1]};
					ball.getHL_DETECTION().put(idImg, hlt_aux);
                }
//				long freeMemory7 = LPhelper.MemoryHelper.freeMemory();
//				long startTime7 = System.currentTimeMillis();
				
//				System.out.println("Fase0: time-> "+ (startTime-startTime8) + "\t mem-> "+ ((freeMemory-freeMemory8)/(1024*1024)));
//				System.out.println("Fase1: time-> "+ (startTime2-startTime) + "\t mem-> "+ ((freeMemory2-freeMemory)/(1024*1024)));
//				System.out.println("Fase2: time-> "+ (startTime3-startTime2) + "\t mem-> "+ ((freeMemory3-freeMemory2)/(1024*1024)));
//				System.out.println("Fase3: time-> "+ (startTime4-startTime3) + "\t mem-> "+ ((freeMemory4-freeMemory3)/(1024*1024)));
//				System.out.println("Fase4: time-> "+ (startTime5-startTime4) + "\t mem-> "+ ((freeMemory5-freeMemory4)/(1024*1024)));
//				System.out.println("Fase5: time-> "+ (startTime6-startTime5) + "\t mem-> "+ ((freeMemory6-freeMemory5)/(1024*1024)));
//				System.out.println("Fase6: time-> "+ (startTime7-startTime6) + "\t mem-> "+ ((freeMemory7-freeMemory6)/(1024*1024)));
				
				
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

	public ArrayList<ImageContainer> getWORKING_IMG_SET() {
		return WORKING_IMG_SET;
	}

	public void setWORKING_IMG_SET(ArrayList<ImageContainer> WORKING_IMG_SET) {
		this.WORKING_IMG_SET = WORKING_IMG_SET;
	}

	public Sphere getBall() {
		return ball;
	}

	public void setBall(Sphere ball) {
		this.ball = ball;
	}

//	public float getScale() {
//		return scale;
//	}
//
//	public void setScale(float scale) {
//		this.scale = scale;
//	}
	
	
	
	
}
