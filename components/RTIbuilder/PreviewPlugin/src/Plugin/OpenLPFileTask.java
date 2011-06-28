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

package Plugin;

import Plugin.helpers.ImageContainer;
import Plugin.helpers.InputReader;
import guicomponents.ProgressBarPopup;
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;
import javax.swing.SwingWorker;

/**
 *
 * @author rcosta
 */
public class OpenLPFileTask extends SwingWorker<Void, Integer> {

    private ProgressBarPopup pbp = null;
    private File selectedFile = null;
    private File imagesFolder = null;
    private File projectFolder = null;
    boolean suc, isDome;
    String filename;
    int start;
    int digit_number;
    private InputReader reader;
    private PluginPanelView parent;

    public OpenLPFileTask(ProgressBarPopup pbp, File selectedFile, File imagesFolder, File projectFolder, boolean isDome, String filename, int start, int digit_number, PluginPanelView parent, InputReader reader) {
        this.pbp = pbp;
        this.selectedFile = selectedFile;
        this.isDome = isDome;
        this.filename = filename;
        this.imagesFolder = imagesFolder;
        this.projectFolder = projectFolder;
        this.start = start;
        this.digit_number = digit_number;
        this.parent = parent;
        this.reader = reader;
    }

    public void setProgressBar(int i) {
        System.out.println("OpenLPFileTask.setProgressBar runs: "+i);
        if (pbp.getProgressMonitor().isCanceled()) {
            System.out.println("OpenLPFileTask.setProgressBar cancelled!");
            this.cancel(true);
        } else {
            System.out.println("OpenLPFileTask.setProgressBar publishes: "+i);
            this.publish(new Integer(i));
        }
    }

    @Override
    protected Void doInBackground() {
        if (isDome) {
            System.out.println("Starting openDomeLPFile");
            //suc = reader.openDomeLPFile(this, selectedFile, imagesFolder, filename, start, digit_number);
            suc = reader.openDomeLPFileV2(this, selectedFile, imagesFolder);
            System.out.println("Done openDomeLPFile? suc="+suc);
        } else {
            System.out.println("Starting openLPFile");
            System.out.flush();
            try {
            suc = reader.openLPFile(this, selectedFile); }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            System.out.println("Done openLPFile? suc="+suc);
        }

        if (!suc) {
            System.out.println("Task cancelada(?)");
            this.cancel(true);
        }
        return null;
    }

    @Override
    protected void process(List<Integer> pairs) {
        Integer progress = pairs.get(pairs.size() - 1);
        pbp.setProgress(progress, "Image " + progress);
    }

    @Override
    protected void done() {
        if (!isCancelled() && suc) {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            pbp.close();
            {
                System.out.println("OpenImageFilesTask: Cache ID:"+parent.getDataCache().getID());
                // Transform image containers into merely buffered images for portability
                TreeMap<UUID,BufferedImage> imgMap = new TreeMap();
                for(Entry<UUID,ImageContainer> eic : parent.getProjectImages().entrySet())
                {
                    imgMap.put(eic.getKey(), eic.getValue().getThumbnailImage());
                }

                parent.getDataCache().remove("Cached Original Thumbs");
                parent.getDataCache().put("Cached Original Thumbs", imgMap);
            }
            parent.setCurrentFolder(projectFolder);
            parent.initPreviewMenu();
            parent.setCursor(null);

        } else {
            System.out.println("Task cancelled");
        }

    }
}

