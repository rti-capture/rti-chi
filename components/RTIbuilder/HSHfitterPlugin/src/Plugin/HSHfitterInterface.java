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

import ModuleInterfaces.RTIBuilderInterface;
import ModuleInterfaces.UserInteractionInterface;
import Plugin.helpers.ImageProcessing;
import XMLcarrier.XMLHandler;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.swing.JOptionPane;

/**
 *
 * @author matheus
 */
public class HSHfitterInterface extends UserInteractionInterface{

    PluginPanel panel;

    public HSHfitterInterface(){
        panel = new PluginPanel();
        defineUserInterface(panel);
    }

    @Override
    public void start(StringBuffer XMLurl,boolean loadStage) {
        if (this.cache!=null && this.cache != panel.getDataCache())
            panel.setDataCache(cache);
       panel.start(XMLurl);
    }

    @Override
    public void setImagesThumbnails(Map<UUID, BufferedImage> thumbnails) {
        //throw new UnsupportedOperationException("FittingPlugin doesnt need thumbnails");
        /*
        Iterator<UUID> i = thumbnails.keySet().iterator();
        int max = 0;
        UUID uuid = null;
        while(i.hasNext()){
            UUID auxUUID = i.next();
            int aux = ImageProcessing.calculateLuminance(thumbnails.get(auxUUID));
            //System.out.println("Luminance : " + aux);
            //System.out.println("Aux : " + aux + " Max : " + max);

            if(aux > max){
                uuid = auxUUID;
                max = aux;
            }
        }

        panel.imgGtLuminance = uuid;
        System.out.println("IMGGTLUMINANCE  : " + uuid.toString());

        try{
            XMLHandler xml = new XMLHandler(panel.xmlPath.toString());
            xml.loadXML();
            panel.bf = ImageProcessing.LoadImage(xml.getImageByUUID(uuid).getUrl());
            panel.AREA_SELECTION_PANEL.setImage(panel.bf);
            panel.SELECTION.setImage(panel.bf);

        }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
*/
    }

    @Override
    public void setParentApplication(RTIBuilderInterface parentApplication) {
    }

}
