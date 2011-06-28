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

package operations;

import XMLcarrier.FileGroup;
import XMLcarrier.ImageFile;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

/**
 *
 * @author rcosta
 */
public class DefaultProjectImagesOpenTask implements iTask {

    //private PluginPanelView parent = null;
    protected OpenDataTask tasker;

    protected FileGroup fGroup = null;
    protected TreeMap<UUID,BufferedImage> imgMap = null;

    int progress = 0;

    protected boolean useCached = false;

    protected int imgWidth = 0;
    protected int imgHeight = 0;

    protected boolean doThumbs = false;

    protected String relativePathModifier = "";

    public DefaultProjectImagesOpenTask(boolean doThumbs)
    {
        this.doThumbs = doThumbs;
    }

    public DefaultProjectImagesOpenTask(boolean doThumbs, boolean useCache)
    {
        this.doThumbs = doThumbs;
        this.useCached = useCache;
    }

    public DefaultProjectImagesOpenTask(boolean doThumbs, boolean useCache, String relPath)
    {
        this.doThumbs = doThumbs;
        this.useCached = useCache;
        this.relativePathModifier = relPath;
    }

    public DefaultProjectImagesOpenTask(OpenDataTask tasker)
    {
        this.tasker = tasker;
    }

    public DefaultProjectImagesOpenTask(OpenDataTask tasker, boolean doThumbs)
    {
        this.tasker = tasker;
        //this.fGroup = fGroup;
        //this.imgMap = imgMap;

        this.doThumbs = doThumbs;
    }

    public DefaultProjectImagesOpenTask(OpenDataTask tasker, boolean doThumbs, String relPath)
    {
        this.tasker = tasker;
        //this.fGroup = fGroup;
        //this.imgMap = imgMap;

        this.doThumbs = doThumbs;
        this.relativePathModifier = relPath;
    }

    public void setRelativePath(String path)
    {
        this.relativePathModifier = path;
    }

    public void useCache()
    {
        this.useCached = true;
    }

    static public BufferedImage getImageBufferFromFileAsThumbnail(File image_file)
    {
        PlanarImage image = null;
        BufferedImage thumbnail = null;
        try {
            image = JAI.create("url", image_file.toURI().toURL());
            System.setProperty("com.sun.media.jai.disableMediaLib", "true");
            int width = image.getWidth();
            int height = image.getHeight();
            float scaleIcon = 65.0f / (Math.max(height, width));
            ParameterBlock params = new ParameterBlock();
            params.addSource(image);
            params.add(scaleIcon);
            params.add(scaleIcon);
            params.add(0.0f);
            params.add(0.0f);
            params.add(Interpolation.getInstance(Interpolation.INTERP_NEAREST));
            thumbnail = JAI.create("scale", params).getAsBufferedImage();
        } catch (MalformedURLException ex) {
            javax.swing.JOptionPane.showConfirmDialog(null, "Image not found: "+image_file.getAbsolutePath(),"Error message (Not supposed to GET here, it's a bug)",javax.swing.JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(DefaultProjectImagesOpenTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        return thumbnail;
    }

    static public BufferedImage getImageBufferFromFile(File image_file)
    {
        BufferedImage result = null;
        try {
            result = JAI.create("url", image_file.toURI().toURL()).getAsBufferedImage();
        } catch (MalformedURLException ex) {
            javax.swing.JOptionPane.showConfirmDialog(null, "Image not found: "+image_file.getAbsolutePath(),"Error message (Not supposed to GET here, it's a bug)",javax.swing.JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(DefaultProjectImagesOpenTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public boolean openDataInBackground(Object inputData, Object outputData, OpenDataTask tasker)
    {


        synchronized(this) {
        this.tasker = tasker;

        if (inputData != null)
            { fGroup = (FileGroup) inputData; }
        else
        {
            tasker.cancel(true);
            return false;
        }

        if (useCached == true && outputData != null)
        {
            imgMap = (TreeMap)outputData;
            //this.imgWidth =
            ArrayList<ImageFile> imglist = fGroup.getList(); //imgMap.firstKey();
            File f = new File(this.relativePathModifier+(imglist.get(0).getUrl()));
            BufferedImage bf = getImageBufferFromFile(f);
            this.imgWidth = bf.getWidth();
            this.imgHeight = bf.getHeight();
            return true;
        }

        if (outputData != null)
        {
            imgMap = (TreeMap)outputData;
            imgMap.clear();
        }
        else
        {
            tasker.cancel(true);
            return false;
        }

        // Now, we must interpret the FileGroup's data and read the image files
        ArrayList<ImageFile> imgList = fGroup.getList();
        ArrayList<UUID> uuidList = fGroup.getRefList();

        if (imgList.size() > 0) {
            if (imgMap == null)
                imgMap = new TreeMap();

            int i = 0;
            for (ImageFile imgF : imgList) {
                BufferedImage imgBuffer = null;
                UUID id = UUID.fromString(imgF.getUuid());//uuidList.get(i);

                File imageFile = new File(this.relativePathModifier+imgF.getUrl());
                if (imageFile != null && imageFile.isFile()) {
                    System.out.println("Reading image: "+i);
                    if (doThumbs) {
                        imgBuffer = getImageBufferFromFileAsThumbnail(imageFile);
                        if (this.imgHeight == this.imgWidth && this.imgWidth == 0)
                        {
                            BufferedImage imgBuffer2 = getImageBufferFromFile(imageFile);
                            imgWidth = imgBuffer2.getWidth();
                            imgHeight = imgBuffer2.getHeight();
                        }
                    } else {
                        imgBuffer = getImageBufferFromFile(imageFile);
                        imgWidth = imgBuffer.getWidth();
                        imgHeight = imgBuffer.getHeight();
                    }

                    if (imgBuffer == null)
                    {
                        tasker.cancel(false);
                        return false;
                    }

                    imgMap.put(id, imgBuffer);
                }
                else
                {
                    int pickedOption = javax.swing.JOptionPane.showConfirmDialog(null, "Image not found: "+imageFile.getAbsolutePath()+"\nContinue loading?","Error message",javax.swing.JOptionPane.YES_NO_OPTION);
                    if (pickedOption == javax.swing.JOptionPane.NO_OPTION)
                    {
                        return false;
                    }
                }

                i++;
                progress = i;
                tasker.setProgressBar(i);
            }
        }
        }
                        System.out.println("Final images (DPIOT): " + imgMap.toString());
                        System.out.flush();        

        return true;
    }

    public int getProgress() {
        return progress; //(Integer)pairs.get(pairs.size() - 1);
    }

    public void taskOnDone() {
        // Do nothing. This is mostly meant to be overriden if
        // specific code is needed
    }

    public void taskOnCancel() {
        // Do nothing. This is meant to be overriden if specific
        // code is needed
    }

    public void setTasker(OpenDataTask tasker) {
        this.tasker = tasker;
    }

    public TreeMap<UUID,BufferedImage> getData()
    {
        return imgMap;
    }

    public int getWidth()
    {
        return imgWidth;
    }

    public int getHeight()
    {
        return imgHeight;
    }

}
