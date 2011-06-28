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

/**
 * File Mimetype discoverer class
 *
 * @author Rui Costa
 */
import javax.activation.MimetypesFileTypeMap;
import java.io.File;

class MimeTypeService {

    protected MimetypesFileTypeMap typeMap;

    public MimeTypeService()
    {
        typeMap = new MimetypesFileTypeMap();
    }

    public String getFileMimeType(File f)
    {
        return typeMap.getContentType(f);
    }

}