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

package ModuleInterfaces;

import java.util.UUID;

/**
 *
 * @author rcosta
 */
public class PluginMetaInfo {

    private UUID componentID;
    private int stage;

    public PluginMetaInfo(UUID cID, int stage)
    {
        componentID = cID;
        this.stage = stage;
    }

    @Override
    public String toString()
    {
        return "PluginMetaInfo[ID=\"" + componentID.toString() + "\", State=" + stage + "]";
    }

    public int getStage()
    {
        return stage;
    }

    public UUID getID()
    {
        return componentID;
    }

}
