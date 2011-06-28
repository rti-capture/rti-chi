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

package Plugin.helpers.DomeSetup;

import java.util.Hashtable;
import java.io.File;
import java.io.IOException;
import java.util.regex.*;
import Plugin.helpers.LPparser.Point3D;
import Plugin.helpers.LPparser.PointContainer;
import Plugin.helpers.LPparser.LPparser;

/**
 * Dome information Setup class. Provides methods allowing the proper binding
 * of image files to their corresponding lighting information
 *
 * @author Rui Costa
 */
public class DomeLDSetup {


    /**
     * The loadDirectory method receives the directory path of the image files
     * to bind with lighting information provided in an LP file whose path is
     * also provided. The result is an hashtable with the file name as its key
     * and the lighting information as its value, contained in a Point3D class
     * instance.<p>
     * <p>
     * The LP file parser will skip over any entry in the LP file it cannot
     * read, thus errors in the LP file will not be caught this way. For a
     * parser that enforces much stricter LP file integrity, please refer to the
     * LPparser.parseLPFileStrict method.<p>
     * <p>
     * In case of a fatal error, the method will throw an IOException with appropriate
     * descriptive text.<p>
     * <p>
     * <b>Concurrency:</b> This method should be <i>thread safe</i>, but is not
     * assuredly so.<p>
     *
     * @param dirPath Path to the directory where the images reside
     * @param lpfilePath Path to the file with the lighting information.
     * @return An Hashtable with the filenames paired with their lighting information
     * @throws IOException Exception thrown if any IO operation fails
     */
    public static Hashtable<String,Point3D> loadDirectory(String dirPath, String lpfilePath) throws IOException
    {
        MimeTypeService mime = new MimeTypeService();
        Hashtable<String,Point3D> table = new Hashtable();
        PointContainer pc;


        // First step, let's make sure paths exists and such
        File dir = new File(dirPath);
        File lpfile = new File(lpfilePath);

        // Sanity checks
        if(!dir.isDirectory())
        {
            throw new IOException("dirPath is not a valid directory.");
        }
        if(!lpfile.isFile())
        {
            throw new IOException("lpfilePath is not a valid file.");
        }

        // Let's read the LPFILE first
        pc = LPparser.parseLPFile(lpfile);
        // Debug
        //System.out.println(pc.toString());

        // Get listing
        String[] files = dir.list();
        // Store imagetype found. All images in set have to be same type
        String imageType = "";

        System.out.println("Starting dir search with "+files.length+" files.");

        // Iterate over the listing
        for(String file : files)
        {
            File tempFile = new File(dirPath+file);

            // If it's a file, do...
            if(tempFile.isFile())
            {
                String fType = mime.getFileMimeType(tempFile);
                // Check if it's an image! If not, skip it
                if (fType.startsWith("image/"))
                {
                    // Debug
                    //System.out.println(fType);

                    // Set the image type we're interested in if it's not already
                    if (imageType.equals(""))
                    {
                        imageType = fType;
                    }

                    // Now let's find which point the file should be associated
                    // with!
                    int boundIndex=-1;
                    Pattern p = Pattern.compile("([0-9]+)");
                    Matcher m = p.matcher(file);
                    while (m.find()) {
                        boundIndex = Integer.parseInt(m.group());
                    }

                    // Sanity check coupled with image type check
                    if (boundIndex>=0 && fType.equals(imageType))
                    {
                        // Now that we, supposedly, have one...
                        Point3D point = pc.getPointByKey(boundIndex);

                        // Make sure a point exists for this file. If not, do
                        // not add
                        if (point!=null)
                            table.put(file, point);
                    }
                }
            }
            // And back to the top, until done with all files
        }

        return table;
    }



}
