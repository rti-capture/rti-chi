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

package guicomponents.exceptions;


/**Exception thrown when the user trys to get a not closed area*/
public class AreaNotDefined extends Exception{

     public AreaNotDefined() {
        super();
    }

    public AreaNotDefined(String message) {
        super(message);
    }

    public AreaNotDefined(String message, Throwable cause) {
        super(message, cause);
    }

    public AreaNotDefined(Throwable cause) {
        super(cause);
    }
    
}
