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


//import Plugin.helpers.InputReader;
import guicomponents.ProgressBarPopup;
import java.awt.Cursor;
import java.io.File;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;


/**
 *
 * @author rcosta
 */
public class OpenDataTask<I,O> extends SwingWorker<Void, Integer> {

    final private ProgressBarPopup pbp; // = null;
    //private File[] selectedFiles = null;

    iTask task = null;
    I input = null;
    O output = null;
    String popupS = null;
    String progressS = null;
    boolean lastExecFinished = false;
    int max;
//    private InputReader reader = null;
//    private PluginPanelView parent = null;

    public OpenDataTask(iTask task, I input, O output, int progressCounter, String popupDisplay, String progressString)
    {
        this.task = task;
        this.input = input;
        this.output = output;
        this.popupS = popupDisplay;
        this.progressS = progressString;
        this.max = progressCounter;

        pbp = new ProgressBarPopup(0,progressCounter);

        /*
        Runnable pbpR = new Runnable() {

            public void run() {
                pbp.createAndShowGUI(popupS);
            }
        };

        SwingUtilities.invokeLater(pbpR);*/
        pbp.createAndShowGUI(popupS);
    }

    /*
    public OpenDataTask(iTask task, I input, O output, int progressCounter, String popupDisplay, String progressString, ProgressBarPopup popupW)
    {
        this.task = task;
        this.input = input;
        this.output = output;
        this.popupS = popupDisplay;
        this.progressS = progressString;
        this.max = progressCounter;

        pbp = popupW;
    }*/

    public void setProgressBar(int i) {
        if (pbp.getProgressMonitor().isCanceled()) {
            this.cancel(true);
        } else {
            this.publish(new Integer(i));
            //this.setProgress(i);
            //pbp.setProgress(i, progressS + i);
        }
    }

    @Override
    protected Void doInBackground() {
        lastExecFinished = true;
        boolean suc = task.openDataInBackground(input, output, this);
        if (!suc) this.cancel(false);
        return null;
    }

    @Override
    protected void process(List<Integer> pairs) {
        Integer progress = pairs.get(pairs.size() - 1); //task.getProgress(pairs);
        pbp.setProgress(progress, progressS + progress);
        this.setProgress( (progress*100) / (max * 100) );
    }

    @Override
    protected void done() {
        if (!isCancelled()) {
            //parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            pbp.close();
            task.taskOnDone();
            lastExecFinished = true;
            //parent.initPreviewMenu();
            //parent.setCursor(null);
        } else {
            lastExecFinished = false;
            System.out.println("Task cancelled");
            task.taskOnCancel();
        }

    }

    public boolean getLastExecFinished()
    {
        return lastExecFinished;
    }
}
