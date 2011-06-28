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

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.util.ArrayList;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

/**
 *
 * @author matheus
 * This class have all the necessary methods for handling images(crop,resize...)
 *
 */
public class ImageProcessing {

    public static boolean ImageResize(File f,String resizeDir,float scale,String outputname){
        boolean success = false;
        if(f != null){
            try{
                PlanarImage img = JAI.create("url", f.toURI().toURL());
                ParameterBlock pb = new ParameterBlock();
                pb.addSource(img);
                pb.add(scale);
                pb.add(scale);
                pb.add(0.0f);
                pb.add(0.0f);
                pb.add(new InterpolationNearest());

                BufferedImage transformedImage = JAI.create("scale", pb, null).getAsBufferedImage();

                JAI.create("filestore", transformedImage , resizeDir + outputname, "JPEG");

                success = true;
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    return success;
    }

    public static boolean ImageResize(File f,String resizeDir,float scale){
        boolean success = false;
        if(f != null){
            try{
                PlanarImage img = JAI.create("url", f.toURI().toURL());
                ParameterBlock pb = new ParameterBlock();
                pb.addSource(img);
                pb.add(scale);
                pb.add(scale);
                pb.add(0.0f);
                pb.add(0.0f);
                pb.add(new InterpolationNearest());

                BufferedImage transformedImage = JAI.create("scale", pb, null).getAsBufferedImage();

                JAI.create("filestore", transformedImage , resizeDir + f.getName(), "JPEG");
                success = true;

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    return success;
    }


    public static boolean ImageResize(File f,String resizeDir,float scaleX,float scaleY){
        boolean success = false;
        if(f != null){
            try{
                PlanarImage img = JAI.create("url", f.toURI().toURL());
                ParameterBlock pb = new ParameterBlock();
                pb.addSource(img);
                pb.add(scaleX);
                pb.add(scaleY);
                pb.add(0.0f);
                pb.add(0.0f);
                pb.add(new InterpolationNearest());

                BufferedImage transformedImage = JAI.create("scale", pb, null).getAsBufferedImage();

                JAI.create("filestore", transformedImage , resizeDir + f.getName(), "JPEG");
                success = true;

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    return success;
    }

    public static BufferedImage LoadImage(File f){
        BufferedImage bf = null;
        try{
            bf = JAI.create("url", f.toURI().toURL()).getAsBufferedImage();

        }catch(Exception e){
            e.printStackTrace();
        }

    return bf;

    }

    public static BufferedImage LoadImage(String fullPath){

        File f = new File(fullPath);
        BufferedImage bf = null;
        if(f.isFile()&&f.canRead()){

            try{
                bf = JAI.create("url", f.toURI().toURL()).getAsBufferedImage();

            }catch(Exception e){
                e.printStackTrace();
            }
        }

    return bf;

    }

    public static boolean Crop(File img,String cropDir,ArrayList<Point> pointList,boolean isFree){
        Polygon CROPPOLYGON = new Polygon();

        for(Point p: pointList){
                 CROPPOLYGON.addPoint(p.x,p.y);
         }

         Rectangle r = CROPPOLYGON.getBounds();
        
        //Rectangular crop

        BufferedImage bf = LoadImage(img);

        int[] cor = {0,0,0};


        ParameterBlock params = new ParameterBlock();
        params.addSource(bf);
        params.add((float) r.x);
        params.add((float) r.y);
        params.add((float) r.width);
        params.add((float) r.height);
        BufferedImage imgBuffer = JAI.create("crop", params).getAsBufferedImage();

        if(isFree){
            for(int wpixel =0; wpixel< r.width; wpixel++)
                for(int hpixel =0; hpixel< r.height; hpixel++)
                    if(!CROPPOLYGON.contains(wpixel+r.x, hpixel+r.y)){
                        imgBuffer.getRaster().setPixel(wpixel, hpixel, cor);
                }

        }

        JAI.create("filestore", imgBuffer , cropDir + img.getName(), "JPEG");
    return true;

    }

    public static BufferedImage PreviewCrop(File img,ArrayList<Point> pointList,boolean isFree){
        Polygon CROPPOLYGON = new Polygon();
        int[] widAndHeight = new int[2];

        for(Point p: pointList){
                 CROPPOLYGON.addPoint(p.x,p.y);
         }

         Rectangle r = CROPPOLYGON.getBounds();

        //Rectangular crop

        BufferedImage bf = LoadImage(img);
        BufferedImage imgBuffer;

        int[] cor = {0,0,0};


        ParameterBlock params = new ParameterBlock();
        params.addSource(bf);
        params.add((float) r.x);
        params.add((float) r.y);
        params.add((float) r.width);
        params.add((float) r.height);
        imgBuffer = JAI.create("crop", params).getAsBufferedImage();

        if(isFree){
            for(int wpixel =0; wpixel< r.width; wpixel++)
                for(int hpixel =0; hpixel< r.height; hpixel++)
                    if(!CROPPOLYGON.contains(wpixel+r.x, hpixel+r.y)){
                        imgBuffer.getRaster().setPixel(wpixel, hpixel, cor);
                }

        }

    return imgBuffer;

    }

    public static boolean CropAndResize(File f,String dir,float scaleX,float scaleY,
            ArrayList<Point> pointList,boolean isFree){
        boolean success = false;

        Polygon CROPPOLYGON = new Polygon();

        for(Point p: pointList){
                 CROPPOLYGON.addPoint(p.x,p.y);
         }

         Rectangle r = CROPPOLYGON.getBounds();

        //Rectangular crop

        BufferedImage bf = LoadImage(f);

        int[] cor = {0,0,0};


        ParameterBlock params = new ParameterBlock();
        params.addSource(bf);
        params.add((float) r.x);
        params.add((float) r.y);
        params.add((float) r.width);
        params.add((float) r.height);
        bf = JAI.create("crop", params).getAsBufferedImage();

        if(isFree){
            for(int wpixel =0; wpixel< r.width; wpixel++)
                for(int hpixel =0; hpixel< r.height; hpixel++)
                    if(!CROPPOLYGON.contains(wpixel+r.x, hpixel+r.y)){
                        bf.getRaster().setPixel(wpixel, hpixel, cor);
                }

        }



        //JAI.create("filestore", imgBuffer , dir + f.getName(), "JPEG");


        if(bf != null){
            try{
                //PlanarImage img = JAI.create("url", f.toURI().toURL());
                ParameterBlock pb = new ParameterBlock();
                pb.addSource(bf);
                pb.add(scaleX);
                pb.add(scaleY);
                pb.add(0.0f);
                pb.add(0.0f);
                pb.add(new InterpolationNearest());

                BufferedImage transformedImage = JAI.create("scale", pb, null).getAsBufferedImage();

                JAI.create("filestore", transformedImage , dir + f.getName(), "JPEG");
                success = true;

            }catch(Exception e){
                e.printStackTrace();
            }
        }
     Runtime.getRuntime().gc();
    return success;
    }

    public static BufferedImage convertToGrayscale(BufferedImage source) {
        BufferedImageOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        return op.filter(source, null);
    }

    public static int calculateLuminance(BufferedImage imgBuf){

            BufferedImage grayScale = convertToGrayscale(imgBuf);
            int luminance = 0;

			int blackHeight = grayScale.getHeight(),
					blackWidht = grayScale.getWidth();

			for (int h = 0; h < blackHeight; h++) {
				  for (int w = 0; w < blackWidht; w++) {
						float c[] = {0};
						grayScale.getRaster().getPixel(w, h, c);
						luminance += c[0];
				  }
			}
	  return luminance;
	  }

}
