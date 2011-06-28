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

package spheredetection.imagehelpers;

import java.io.File;
import javax.media.jai.JAI;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.PlanarImage;

public class SubImageSaveHelper {
    
    public static boolean SaveImage(BufferedImage image,String filename, String projectName, String WorkingDirectory,String Dir, Boolean SAVEIMG) {
        if(!SAVEIMG) return false;
		File dir = new File(WorkingDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (dir.isDirectory() && dir.canWrite()) {
			JAI.create("filestore", image , WorkingDirectory + File.separator + projectName + "_" + filename, "JPEG");
        }
		return true;
    }
	
    public static boolean SaveScaledImage(PlanarImage image, float scaleFactor, String filename, String WorkingDirectory,String Dir, Boolean SAVEIMG) {
        if(!SAVEIMG) return false;
		
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(image);
		pb.add(scaleFactor);
		pb.add(scaleFactor);
		pb.add(0.0f);
		pb.add(0.0f);
		pb.add(new InterpolationNearest());
		BufferedImage transformedImage = JAI.create("scale", pb, null).getAsBufferedImage();
		
		
        String wd = WorkingDirectory + File.separator + "subimages" + File.separator + Dir;
        File dir = new File(wd);
        if (!dir.exists()) {
            (new File(wd)).mkdirs();
        }
        if (dir.isDirectory() && dir.canWrite()) {
            JAI.create("filestore", transformedImage , wd + File.separator + filename, "JPEG");
        }       
        return true;
    }
	
	public static boolean SaveScaledImage(BufferedImage image, float scaleFactor, String filename, String WorkingDirectory,String Dir, Boolean SAVEIMG) {
        if(!SAVEIMG) return false;
		
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(image);
		pb.add(scaleFactor);
		pb.add(scaleFactor);
		pb.add(0.0f);
		pb.add(0.0f);
		pb.add(new InterpolationNearest());
		BufferedImage transformedImage = JAI.create("scale", pb, null).getAsBufferedImage();
		
		
        String wd = WorkingDirectory + File.separator + "subimages" + File.separator + Dir;
        File dir = new File(wd);
        if (!dir.exists()) {
            (new File(wd)).mkdirs();
        }
        if (dir.isDirectory() && dir.canWrite()) {
            JAI.create("filestore", transformedImage , wd + File.separator + filename, "JPEG");
        }       
        return true;
    }
	

}
