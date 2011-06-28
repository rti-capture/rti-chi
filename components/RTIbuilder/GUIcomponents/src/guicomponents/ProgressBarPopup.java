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
package guicomponents;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ProgressMonitor;

/**Progress popup, to show to the user some process progress when such is viable.*/
public class ProgressBarPopup {

	/**The progress bar max value*/
	protected int max = Integer.MAX_VALUE;
	/**The progress bar min value*/
	protected int min = 0;
	/**The progress bar current value*/
	protected Integer progress = 0;
    /**The monitor*/
	protected ProgressMonitor progressMonitor;
	protected JDialog frame;
    /**True if the progress cannot be determined, false otherwise*/
	protected boolean inderteminate;
    /**The progress bar*/
	protected JProgressBar progressBar;

    /**ProgressBarPopup constructor, that makes the progress inderteminated*/
	public ProgressBarPopup() {
		this.inderteminate = true;
	}

    /**ProgressBarPopup constructor, that sets the maximum and minimum value for the process progress.
     *@param min the progress minimum.
     *@param max the progress maximum.
     */
	public ProgressBarPopup(int min, int max) {
		this.inderteminate = false;
		this.min = min;
		this.max = max;
	}

        public void setbounds(int min, int max)
        {
            this.min = min;
            this.max = max;
            if (progressBar != null)
            {
                progressBar.setMinimum(min);
                progressBar.setMaximum(max);
            }
        }

     /**Creates and shows a new ProgressBarPopup.
     *@param title the progress popup title.
     */
	public void createAndShowGUI(String title) {
		if(inderteminate) {
			frame = new JDialog((JFrame)null,title,true);
			frame.getRootPane().setWindowDecorationStyle(JOptionPane.INFORMATION_MESSAGE);
			frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			progressBar = new JProgressBar(min, max);
			progressBar.setIndeterminate(true);
			JPanel panel = new JPanel();
			panel.add(progressBar);
			frame.add(panel, BorderLayout.PAGE_START);
			panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			frame.setAlwaysOnTop(true);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);


		} else {
			frame = new JDialog((JFrame)null,title);
			progressMonitor = new ProgressMonitor(frame,
				title,
				"", min, max);
			progressMonitor.setProgress(min);
                        /*
                        progressMonitor.setMillisToPopup(0);
			frame.getRootPane().setWindowDecorationStyle(JOptionPane.INFORMATION_MESSAGE);
			frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			frame.setAlwaysOnTop(true);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);*/
		}
	}


	public int getProgress() {
		return progress;
	}

     /**Sets the progress value.
     *@param current the current progress value.
     */
	public void setProgress(int current) {
		this.progress = current;
		progressMonitor.setProgress(current);

	}

	public void setProgress(int current, String note) {
		this.progress = current;
		progressMonitor.setProgress(current);
		progressMonitor.setNote(note);
	}

	public ProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	public void setProgressMonitor(ProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	public void close() {
		if(inderteminate) {
			progressBar.setValue(max);
		} else {
			progressMonitor.setProgress(max);
		}
		frame.setVisible(false);
		frame.dispose();
	}

	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
//		final JFrame f  = new JFrame("WOW");
//		final ProgressBarPopup p = new ProgressBarPopup(0, 1000);
		final ProgressBarPopup p = new ProgressBarPopup();
		javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				p.createAndShowGUI("LOADINGG");
			}
		});
//
		System.out.println("INIT");
		Thread.sleep(3000);
		System.out.println("DONE");

//		for (int i = 0; i < 1000; i++) {
//			if(p.progressMonitor.isCanceled()) {
//				p.close();
//				break;
//			}
//			p.progressMonitor.setProgress(i);
//			p.progressMonitor.setNote("Ite "+i);
//			try {
//				Thread.sleep(15);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
		p.close();
		System.out.println("Close");
	}
}
