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

package operations;

import java.lang.Byte;

/**
 *
 * @author rcosta
 */
public class InvokeExample implements Runnable {

    public void run()
    {
        String[] list = new String[2];
        list[0] = "filepath 1";
        list[1] = "filepath 2";

        Byte[][] data = new Byte[2][];

        int progCounter = 2;

        WorkerExample we = new WorkerExample();

        OpenDataTask tasker = new OpenDataTask(we, list, data, progCounter, "Reading files...", "File being read: ");
        tasker.execute();
    }
}
