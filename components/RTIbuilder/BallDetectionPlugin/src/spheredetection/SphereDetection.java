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

import XMLcarrier.Exceptions.UUIDNotFound;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import spheredetection.helpers.Area;
import XMLcarrier.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.JAI;

public class SphereDetection implements Runnable {

    public static final String XML_VERSION_TAG = "Ball Detection V1.0";
    private StringBuffer XMLpath = null;
    private UUID id = null;
    private XMLHandler carrier = null;
    private float scaleFactor;

    public SphereDetection(StringBuffer XMLurl) {

        XMLpath = XMLurl;
        id = UUID.nameUUIDFromBytes(XML_VERSION_TAG.getBytes());


    }

    private BufferedImage loadImage(UUID uuid) {
        try {
            ImageFile image = carrier.getImageByUUID(uuid);
            File fileImage = new File(image.getUrl());
            BufferedImage imgP = null;
          //  if (DngManipulation.isDNG(fileImage)) {
                //imgP = ImageContainer.getDNGImage(fileImage);
          //  } else {
                imgP = JAI.create("url", fileImage.toURI().toURL()).getAsBufferedImage();
           // }
            return imgP;
        } catch (Exception ex) {
            Logger.getLogger(SphereDetection.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public void run() {
        try
        {
            System.out.println("Starting Sphere Detection catch zone");
            specrun();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void specrun() {

        Data processData = null;

        carrier = new XMLHandler(XMLpath.toString());
        try {
            carrier.loadXML();
            processData = carrier.getDataByUUID(carrier.getInputReferences(id.toString()).get("BDi"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<FutureTask> arrFT = new ArrayList<FutureTask>();
        ExecutorService es = Executors.newCachedThreadPool();
        ArrayList<AreaInfo> areasInfo = processData.getAreas();
        TreeMap<UUID, Area> Areas = new TreeMap<UUID, Area>();
//		System.out.println("BD AreaSize: " + areasInfo.size());




        TreeSet<UUID> usedImages = new TreeSet<UUID>();
        int i = 0;
        for (AreaInfo ai : areasInfo) {
            ArrayList<UUID> areaImgsIds = carrier.getImagePtr(ai.getFileGroupID());
            ArrayList<ImageFile> areaImgs = new ArrayList<ImageFile>();
            System.gc();
            for (UUID id_img : areaImgsIds) {
                try {
                    areaImgs.add(carrier.getImageByUUID(id_img));
                } catch (UUIDNotFound ex) {
                    Logger.getLogger(SphereDetection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            scaleFactor = 1;//Float.valueOf(ai.getAttribute("scaleFactor"));
            UUID areaId = ai.getAreaId();
            Point2D xy = ai.getBegin();
            Point2D wh = ai.getEnd();
            int x = (int) (xy.getX() * scaleFactor);
            int y = (int) (xy.getY() * scaleFactor);
            int w = (int) ((wh.getX() - xy.getX()) * scaleFactor);
            int h = (int) ((wh.getY() - xy.getY()) * scaleFactor);
            int alg = Integer.valueOf(ai.getAttribute("algorithm"));
            boolean hou = Boolean.valueOf(ai.getAttribute("hough"));
            boolean bin = Boolean.valueOf(ai.getAttribute("binarize"));
            String projectName = ai.getAttribute("projectName");
            String projectPath = carrier.getProjectInfo().getParamterByName("ProjectPath")+File.separator+carrier.getProjectInfo().getAssemblyFilesDirectory();//ai.getAttribute("projectPath");
            Point2D point = ai.getBegin();


            Area area = new Area(areaId, point, i, projectName, projectPath, x, y, w, h, alg, hou, bin, areaImgs, new TreeMap<UUID, BufferedImage>());

            for (UUID id_img : areaImgsIds) {
                usedImages.add(id_img);
            }

            Areas.put(areaId, area);
            i++;
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
                ImageFile image = carrier.getImageByUUID(s);
                String prjPath = carrier.getProjectInfo().getParamterByName("ProjectPath")+File.separator;
                File fileImage = new File(prjPath + image.getUrl());
                BufferedImage imgP = null;
//                if (DngManipulation.isDNG(fileImage)) {
  //                  imgP =DngManipulation.getDNGImage(fileImage,dng_image);
    //            } else {
                    imgP = JAI.create("url", fileImage.toURI().toURL()).getAsBufferedImage();
      //          }
                if (imgP != null) {
                    for (Area area : Areas.values()) {
                        for (ImageFile imgF : area.getImages()) {
                            if (imgF.getUuid().equals(s.toString())) {
                                area.getWorkingImages().put(s, imgP.getSubimage(Math.round(area.getX()), Math.round(area.getY()), Math.round(area.getW()), Math.round(area.getH())));
                            }
                        }
                    }
                    imgP = null;
                    System.gc();
                }
            //workingImages.put(s, imgP);
            } catch (Exception ex) {
                Logger.getLogger(SphereDetection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
//		System.out.println("TAM:"+workingImages.size());
//		long freeMemory2 = LPhelper.MemoryHelper.freeMemory();
//		long stopTime = System.currentTimeMillis();
//		long runTime = stopTime - startTime;
//		long freeMemory3 = freeMemory - freeMemory2;
//		System.out.println("\tBD: Run time: " + runTime+ "\n\t    Used mem: " + (freeMemory3/(1024*1024)));

        String projectpath = carrier.getProjectInfo().getParamterByName("ProjectPath")+File.separator;
        for (Area area : Areas.values()) {
                arrFT.add(new FutureTask(new SphereDetectionThread(area,projectpath)));
        }

        ArrayList<Area> arrCirc = new ArrayList<Area>();
        Iterator<FutureTask> iFT = arrFT.iterator();

        while (iFT.hasNext()) {
            es.submit(iFT.next());
        }

        iFT = arrFT.iterator();
        while (iFT.hasNext()) {
            try {
                arrCirc.add((Area) iFT.next().get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


//		long freeMemory4 = LPhelper.MemoryHelper.freeMemory();
//		long stopTime2 = System.currentTimeMillis();
//		long runTime2 = stopTime2 - startTime;
//		long freeMemory5 = freeMemory - freeMemory4;
//		System.out.println("\tBD: Run time: " + runTime2+ "\n\t    Used mem: " + (freeMemory5/(1024*1024)));
//				

        for (Area a : arrCirc) {
            System.out.println("\tBD Final: x:" + a.getBallDetection()[0] + "  y:" + a.getBallDetection()[1] + "  r:" + a.getBallDetection()[2]);
        }

        Data BDoutput = null;
        try {
            UUID BDoutputId = UUID.fromString(carrier.getOutputReferences(id.toString()).get("BDo"));
            BDoutput = new Data(BDoutputId);
            BDoutput.setName("BallDetectionOutput");
        } catch (Exception e) {
            e.printStackTrace();
        }

        i = 0;
        Iterator<Area> resultCirc = arrCirc.iterator();
        while (resultCirc.hasNext()) {
            Area areaRes = resultCirc.next();
			AreaInfo ai = null;
			for(AreaInfo ai_aux : areasInfo) {
				if(ai_aux.getAreaId().equals(areaRes.getAreaId())) {
					ai = ai_aux;
					break;
				}
			}
			
            try {
                ArrayList<ImageFile> fg = new ArrayList<ImageFile>();
                ImageFile edge = areaRes.getEdge();
                String edgename = (new File(edge.getUrl())).getName();
                ImageFile median = areaRes.getMedian();
                String medianname = (new File(median.getUrl())).getName();
                edge = new ImageFile(edge.getMimetype(),
                        carrier.getProjectInfo().getAssemblyFilesDirectory()+File.separator+edgename,
                        edge.getUuid(), edge.getChecksum());
                median = new ImageFile(median.getMimetype(),
                        carrier.getProjectInfo().getAssemblyFilesDirectory()+File.separator+medianname,
                        median.getUuid(), median.getChecksum());
                fg.add(edge);
                fg.add(median);
                carrier.addFileGroup(new XMLcarrier.FileGroup(UUID.randomUUID(), "Stage1 and Stage2 images", fg));
//				carrier.addFileInfo(areaRes.getEdge());
//				carrier.addFileInfo(areaRes.getMedian());
            } catch (Exception e) {
                e.printStackTrace();
            }

            ai.setA(new TreeMap<String, String>());
            ai.addAreaAttribute("x", ((areaRes.getBallDetection()[0] / scaleFactor) + areaRes.getBeginPoint().getX()) + "");
            ai.addAreaAttribute("y", ((areaRes.getBallDetection()[1] / scaleFactor) + areaRes.getBeginPoint().getY()) + "");
            ai.addAreaAttribute("r", (areaRes.getBallDetection()[2] / scaleFactor) + "");
            ai.addAreaAttribute("MedianID", areaRes.getMedian().getUuid());
            ai.addAreaAttribute("EdgeID", areaRes.getEdge().getUuid());
            BDoutput.addAreaInfo(ai);
            i++;
        }

        carrier.addAreaData(BDoutput);
        try {
            carrier.writeXML();
        } catch (Exception e) {
            e.printStackTrace();
        }



//		
//		long freeMemory2 = LPhelper.MemoryHelper.freeMemory();
//		long stopTime = System.currentTimeMillis();
//		long runTime = stopTime - startTime;
//		long freeMemory3 = freeMemory - freeMemory2;
//		System.out.println("\tBD: Run time: " + runTime+ "\n\t    Used mem: " + (freeMemory3/(1024*1024)));
//		



    }

    public StringBuffer getXMLpath() {
        return XMLpath;
    }

    public void setXMLpath(StringBuffer XMLpath) {
        this.XMLpath = XMLpath;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
