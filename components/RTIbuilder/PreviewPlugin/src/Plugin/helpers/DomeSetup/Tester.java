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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Hashtable;
import Plugin.helpers.LPparser.Point3D;

/**
 *
 * @author rcosta
 */
public class Tester {

    public static void main(String[] args)
    {

        DomeSetup dome=null;

        //File f = new File("C:\\Users\\rcosta\\Documents\\NetBeansProjects\\RTIbuilder\\SX Temp\\src\\PLACA_A.lp");
        try {
            dome = new DomeSetup("C:\\Users\\rcosta\\Documents\\NetBeansProjects\\RTIbuilder\\SX Temp\\src\\PLACA_A.lp", "C:\\Users\\rcosta\\Documents\\NetBeansProjects\\RTIbuilder\\SX Temp\\TestData\\TESTE\\PLACA_A\\jpeg-exports\\");
            System.out.println(dome.toString());
        } catch (IOException ex) {
            Logger.getLogger(Tester.class.getName()).log(Level.SEVERE, null, ex);
        }

        /*
        Hashtable<String,Point3D> table=null;

        try {
            table = DomeLDSetup.loadDirectory("C:\\Users\\rcosta\\Documents\\NetBeansProjects\\RTIbuilder\\SX Temp\\TestData\\TESTE\\PLACA_A\\jpeg-exports\\","C:\\Users\\rcosta\\Documents\\NetBeansProjects\\RTIbuilder\\SX Temp\\src\\PLACA_A.lp");
            //PointContainer pc = LPparser.parseLPFile("38\nDSCF_0001.jpg -0.01813509 -0.33164996 0.9432282\nDSCF_0003.jpg -0.040955737 0.32322052 0.945437");

        } catch (IOException ex) {
            Logger.getLogger(Tester.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println(table.toString());*/

        //PointContainer pc = LPparser.parseLPFile("38\nDSCF_0001.jpg -0.01813509 -0.33164996 0.9432282\nDSCF_0003.jpg -0.040955737 0.32322052 0.945437");

    }


}
