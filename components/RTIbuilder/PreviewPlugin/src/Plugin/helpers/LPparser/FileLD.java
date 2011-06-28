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

import Plugin.helpers.ImageContainer;
import java.io.File;
import java.util.UUID;

/**
 *
 * @author rcosta
 */
public class FileLD implements Comparable {

    private String fileName;
    private Point3D<Float> lightInfo;

    public FileLD(String fName, Point3D lInfo)
    {
        fileName = fName.trim();
        lightInfo = lInfo;
    }

    public String getFileName()
    {
        return fileName;
    }

    public Point3D getLightInfo()
    {
        return lightInfo;
    }

    public void setFileName(String fName)
    {
        fileName = fName.trim();
    }

    public void setLightInfo(Point3D lInfo)
    {
        lightInfo = lInfo;
    }

    public ImageContainer createContainer(UUID uuid, String path)
    {
        File imgFile = new File(path+File.separator+fileName);
        ImageContainer img = new ImageContainer(uuid,imgFile);
        float[] f = {lightInfo.getX(), lightInfo.getY(), lightInfo.getZ()};
        img.setLp(f);

        return img;
    }

    @Override
    public String toString()
    {
        return "{"+fileName+","+lightInfo.toString()+"}";
    }

    public int compareTo(Object o) {
        if (this.getClass() != o.getClass())
        {
            throw new ClassCastException();
        }

        return this.fileName.compareTo(((FileLD)o).getFileName());
    }

}
