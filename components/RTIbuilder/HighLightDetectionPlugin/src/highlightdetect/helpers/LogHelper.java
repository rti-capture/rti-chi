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
package highlightdetect.helpers;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author jbarbosa
 */
public class LogHelper {

    private String FileName;
    private boolean LogSave;

    public LogHelper(String FileName, boolean LogSave) {
        this.FileName = FileName;
        this.LogSave = LogSave;
    }

    public boolean LogMessage(String Message) {
        if (this.LogSave) {
            try {
                PrintStream log = new PrintStream(new FileOutputStream(new File(this.FileName), true));
                log.println("" + (new java.util.Date()).getTime() + " " + Message);
                log.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return true;
    }

    public static String printArray(int arr[]) {
        String ret = "[ ";
        for (int i = 0; i < arr.length; i++) {
            ret += arr[i] + ", ";
        }
        return ret.substring(0, ret.length() - 2) + " ]";
    }

    public static String printArray(float arr[]) {
        String ret = "[ ";
        for (int i = 0; i < arr.length; i++) {
            ret += arr[i] + ", ";
        }
        return ret.substring(0, ret.length() - 2) + " ]";
    }

    public static String printArray(double arr[]) {
        String ret = "[ ";
        for (int i = 0; i < arr.length; i++) {
            ret += arr[i] + ", ";
        }
        return ret.substring(0, ret.length() - 2) + " ]";
    }

    public static String printArray(long arr[]) {
        String ret = "[ ";
        for (int i = 0; i < arr.length; i++) {
            ret += arr[i] + ", ";
        }
        return ret.substring(0, ret.length() - 2) + " ]";
    }

    public static String printArray(String arr[]) {
        String ret = "[ ";
        for (int i = 0; i < arr.length; i++) {
            ret += arr[i] + ", ";
        }
        return ret.substring(0, ret.length() - 2) + " ]";
    }

    public static String printArray(boolean arr[]) {
        String ret = "[ ";
        for (int i = 0; i < arr.length; i++) {
            ret += arr[i] + ", ";
        }
        return ret.substring(0, ret.length() - 2) + " ]";
    }

    public static String printArray(int arr[][]) {
        String ret = "[ ";
        for (int i = 0; i < arr.length; i++) {
            ret += printArray(arr[i]) + ", ";
        }
        return ret.substring(0, ret.length() - 2) + " ]";
    }

    public static String printArray(float arr[][]) {
        String ret = "[ ";
        for (int i = 0; i < arr.length; i++) {
            ret += arr[i] + ", ";
        }
        return ret.substring(0, ret.length() - 2) + " ]";
    }

    public static String printArray(double arr[][]) {
        String ret = "[ ";
        for (int i = 0; i < arr.length; i++) {
            ret += printArray(arr[i]) + ", ";
        }
        return ret.substring(0, ret.length() - 2) + " ]";
    }

    public static String printArray(long arr[][]) {
        String ret = "[ ";
        for (int i = 0; i < arr.length; i++) {
            ret += printArray(arr[i]) + ", ";
        }
        return ret.substring(0, ret.length() - 2) + " ]";
    }

    public static String printArray(String arr[][]) {
        String ret = "[ ";
        for (int i = 0; i < arr.length; i++) {
            ret += printArray(arr[i]) + ", ";
        }
        return ret.substring(0, ret.length() - 2) + " ]";
    }

    public static String printArray(boolean arr[][]) {
        String ret = "[ ";
        for (int i = 0; i < arr.length; i++) {
            ret += printArray(arr[i]) + ", ";
        }
        return ret.substring(0, ret.length() - 2) + " ]";
   }
    
   
}
