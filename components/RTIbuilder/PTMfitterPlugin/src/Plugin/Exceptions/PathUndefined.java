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

package Plugin.Exceptions;

/**
 *
 * @author matheus
 */
public class PathUndefined extends Exception{

    public PathUndefined(Throwable arg0) {
        super(arg0);
    }

    public PathUndefined(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public PathUndefined(String arg0) {
        super(arg0);
    }

    public PathUndefined() {
        super();
    }

}
