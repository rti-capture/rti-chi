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
import XMLcarrier.AreaInfo;
import XMLcarrier.Data;
import XMLcarrier.Exceptions.UUIDNotFound;
import XMLcarrier.ImageFile;
import XMLcarrier.Parameter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import XMLcarrier.XMLHandler;
import highlightdetect.helpers.Sphere;
import highlightdetect.imagehelpers.ImageLabeling;
import highlightdetect.imagehelpers.ImageProcessing;
import highlightdetect.imagehelpers.SubImageSaveHelper;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.JAI;
//import LPhelper.DngManipulation;


public class HighlightDetect implements Runnable {
	
	
	public static final String XML_VERSION_TAG = "Highlight Detect V1.0";
	private StringBuffer XMLpath = null;
	private UUID id = null;
	private XMLHandler carrier = null;
//	private TreeMap<UUID,BufferedImage> workingImages;


	public HighlightDetect(StringBuffer XMLurl) {

		XMLpath = XMLurl;
		id = UUID.nameUUIDFromBytes(XML_VERSION_TAG.getBytes());
	}

    @SuppressWarnings("unchecked")
	public void run() {

		Data processData = null;
		carrier = new XMLHandler(XMLpath.toString());
		try {
			carrier.loadXML();
			processData = carrier.getDataByUUID(carrier.getInputReferences(id.toString()).get("HLi"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		ArrayList<FutureTask> arrFT = new ArrayList<FutureTask>();
		ExecutorService es = Executors.newCachedThreadPool();
		ArrayList<AreaInfo> areasInfo = processData.getAreas();		
		
		TreeMap<UUID,AreaInfo> areasInfoMap = new TreeMap<UUID,AreaInfo>();
		for(AreaInfo ai : areasInfo)
		{
			UUID ballId = ai.getAreaId();
			areasInfoMap.put(ballId,ai);
		}
		
//		System.out.println("HLT BallsSize: " + areasInfo.size());
		
		
		
//		long freeMemory = LPhelper.MemoryHelper.freeMemory();
//		long startTime = System.currentTimeMillis();
//		TreeSet<UUID> usedImages = new TreeSet<UUID>();
//		for(AreaInfo ai : areasInfo)
//		{
//			for (UUID id_img : carrier.getImagePtr(ai.getFileGroupID())) 
//			{
//				usedImages.add(id_img);
//			}
//		}
//		System.out.println("HLT NumImgsUsed: " + usedImages.size());
//		System.out.println("\tHLT: Loading images!");
		
		
//		workingImages = new TreeMap<UUID,BufferedImage>();
//		for(UUID s : usedImages)
//		{
//			try {
//				ImageFile image = carrier.getImageByUUID(s);
//				
//				File fileImage = new File(image.getUrl());
//				BufferedImage imgP = null;
//				if(ImageContainer.isDNG(fileImage))
//					imgP = ImageContainer.getDNGImage(fileImage);
//				else
//					imgP = JAI.create("url", fileImage.toURI().toURL()).getAsBufferedImage();
//				workingImages.put(s, imgP);
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
//		}
//		System.out.println("\tHLT: Loading images done!");
		
		
		TreeSet<UUID> usedImages = new TreeSet<UUID>();
        TreeMap<UUID, Sphere> Balls = new TreeMap<UUID, Sphere>();
		for(AreaInfo ai : areasInfo)
		{
			try {
				ArrayList<UUID> areaImgsIds = carrier.getImagePtr(ai.getFileGroupID());
				ArrayList<ImageFile> areaImgs = new ArrayList<ImageFile>();
				for(UUID id_img : areaImgsIds) {
					areaImgs.add(carrier.getImageByUUID(id_img));
				}
				UUID ballId = ai.getAreaId();
				TreeMap<UUID,ImageFile> imgsURL = new TreeMap<UUID,ImageFile>();
				for(ImageFile imgF : areaImgs)
				{
					imgsURL.put(UUID.fromString(imgF.getUuid()),imgF);
				}
				float x = (Float.valueOf(ai.getAttribute("x")));
				float y = (Float.valueOf(ai.getAttribute("y")));
				float r = (Float.valueOf(ai.getAttribute("r")));
				int tre = Integer.valueOf(ai.getAttribute("threshold"));
				String projectName = ai.getAttribute("projectName");
				String projectPath = ai.getAttribute("projectPath");
				Point2D point = ai.getBegin();
				Sphere ball = new Sphere(ballId, point, projectName, projectPath, x, y, r, tre, imgsURL, new TreeMap<UUID,BufferedImage>());
//				arrFT.add(new FutureTask(new HighlightDetectThread(ball)));
				
				for (UUID id_img : areaImgsIds) {
					usedImages.add(id_img);
				}

				Balls.put(ballId, ball);
				
			} catch (UUIDNotFound ex) {
				ex.printStackTrace();
			}
		}	
		
		String used_dng = carrier.getProjectInfo().getParamterByName("DNG image");
		int dng_image = -1;
		if(used_dng!=null&&!used_dng.equals("")){
			try{
				dng_image = Integer.parseInt(used_dng.trim());
			}
			catch(NumberFormatException e){
			}
		}
        for (UUID s : usedImages) {
            try {
                String projectpath = carrier.getProjectInfo().getParamterByName("ProjectPath")+File.separator;
                ImageFile image = carrier.getImageByUUID(s);
                File fileImage = new File(projectpath+image.getUrl());
                BufferedImage imgP = null;
//                if (DngManipulation.isDNG(fileImage)) {
//                    imgP = DngManipulation.getDNGImage(fileImage,dng_image);
//                } else {
                    imgP = JAI.create("url", fileImage.toURI().toURL()).getAsBufferedImage();
  //              }
                if (imgP != null) {
                    for (Sphere ball : Balls.values()) {
						if (ball.getImages().containsKey(s)) {
							Rectangle areaBD = Sphere.getRectanglefromSphere(ball.getBALL_DETECTION(),0);
//							ball.getWorkingImages().put(s, imgP.getSubimage(Math.round(areaBD.x), Math.round(areaBD.y), Math.round(areaBD.width), Math.round(areaBD.height)));
							BufferedImage cropImg = imgP.getSubimage(Math.round(areaBD.x), Math.round(areaBD.y), Math.round(areaBD.width), Math.round(areaBD.height));
							cropImg = ImageProcessing.convertToGrayscale(cropImg);
							ImageProcessing.blendFilterGray(ball.getBlend(),cropImg);
							cropImg = ImageProcessing.clearOutside(cropImg,ball.getBALL_DETECTION());
							float[] HLT = ImageLabeling.findTh(cropImg, 230);
							if (HLT[0] == -1 && HLT[1] == -1) 
							{
								HLT = ImageLabeling.findTh(cropImg, ball.getUSER_THRESHOLD());
								//System.out.println(LogHelper.printArray(HLT));
								if (HLT[0] != -1 && HLT[1] != -1) 
								{
									Float hlt_aux[] = {HLT[0], HLT[1]};
									ball.getHL_DETECTION().put(s, hlt_aux);
								}
							} 
							else 
							{
								Float hlt_aux[] = {HLT[0], HLT[1]};
								ball.getHL_DETECTION().put(s, hlt_aux);
							}
							cropImg = null;
							System.gc();
						}
                    }
                    imgP = null;
                    System.gc();
                }
            //workingImages.put(s, imgP);
            } catch (Exception ex) {
                Logger.getLogger(HighlightDetect.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
		
		
//		
//        for (Ball ball : Balls.values()) {
//                arrFT.add(new FutureTask(new HighlightDetectThread(ball)));
//        }
//		
//
//		ArrayList<Ball> arrBall = new ArrayList<Ball>();
//		Iterator<FutureTask> iFT = arrFT.iterator();
//
//		while (iFT.hasNext()) {
//			es.submit(iFT.next());
//		}
//
//		iFT = arrFT.iterator();
//		while (iFT.hasNext()) {
//			try {
//				arrBall.add((Ball) iFT.next().get());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//		for (Ball b : arrBall) {
//			for(Float[] hlt : b.getHL_DETECTION().values())
//			{
//				System.out.println("\tHLT result: x:" + hlt[0] + "  y:" + hlt[1] + "!!");
//			}
//		}
//		
		
		
		Data HLToutput = null;
		try {
			UUID HLToutputId = UUID.fromString(carrier.getOutputReferences(id.toString()).get("HLo"));
			HLToutput = new Data(HLToutputId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int k = 1;
//		Iterator<Ball> resultBall = arrBall.iterator();
		Iterator<Sphere> resultBall = Balls.values().iterator();
		while (resultBall.hasNext()) {

			Sphere ball = resultBall.next();
			AreaInfo ai = areasInfoMap.get(ball.getBallId());
			ai.setA(new TreeMap<String,String>());
			UUID HLresultId = UUID.randomUUID();
			ai.addAreaAttribute("Ref_HighLights", HLresultId.toString());
			Data HLresult = new Data(HLresultId);
			int j = 0;
			for(UUID imgID : ball.getHL_DETECTION().keySet())
			{
				j++;
				HLresult.addParameter(new Parameter("x"+j, ( ((ball.getHL_DETECTION().get(imgID)[0]+ball.getBALL_DETECTION()[0]-ball.getBALL_DETECTION()[2])))));
				HLresult.addParameter(new Parameter("y"+j, ( ((ball.getHL_DETECTION().get(imgID)[1]+ball.getBALL_DETECTION()[1]-ball.getBALL_DETECTION()[2])))));
				HLresult.addParameter(new Parameter("img"+j, imgID));
			}
			HLresult.addParameter(new Parameter("numBalls", j));
			ImageFile blendImg = createBlendImage(ball,k);
			try {
				ArrayList<ImageFile> fg = new ArrayList<ImageFile>();
                                ImageFile bImg = blendImg;
                                String blendname = (new File(bImg.getUrl())).getName();
                                bImg = new ImageFile(bImg.getMimetype(),
                                    carrier.getProjectInfo().getAssemblyFilesDirectory()+File.separator+blendname,
                                    bImg.getUuid(), bImg.getChecksum());
				fg.add(bImg);
				carrier.addFileGroup(new XMLcarrier.FileGroup(UUID.randomUUID(), "Blend Image", fg ));
//				carrier.addFileInfo(blendImg);
			} catch(Exception ex) {ex.printStackTrace();}
			ai.addAreaAttribute("BlendID", blendImg.getUuid());
			HLToutput.addAreaInfo(ai);
			carrier.addData(HLresult);
			k++;
		}

		carrier.addAreaData(HLToutput);
		try {
			carrier.writeXML();
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		
		
//		long freeMemory2 = LPhelper.MemoryHelper.freeMemory();
//		long stopTime = System.currentTimeMillis();
//		long runTime = stopTime - startTime;
//		long freeMemory3 = freeMemory - freeMemory2;
//		System.out.println("\tHLT: Run time: " + runTime+ "\n\t    Used mem: " + (freeMemory3/(1024*1024)));
//		
//		
		
		
    }

	public ImageFile createBlendImage(Sphere ball, int i)
	{
//		float bd[] = ball.getBALL_DETECTION();
//		float x = bd[0];
//		float y = bd[1];
//		float r = bd[2];
//		
//		BufferedImage blend = new BufferedImage(Math.round(r*2), Math.round(r*2), BufferedImage.TYPE_INT_RGB);
//		Rectangle areaBD = new Rectangle(Math.round(x-r),Math.round(y-r) , Math.round(r+r), Math.round(r+r));
//
//		for(PlanarImage imgP : workingImages.values())
//		{
//			ImageProcessing.blendFilter(blend, LPhelper.ImageContainer.getImageCropFromPlanarImage(imgP, areaBD));
//		}
		BufferedImage blend = ball.getBlend();
		String project_path = carrier.getProjectInfo().getParamterByName("ProjectPath")+File.separator+carrier.getProjectInfo().getAssemblyFilesDirectory();
		SubImageSaveHelper.SaveImage(blend, ball.getProjectName()+"_blend"+i+".jpg", project_path, true);
		String pathImg = project_path+File.separator+ball.getProjectName()+"_blend"+i+".jpg";
		return (new ImageFile("image/JPG", pathImg, UUID.randomUUID().toString(), ""));
	}
	
	public StringBuffer getXMLpath() {
		return XMLpath;
	}

	public void setXMLpath(StringBuffer XMLpath) {
		this.XMLpath = XMLpath;
	}

	public XMLHandler getCarrier() {
		return carrier;
	}

	public void setCarrier(XMLHandler carrier) {
		this.carrier = carrier;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

}
