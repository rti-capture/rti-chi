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

package Exceptions;


/**
 *  @author jbarbosa
 */
// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.2EB37276-0D42-715C-D3A5-09C083B4A5E5]
// </editor-fold> 
public class ModuleException extends Exception {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.B56C5389-7EB5-29FE-8409-F685E2258DE0]
    // </editor-fold> 
    public ModuleException () {
        super();
    }

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.CA4A2C30-077B-7EAA-94A0-5B0BE16052A3]
    // </editor-fold> 
    public ModuleException (String message) {
        super(message);
    }

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.669AEB41-C0F4-D2DF-47EA-7A3C870EE549]
    // </editor-fold> 
    public ModuleException (String message, Throwable cause) {
        super(message, cause);
    }

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.3724DDF1-888A-74EC-AFC1-A39C6E2300EF]
    // </editor-fold> 
    public ModuleException (Throwable cause) {
        super(cause);
    }

}

