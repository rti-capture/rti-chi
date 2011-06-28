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
public class OpenImageFilesTask extends SwingWorker<Void, Integer> {

    private ProgressBarPopup pbp = null;
    private File[] selectedFiles = null;
    private InputReader reader = null;
    private PluginPanelView parent = null;

    public OpenImageFilesTask(ProgressBarPopup pbp, File[] selectedFiles, InputReader reader, PluginPanelView parent) {
        this.pbp = pbp;
        this.selectedFiles = selectedFiles;
        this.reader = reader;
        this.parent = parent;
    }

    public void setProgressBar(int i) {
        if (pbp.getProgressMonitor().isCanceled()) {
            this.cancel(true);
        } else {
            this.publish(new Integer(i));
        }
    }

    @Override
    protected Void doInBackground() {
        reader.openImageFiles(this, selectedFiles);
        return null;
    }

    @Override
    protected void process(List<Integer> pairs) {
        Integer progress = pairs.get(pairs.size() - 1);
        pbp.setProgress(progress, "Image " + progress);
    }

    @Override
    protected void done() {
        if (!isCancelled()) {
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
            parent.initPreviewMenu();
            parent.setCursor(null);
        } else {
            System.out.println("Task cancelled");
        }

    }
}
