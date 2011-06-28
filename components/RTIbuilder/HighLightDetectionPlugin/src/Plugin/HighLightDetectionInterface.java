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
package Plugin;

import Exceptions.ArgumentException;
import Exceptions.ModuleException;
import Exceptions.XMLcarrierException;
import ModuleInterfaces.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.UUID;

public class HighLightDetectionInterface extends UserInteractionInterface {

    PluginPanel panel;

    public HighLightDetectionInterface() {
        panel = new PluginPanel();
        defineUserInterface(panel);
    }

    @Override
    public void start(StringBuffer XMLurl,boolean stageLoader) throws ArgumentException, ModuleException, XMLcarrierException {
        if (this.cache != null)
        {
            panel.cache = this.cache;
        }
        panel.start(XMLurl);
    }

    public void setMetaInfo(PluginMetaInfo pI)
    {
        this.plugInfo = pI;
        System.out.println("Outputting PluginMetaInfo just set:");
        System.out.println(pI.toString());
        System.out.println("");
        panel.plugInfo = pI;
    }

    @Override
    public void setImagesThumbnails(Map<UUID, BufferedImage> thumbnails) {
        //panel.setThumbnails(thumbnails);
    }

    @Override
    public void setParentApplication(RTIBuilderInterface parentApplication) {
    }
}
