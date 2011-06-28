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

import java.lang.Number;
import java.lang.reflect.Array;

/**
 * Point3D Interface Implementation.<p>
 * <p>
 * This class implements a container for a 3D Point structure of a generic
 * numeric type T. <p>
 * <p>
 * <b>Concurrency:</b> This class is <i>not thread safe</i>.<p>
 *
 * @author Rui Costa
 */
public class Point3D<T>{

    protected T x,y,z;

    /**
     * Constructor taking a Point3D instance as input.
     *
     * @param p Point3D instance
     */
    public Point3D(Point3D<T> p)
    {
        x = p.getX();
        y = p.getY();
        z = p.getZ();
    }

    /**
     * Constructor taking separate values as input coordinates.
     *
     * @param x X Coord input
     * @param y Y Coord input
     * @param z Z Coord input
     */
    public Point3D(T x, T y, T z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Get X Coordinate value
     *
     * @return X Coordinate
     */
    public T getX() {
        return x;
    }

    /**
     * Get Y Coordinate value
     *
     * @return Y Coordinate
     */
    public T getY() {
        return y;
    }

    /**
     * Get Z Coordinate value
     *
     * @return Z Coordinate
     */
    public T getZ() {
        return z;
    }

    /**
     * Set the X Coordinate value
     *
     * @param num New X Coordinate value
     */
    public void setX(T num) {
        x = num;
    }

    /**
     * Set the Y Coordinate value
     *
     * @param num New Y Coordinate value
     */
    public void setY(T num) {
        y = num;
    }

    /**
     * Set the Z Coordinate value
     *
     * @param num New Z Coordinate value
     */
    public void setZ(T num) {
        z = num;
    }

    private static <T> T[] createArray(T... items)
    {
        return items;
    }

    /**
     * Get the Point3D's contents as an array
     * 
     * @return Array with the following contents: {getX(), getY(), getZ()}
     */
    public T[] getAsArray()
    {
        T[] arr = createArray(this.getX(), this.getY(), this.getZ());

        return arr;
    }

    @Override
    public String toString()
    {
        return "{"+x+","+y+","+z+"}";
    }

}
