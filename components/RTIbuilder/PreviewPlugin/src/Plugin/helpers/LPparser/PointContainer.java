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

import java.util.Hashtable;

/**
 * The PointContainer class is designed to hold a collection of Point3D instances
 * associated with key values.
 *
 * This class, though it uses a Hashtable behind the scenes, is not guaranteed to
 * be thread-safe, so no assumptions should be made about its behaviour under
 * concurrent accesses.
 *
 * @author Rui Costa
 */
public class PointContainer {

    private Hashtable<Integer,Point3D> table;
    private int initialSize;

    public PointContainer()
    {
        table = new Hashtable();
        initialSize = 0;
    }

    public PointContainer(int expectedNum)
    {
        table = new Hashtable(expectedNum);
        initialSize = expectedNum;
    }

    public void addPoint(int key, Point3D point)
    {
        table.put(key, point);
    }

    public Point3D getPointByKey(int key)
    {
        return table.get(key);
    }

    @Override
    public String toString()
    {
        String s = "Point Container: ";
        s += initialSize;
        s += " -> ";
        s += table.toString();

        return s;
    }

}
