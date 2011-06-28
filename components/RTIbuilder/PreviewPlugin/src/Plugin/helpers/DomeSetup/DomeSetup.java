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

import Plugin.helpers.LPparser.FileLD;
import Plugin.helpers.LPparser.LPparser;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The DomeSetup class is designed to read an LP File and the contents of a
 * directory with images associated with the LP File and create a logical binding
 * between the contents of the file and the contents of the directory. It is
 * thus used for setting up the lighting information associated with the images.
 *
 * @author Rui Costa
 */
public class DomeSetup {

    protected Hashtable<Integer,FileLD> table;

    public DomeSetup(File lpfile, File dir) throws IOException
    {
        table = new Hashtable();

        // Sanity checks
        if(!dir.isDirectory())
        {
            throw new IOException("dirPath is not a valid directory.");
        }
        if(!lpfile.isFile())
        {
            throw new IOException("lpfilePath is not a valid file.");
        }

        // Read LP File
        LPparser lpP = new LPparser(lpfile);

        // Get MimeTypeService up.
        MimeTypeService mime = new MimeTypeService();

        // Get listing
        String[] files = dir.list();
        // Store imagetype found. All images in set have to be same type
        String imageType = "";

        System.out.println("Starting dir search with "+files.length+" files.");

        // Iterate over the listing
        for(String file : files)
        {
            File tempFile = new File(dir.getPath()+File.separator+file);
            System.out.println(dir.getPath()+File.separator+file);

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
                        FileLD fLD = (FileLD)lpP.getTable().get(boundIndex);

                        // Make sure a point exists for this file. If not, do
                        // not add
                        if (fLD!=null && file!=null)
                            table.put(boundIndex, new FileLD(file,fLD.getLightInfo()));
                    }
                }
            }
            // And back to the top, until done with all files
        }

    }


    public DomeSetup(String lpfilePath, String dirPath) throws IOException
    {

        System.out.println("DomeSetup(\""+lpfilePath+"\",\""+dirPath+"\")");

        // First step, let's make sure paths exists and such
        File dir = new File(dirPath);
        File lpfile = new File(lpfilePath);

        table = new Hashtable();

        // Sanity checks
        if(!dir.isDirectory())
        {
            throw new IOException("dirPath is not a valid directory.");
        }
        if(!lpfile.isFile())
        {
            throw new IOException("lpfilePath is not a valid file.");
        }

        // Read LP File
        LPparser lpP = new LPparser(lpfilePath);

        // Get MimeTypeService up.
        MimeTypeService mime = new MimeTypeService();

        // Get listing
        String[] files = dir.list();
        // Store imagetype found. All images in set have to be same type
        String imageType = "";

        System.out.println("Starting dir search with "+files.length+" files.");

        // Iterate over the listing
        for(String file : files)
        {
            File tempFile = new File(dirPath+file);
            //System.out.println(dir.getPath()+file);

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
                        FileLD fLD = (FileLD)lpP.getTable().get(boundIndex);

                        // Make sure a point exists for this file. If not, do
                        // not add
                        if (fLD!=null && file!=null)
                            table.put(boundIndex, new FileLD(file,fLD.getLightInfo()));
                    }
                }
            }
            // And back to the top, until done with all files
        }

    }

    /**
     * Returns the Hashtable associated with this DomeSetup instance. This is the
     * actual table and not a copy, so use with care.<p>
     *
     * @return The Hashtable associated with this DomeSetup instance
     */
    public Hashtable<Integer,FileLD> getTable()
    {
        return table;
    }

    @Override
    public String toString()
    {
        return "Dome={"+table.toString()+"}";
    }


}
