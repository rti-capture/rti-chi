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

package Plugin.helpers.LPparser;

import java.io.*;
import java.util.regex.*;
import java.util.Hashtable;

/**
 * LP File Parser
 * 
 * Parses and holds the contents of a valid .lp file.
 *
 * @author Rui Costa
 * @author João Barbosa
 */
public class LPparser {

    protected Hashtable<Integer,FileLD> table;

    public LPparser(File path) throws IOException
    {
        table = new Hashtable();

        // Check if path is a valid target
        if (!path.isFile())
            throw new IOException("File not found or path is not a file!");

        // Read the entire file and commit to memory.
        String content = readFileToString(path);

        // Now that the file is in memory, we'll parse it.
        float fl[] = new float[3];
        int Index = -1;
        int i = 0;

        String[] Lines = content.split("\n");

        for (String l : Lines) {
            Pattern p = Pattern.compile("(-0\\.[0-9]+|0\\.[0-9]+)");
            Matcher m = p.matcher(l);

            i=0;
            while (m.find()) {
                String fls = m.group();
                fl[i] = Float.parseFloat(fls);
                l = l.replaceAll(fls, "");
                i++;
            }

            if(i<3) continue;
            p = Pattern.compile("([0-9]+)");
            m = p.matcher(l);
            while (m.find()) {
                Index = Integer.parseInt(m.group());
            }

            table.put(Index, new FileLD( l, new Point3D(fl[0],fl[1],fl[2])));
        }

    }


    /**
     * Constructor taking the LP file's path as argument. It loads its data into
     * an hashtable which can be obtained through the getTable method. The parser
     * is not strict and may ignore errors in the LP file.
     *
     * Any fatal error results in a thrown exception.
     *
     * @param filePath Path to the LP input file
     * @throws IOException Thrown if there is any fatal input/output exception
     */
    public LPparser(String filePath) throws IOException
    {
        table = new Hashtable();

        // Check if path is a valid target
        File path = new File(filePath);
        if (!path.isFile())
            throw new IOException("File not found or path is not a file!");

        // Read the entire file and commit to memory.
        String content = readFileToString(path);

        // Now that the file is in memory, we'll parse it.
        float fl[] = new float[3];
        int Index = -1;
        int i = 0;

        String[] Lines = content.split("\n");

        for (String l : Lines) {
            Pattern p = Pattern.compile("(-0\\.[0-9]+|0\\.[0-9]+)");
            Matcher m = p.matcher(l);

            i=0;
            while (m.find()) {
                String fls = m.group();
                fl[i] = Float.parseFloat(fls);
                l = l.replaceAll(fls, "");
                i++;
            }

            if(i<3) continue;
            p = Pattern.compile("([0-9]+)");
            m = p.matcher(l);
            while (m.find()) {
                Index = Integer.parseInt(m.group());
            }

            table.put(Index, new FileLD( l, new Point3D(fl[0],fl[1],fl[2])));
        }

    }

    @Override
    public String toString()
    {
        return "LPparser={"+table.toString()+"}";
    }

    /**
     * Returns the Hashtable associated with this LPparser instance. This is the
     * actual table and not a copy, so use with care.<p>
     *
     * @return The Hashtable associated with this LPparser instance
     */
    public Hashtable<Integer,FileLD> getTable()
    {
        return table;
    }

    public static String readFileToString(File path) throws IOException
    {
        // Input stream for reading from
        InputStream is = new FileInputStream(path);

        // How big is the file?
        long length = path.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
            throw new IOException("File is too large to handle! (>" + Integer.MAX_VALUE + " bytes!)");
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+path.getName());
        }

        // Close the input stream and turn bytes into String
        is.close();

        String s = new String(bytes);

        return s;
    }

    @Deprecated
    public static PointContainer parseLPFile(File path) throws IOException
    {
        // Input stream for reading from
        InputStream is = new FileInputStream(path);

        // How big is the file?
        long length = path.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
            throw new IOException("File is too large to handle! (>" + Integer.MAX_VALUE + " bytes!)");
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+path.getName());
        }

        // Close the input stream and turn bytes into String
        is.close();

        String s = new String(bytes);

        // Call parser
        return parseLPFile(s);
    }

    @Deprecated
    public static PointContainer parseLPFile(String content)
    {
        float fl[] = new float[3];
        int Index = -1;
        int i = 0;

        PointContainer pc = new PointContainer();

        String[] Lines = content.split("\n");

        for (String l : Lines) {
            Pattern p = Pattern.compile("(-0\\.[0-9]+|0\\.[0-9]+)");
            Matcher m = p.matcher(l);

            i=0;
            while (m.find()) {
                String fls = m.group();
                fl[i] = Float.parseFloat(fls);
                l = l.replaceAll(fls, "");
                i++;
            }

            if(i<3) continue;
            p = Pattern.compile("([0-9]+)");
            m = p.matcher(l);
            while (m.find()) {
                Index = Integer.parseInt(m.group());
            }

            pc.addPoint(Index, new Point3D(fl[0],fl[1],fl[2]));
            //System.out.println("" + Index + " " + fl[0] + " " + fl[1] + " " + fl[2]);
            //}
        }

        return pc;
    }

    @Deprecated
    public static PointContainer parseLPFileStrict(String content)
    {
        /* Alright, we need to do the following steps to parse this.
         *
         * 1- Read the number of entries expected
         * 2- Initialize a PointContainer instance
         * 3- Parse line by line and add the entries read into the container
         * 4- Return the container to the caller.
         *
         * */

        // TESTING ONLY
        //System.out.println(content);

        // PointContainer declaration
        PointContainer pc = null;

        /*
         * 1- Read the number of entries expected
         * */

        // Initialize Pattern Matcher
        // First line Pattern plus Matcher
        Pattern firstLineP = Pattern.compile("^\\d+$");
        Matcher firstLineM = firstLineP.matcher("");
        // Float Pattern
        Pattern floatP = Pattern.compile("[+-]?\\d+\\.?\\d*");
        Matcher floatM = floatP.matcher("");
        // Integer Pattern
        Pattern intP = Pattern.compile("[+-]?\\d+");
        Matcher intM = intP.matcher("");

        //Pattern
        // Pattern firstLineP = Pattern.compile("^-?\\d+\\.?\\d*$");

        // Break up file into lines
        String[] lines = content.split("\n");

        /*
        for(String l : lines)
        {
            System.out.println(l);
        }*/

        // Sanity check
        if (lines.length<=0) { return pc; }

        // Read first line to initialize container
        if (firstLineM.reset(lines[0]).matches())
        {
            int num = Integer.decode(lines[0]);
            pc = new PointContainer(num);

            // DEBUGGING AID
            //System.out.println("Read there are: "+num);
        }
        else
        {
            // ERROR
            return pc;
        }

        /*
         * Iterate through every line and parse it for the data we want.
         * 
         * ASSUMPTIONS TAKEN:
         * 1- Filenames may have nested extensions, and only the name before the
         * first dot is evaluated
         * 2- Line format goes like this:
         * DSCF_0001.jpg -0.01813509 -0.33164996 0.9432282
         * Only "0001" and the three floating point numbers are interesting to
         * the parser, so only that is kept
         * 3- No assumption other than "Integer" is taken for reading the first
         * value. Other values are assumed to be floats
         * 4- Filenames are assumed, but not strictly, to have an absolute path
         * 5- Files are assumed to be valid - an error explicitly returns null
         * to the caller
         * */
        boolean first = true;
        for(String l : lines)
        {
            if(!first)
            {
                // Break-up line into words
                String[] words = l.split(" ");

                // Sanity check - Do we have at least 4 words?
                if (words.length<4) { return null; }

                float x,y,z;

                // Evaluate floating point values
                if ( floatM.reset(words[words.length-1]).matches() &&
                     floatM.reset(words[words.length-2]).matches() &&
                     floatM.reset(words[words.length-3]).matches() )
                {
                    x = Float.parseFloat(words[words.length-3]);
                    y = Float.parseFloat(words[words.length-2]);
                    z = Float.parseFloat(words[words.length-1]);
                }
                else
                {
                    return null;
                }

                // Let's get just the filename
                String filePath = "";

                // In case the filename had spaces in it, we must merge the
                // filename back together. And we exclude the floating point
                // values we just evaluated.
                for (int counter=0; counter<words.length-3; counter++)
                {
                    if (counter!=0)
                        filePath += " ";
                    filePath += words[counter];
                }

                // Now that we have the filename back, we break it up again,
                // removing directories.
                String pathing[] = filePath.split("[\\/]");

                // And now we get the actual filename
                String fileName = pathing[pathing.length-1];

                // Evaluate file name
                // Break-up extensions
                String[] fnp = fileName.split("\\.");

                // Break up by "_" tokens
                String[] substrs = fnp[0].split("_");

                // And now, let's match the last substring for this last breakup
                if (intM.reset(substrs[substrs.length-1]).matches())
                {
                    pc.addPoint(Integer.parseInt(substrs[substrs.length-1]),
                            new Point3D(x,y,z));
                }
                else
                {
                    return null;
                }
            }
            else
            {
                first = false;
            }
        }

        return pc;
    }

}
