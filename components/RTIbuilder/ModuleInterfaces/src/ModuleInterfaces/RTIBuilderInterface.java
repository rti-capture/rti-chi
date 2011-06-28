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

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.UUID;
import XMLcarrier.XMLHandler;

/**
 *
 * @author Administrator
 */
public interface RTIBuilderInterface {

    public void setThumbnails(Map<UUID, BufferedImage> thumbnails);

    public void setXMLcarrier(StringBuffer XMLcarrierPath);

    public XMLHandler getXMLcarrier();
}
