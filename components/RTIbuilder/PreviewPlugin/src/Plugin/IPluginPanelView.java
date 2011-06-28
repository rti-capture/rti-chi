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

import DataCache.iDataCache;
import Exceptions.ArgumentException;
import Exceptions.ModuleException;
import Exceptions.XMLcarrierException;
import Plugin.helpers.ImageContainer;
import java.awt.Container;
import java.io.File;
import java.util.TreeMap;
import java.util.UUID;

/**
 *
 * @author rcosta
 */
public interface IPluginPanelView {
    int OPEN_DOME_LP = 2;
    int OPEN_FILE_LP = 1;
    int OPEN_FOLDER = 0;

    void initPreviewMenu();

    void initPreviewMenu(boolean addLPinfo);

    void openDomeLPFile();

    void openFolder();

    void openLPFile();

    /**
     * Sets the plugin current selected folder
     */
    void setCurrentFolder(File folder);

    /**
     * Set the project images
     */
    void setProjectImages(TreeMap<UUID, ImageContainer> images);

    /**
     * Initializes the preview panel changing labels and visible components according to the user permission.
     */
    void start(StringBuffer XMLurl, int user_option) throws ArgumentException, ModuleException, XMLcarrierException;

    // Temporary, perhaps.
    public Container getParent();

    public iDataCache getDataCache();
    public void setDataCache(iDataCache cache);

}
