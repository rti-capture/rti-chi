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

import Plugin.IPluginPanelView;
//import Plugin.PluginPanel;
import Plugin.OpenImageFilesTask;
import Plugin.OpenLPFileTask;
import Plugin.helpers.DomeSetup.DomeSetup;
import Plugin.helpers.LPparser.FileLD;
import Plugin.helpers.exceptions.FileManipulationFileExtensionInvalid;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Administrator
 */
public class InputReader {

	IPluginPanelView parent;

	public InputReader(IPluginPanelView parent) {
		this.parent = parent;
	}

        /**
         * Open an LP File and read the images, associating images with lighting
         * coordinates as well.
         *
         * NOT COMPLETED
         *
         * @param task Caller associated with the task
         * @param lp LP File
         * @param image_folder Image Folder where to find the images
         * @return True if task was cancelled before completion, False otherwise
         */
	public boolean openDomeLPFileV2(OpenLPFileTask task, File lp, File image_folder) 
        {
            // Create a DomeSetup instance, reading the LP File and associating with the image files
            DomeSetup domeSetup = null;
            try {
                domeSetup = new DomeSetup(lp, image_folder);
            } catch (IOException ex) {
                ex.printStackTrace();
                Logger.getLogger(InputReader.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Sanity check
            if (domeSetup == null) {return false;}
            if (domeSetup.getTable().size()<=0) {System.out.println("Empty DomeSetup instance, no images loaded??."); return false;}

            System.out.println("Attempting to open Dome Image Files");

            boolean canceled = openDomeImageFiles(task, domeSetup, image_folder);

            return !canceled;
        }

        public boolean openDomeImageFiles(OpenLPFileTask task, DomeSetup setup, File image_folder)
        {
            System.out.println(setup.getTable().toString());

            Hashtable fileTable = setup.getTable();

            // Do work
            UUID[] uuids = null;
            try {
                uuids = new UUID[fileTable.size()];
                for(int i=0;i<uuids.length;i++)
                    {uuids[i] = UUID.randomUUID();}
                Arrays.sort(uuids);
            } catch (Exception e) {
                e.printStackTrace();
            }
            

            Collection<FileLD> fileLDsC = fileTable.values();
            FileLD[] fileLDs = new FileLD[fileLDsC.size()];
            try {
                fileLDs = fileLDsC.toArray(fileLDs);
                Arrays.sort(fileLDs);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Not sorting fileLDs...
            //

            System.out.println(fileLDs);

            TreeMap<UUID,ImageContainer> imageTable = new TreeMap();

            boolean cancel = false;
            int i=0;
            for(FileLD fld : fileLDs)
            {
                if (task.isCancelled()) {
                    System.out.println("OpenDomeImageFiles:Task cancelled");
                    cancel = true;
                    break;
                }

                try{
                imageTable.put(uuids[i],fld.createContainer(uuids[i], image_folder.getAbsolutePath()));
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                i++;
                try {
                    task.setProgressBar(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (cancel) {
                parent.setProjectImages(null);
            } else {
                //Set the images in the plugin panel
                parent.setProjectImages(imageTable);
            }

            //parent.setProjectImages(imageTable);
            System.gc();

            System.out.println("ImgNum:"+imageTable.size());

            return cancel;
        }

	/**Opens the images files and store them in image containers.*/
	public void openImageFiles(OpenImageFilesTask task, File[] files) {


		//Creat a tree map to store the values
		TreeMap<UUID, ImageContainer> images = new TreeMap<UUID, ImageContainer>();
		ArrayList<File> files2 = new ArrayList<File>();
		int i;
		//Read all the files in the folder, and store the images
		try {
			for (i = 0; i < files.length; i++) {
				if (files[i].isFile() && FileManipulation.validExtension(files[i], "JPG")) {
					files2.add(files[i]);
				}
			}
		} catch (FileManipulationFileExtensionInvalid e) {
			e.printStackTrace();
		}
		//Generate a unique indentifier for all the images and sort them
		final int size = files2.size();
		UUID[] uuids = new UUID[size];
		for (i = 0; i < size; i++) {
			uuids[i] = UUID.randomUUID();
		}
		Arrays.sort(uuids);
		Collections.sort(files2);

		boolean cancel = false;
		//Create an image container with the images and the sorted ids, this way the images appear always in the same order
		for (i = 0; i < size; i++) {
			if (task.isCancelled()) {
				System.out.println("Task cancelled");
				cancel = true;
				break;
			}
			File image = files2.get(i);
			ImageContainer ic = new ImageContainer(uuids[i], image);
			images.put(ic.getImageId(), ic);
			task.setProgressBar(i);
		}

		if (cancel) {
			parent.setProjectImages(null);
		} else {
			//Set the images in the plugin panel
			parent.setProjectImages(images);
		}

		parent.setProjectImages(images);
		System.gc();
	}

	/**Opens a lp file, extracts the images and the light position values.*/
	public boolean openDomeLPFile(OpenLPFileTask task, File lp, File image_folder, String file_name, int start, int digit_number) {

		//A reader for the file
		BufferedReader br = null;
		String line;

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(lp)));

			int imag_counter = start;
			int image_number = 0;
			boolean canceled = false;
			//a map to store the lp values for each filename
			TreeMap<String, float[]> lpvalues = new TreeMap<String, float[]>();
			//float coords[][] = new float[num][3];
			ArrayList<File> files = new ArrayList<File>();

			//Read the image number, ignored
			int num = Integer.parseInt(br.readLine());

			// br.readLine() returns null if the file does not have more lines.
			//for all the images
			while ((line = br.readLine()) != null && !canceled) {
				//take the fornt and end white spaces
				line = line.trim();
				System.out.println("\n" + imag_counter + " " + line);
				String[] strs = line.split(" ");
				//it is a dome lp file

				if(!line.equals("")){
				
				if (strs.length == 3) {
					//take the lp coordinates
					float[] lp_value = new float[3];
					lp_value[0] = Float.parseFloat(strs[0]);
					lp_value[1] = Float.parseFloat(strs[1]);
					lp_value[2] = Float.parseFloat(strs[2]);

					//Generate the suposed file name that contains this coordinates
					StringBuilder start_value = new StringBuilder();
					for (int img = digit_number - 1; img > 0; img--) {
						if (imag_counter < ((int) (Math.pow(10, img)))) {
							start_value.append("0");
						}
					}
					start_value.append(imag_counter);
					String image_file_name = file_name.replace("%N", start_value.toString()).replace("%D", Integer.toString(imag_counter));

					lpvalues.put(image_file_name, lp_value);

				} //it is a rti lp file
				else{
                    if (strs.length > 3) {
					//it a normal lp, warn the user
                        JOptionPane.showMessageDialog(null, "Not the expected light position file. Check if this is really a dome LP file.", "LP File Error", JOptionPane.ERROR_MESSAGE);
                        canceled = true;
                    }
                    else{
                        JOptionPane.showMessageDialog(null, "Error during LP parse. Bad format.", "LP file erro", JOptionPane.ERROR_MESSAGE);
                        System.err.println("ERROR: Parsing LP file. Bad format.\n");
                    }
                }
				for (String s : strs) {
					System.out.print("%" + s);
				}
				imag_counter++;
				image_number++;
				}
			}
			// dispose all the resources after using them.
			br.close();

			// Loads image files. Happens when file does not explicitly has the name of image files in it.
			if (!canceled) {
				canceled = !openDomeFiles(task, lpvalues);
			}
			return !canceled;

		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "File not found.", "LP file error", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error in file parse.", "LP file error", JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
			Logger.getLogger(InputReader.class.getName()).log(Level.SEVERE, null, e);
			JOptionPane.showMessageDialog(null, "An error was ocurred during LP file parse.", "LP file error", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}

	/**Opens a lp file, extracts the images and the light position values.*/
	public boolean openLPFile(OpenLPFileTask task, File lp) {

		//A reader for the file
		BufferedReader br = null;
		String line;

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(lp)));
			//Read the image number
			int num = Integer.parseInt(br.readLine());
			int i = 0;

			//a map to store the lp values for each filename
			TreeMap<String, float[]> lpvalues = new TreeMap<String, float[]>();
			//float coords[][] = new float[num][3];
			File[] files = new File[num];

			boolean canceled = false;
			// br.readLine() returns null if the file does not have more lines.
			//for all the images
			while ((line = br.readLine()) != null && i < num + 1 && !canceled) {
				//take the fornt and end white spaces
				line = line.trim();
				System.out.println("\n" + i + " " + line);
				String[] strs = line.split(" ");
				//it is a dome lp file
				if (strs.length == 3) {
					//it a dome lp, warn the user
					JOptionPane.showMessageDialog(null, "Not the expected light position file. Check if this is not a dome LP file.", "LP File Error", JOptionPane.ERROR_MESSAGE);
					canceled = true;
				} //it is a rti lp file
				else if (strs.length > 3) {

					int aux = strs.length;
					System.out.println("VAlues; " + aux + " i:" + i);
					//File name
					String name = new String();
					//lp values
					float[] lp_value = new float[3];
					lp_value[0] = Float.parseFloat(strs[aux - 1]);
					lp_value[1] = Float.parseFloat(strs[aux - 2]);
					lp_value[2] = Float.parseFloat(strs[aux - 3]);
					//build the name, if it has spaces
					for (int j = 0; j < aux - 3; j++) {
						name += strs[j] + " ";
					}
					//Generate the file present in the lp file
					System.out.println(" name:" + name);
					File file = null;
					if (name.charAt(0) == '/' || name.contains(":")) {
						file = new File(name.trim());
					} else {
						file = new File(lp.getParent() + File.pathSeparator + name.trim());
					}
					files[i] = file;
					lpvalues.put(file.getName(), lp_value);
				} else {
					File f = new File(lp.getParent());
					String file_names[] = f.list();
					Arrays.sort(file_names);
					for (i = 0; i < file_names.length; i++) {
						files[i] = new File(file_names[i]);
					}
					canceled = true;
					JOptionPane.showMessageDialog(null, "Error during LP parse. Bad format.", "LP file error", JOptionPane.ERROR_MESSAGE);

					System.err.println("ERROR: Parsing LP file. Bad format.\n");
				}
				for (String s : strs) {
					System.out.print("%" + s);
				}
				i++;
			}
			// dispose all the resources after using them.
			br.close();

			// Loads image files. Happens when file does not explicitly has the name of image files in it.
			if (!canceled) {

				System.out.println("Files: " + files.length);
				//open the image files
				openFiles(task, files, lpvalues);
			}




			if (i != num) {
				System.err.println("WARNING: Parsing LP file. Number of images does not match.");
			}

			return !canceled;
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "File not found.", "LP file error", JOptionPane.ERROR_MESSAGE);
                    //e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error in file parse.", "LP file error", JOptionPane.ERROR_MESSAGE);
                    //e.printStackTrace();
		} catch (Exception e) {
                        //System.out.print("Exception! "+e.toString());
                        //System.out.flush();
                        //e.printStackTrace();
			JOptionPane.showMessageDialog(null, "An error was  ocurred during LP file parse.", "LP file error", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}

	/**Opens the image files refered in the lp file, and store them in Image containers
	 *@return true if successful, false otherwise.
	 */
	public boolean openFiles(OpenLPFileTask task, File[] files, TreeMap<String, float[]> lpvalues) {


		//A tree map to store the images
		TreeMap<UUID, ImageContainer> images = new TreeMap<UUID, ImageContainer>();
		ArrayList<File> files2 = new ArrayList<File>();
		int i;
		boolean error = false;
		//get all the valide files
		try {
			for (i = 0; i < files.length; i++) {
				//System.out.println("Is file: "+files[i].isFile()+ "extension: "+FileManipulation.validExtension(files[i], ":JPG"));
				if (files[i].isFile() && FileManipulation.validExtension(files[i], ":JPG")) {
					files2.add(files[i]);
				}
			}
		} catch (FileManipulationFileExtensionInvalid e) {
			e.printStackTrace();
		}

		//Generate a unique indentifier for all the images and sort them
		int size = files2.size();
		System.out.println("Files2: " + files2.size());
		UUID[] uuids = new UUID[size];
		for (i = 0; i < size; i++) {
			uuids[i] = UUID.randomUUID();
		}

		//Create an image container with the images and the sorted ids, this way the images appear always in the same order
		Arrays.sort(uuids);
		Collections.sort(files2);
		int loaded_number = 0;
		for (i = 0; i < size; i++) {
			if (task.isCancelled()) {
				System.out.println("Task cancelled in openFiles");
				error = true;
				break;
			}

			File image = files2.get(i);
			if (lpvalues.keySet().contains(files2.get(i).getName())) {

				ImageContainer ic = new ImageContainer(uuids[i], image);
				ic.setLp(lpvalues.get(files2.get(i).getName()));
				images.put(ic.getImageId(), ic);
				loaded_number++;
			}
			task.setProgressBar(i);
		}
		if (loaded_number != lpvalues.size()) {
			error = true;
			JOptionPane.showMessageDialog(null, "Some images are not present.", "Image loading error", JOptionPane.ERROR_MESSAGE);
		}
		if (!error) {
			System.out.println("Setting images: " + images.size());
			parent.setProjectImages(images);
			return true;
		}
		System.gc();
		return false;
	}

	/**Opens the image files refered in the lp file, and store them in Image containers.
	 *@return true if successful, false otherwise.
	 */
	public boolean openDomeFiles(OpenLPFileTask task, TreeMap<String, float[]> lpvalues) {


		//A tree map to store the images
		TreeMap<UUID, ImageContainer> images = new TreeMap<UUID, ImageContainer>();
		ArrayList<File> files2 = new ArrayList<File>();
		int i;
		boolean error = false;
        ArrayList<String> missing_files = new ArrayList<String>();

		//get all the valide files
		try {
			for (String filename : lpvalues.keySet()) {
				File f = new File(filename.trim());
				if (f.isFile() && FileManipulation.validExtension(f, ":JPG")) {
					files2.add(f);
				} else {
                    missing_files.add(filename);
				}
			}
		} catch (FileManipulationFileExtensionInvalid e) {
			e.printStackTrace();
		}

        if(!missing_files.isEmpty()&&!files2.isEmpty()){
            StringBuilder missing_message = new StringBuilder();
            missing_message.append("Unable to locate the folowing images:\n");
            for(String image: missing_files){
                missing_message.append("  "+image+"\n");
            }
            JOptionPane.showMessageDialog(null, missing_message.toString(), "Missing Images", JOptionPane.WARNING_MESSAGE);
        }
        if(files2.isEmpty()){
            error=true;
        }

		if (!error) {
			//Generate a unique indentifier for all the images and sort them
			int size = files2.size();
			System.out.println("Files2: " + files2.size());
			UUID[] uuids = new UUID[size];
			for (i = 0; i < size; i++) {
				uuids[i] = UUID.randomUUID();
			}

			//Create an image container with the images and the sorted ids, this way the images appear always in the same order
			Arrays.sort(uuids);
			Collections.sort(files2);
			for (i = 0; i < size; i++) {
				if (task.isCancelled()) {
					System.out.println("Task cancelled");
					error = true;
					break;
				}
				File image = files2.get(i);
				ImageContainer ic = new ImageContainer(uuids[i], image);
				ic.setLp(lpvalues.get(files2.get(i).getAbsolutePath()));
				images.put(ic.getImageId(), ic);
				task.setProgressBar(i);
			}
			System.out.println("Setting images: " + images.size());
			parent.setProjectImages(images);
			System.gc();
			return true;
		} else {
			JOptionPane.showMessageDialog(null, "No images found.", "Image loading error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
}

