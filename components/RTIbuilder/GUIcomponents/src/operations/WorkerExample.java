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

/**
 *
 * @author rcosta
 */
public class WorkerExample implements iTask {

    private int counter;

    public boolean openDataInBackground(Object inputData, Object outputData, OpenDataTask tasker) {

        String[] list = (String[])inputData;

        Byte[][] data = (Byte[][])outputData;

        counter = 0;
        for (String path : list)
        {
            if(tasker.isCancelled())
            {
                return false;
            }
            data[counter] = readFile(path);
            counter++;
            tasker.setProgressBar(counter);
        }

        return true;
    }

    public int getProgress() {
        return counter;
    }

    public void taskOnDone() {
        // Do whatever.
    }

    public void taskOnCancel() {
        // Do whatever.
    }

    Byte[] readFile(String path)
    {
        return new Byte[1];
    }
}
