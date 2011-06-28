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

package spheredetection.imagehelpers;
@SuppressWarnings("unchecked")
class Circulo implements java.lang.Comparable {

    private float x;
    private float y;
    private float r;

    public Circulo(float x, float y, float r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }

    public int compareTo(Object e) {
        Circulo c = (Circulo) e;
        if (getR() == c.getR()) {
            return 0;
        }
        if (getR() > c.getR()) {
            return -1;
        }
        return 1;
    }

    @Override
    public String toString() {
        return "Circle: (" + getX() + "," + getY() + "," + getR() + ")";
    }

    public

    float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }
}
