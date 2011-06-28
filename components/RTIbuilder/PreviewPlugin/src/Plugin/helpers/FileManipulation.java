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

import Plugin.helpers.exceptions.FileManipulationFileExtensionInvalid;

public class FileManipulation {

    public static boolean validExtension(java.io.File f, String CONF_IMAGE_ALLOWED_TYPE) throws FileManipulationFileExtensionInvalid {


        if (f.isDirectory()) {
            throw new FileManipulationFileExtensionInvalid("File is a folder");
        }
		if((f.isHidden())||(f.getName().charAt(0)=='.')){
			return false;
		}
        String extension = getExtension(f);
       
        if (extension.trim().length() != 0 && CONF_IMAGE_ALLOWED_TYPE.toUpperCase().contains(extension.toUpperCase().trim())) {
            return true;
        } 
		else {
			return false;
        }
    }

    public static String getExtension(java.io.File f) {
        String ext = "";

        String s = f.getName();

        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }

        return ext;
    }

	public static String getSimpleName(java.io.File f) {
        String ext = "";

        String s = f.getName();

        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(0,i);
        }

        return ext;
    }


}
