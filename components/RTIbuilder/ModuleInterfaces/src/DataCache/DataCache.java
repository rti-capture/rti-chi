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

package DataCache;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The DataCache class implements a data storage mechanism by piggybacking on the
 * available ConcurrentHashMap implementation.
 *
 * @author rcosta
 */
public class DataCache extends ConcurrentHashMap implements iDataCache {

    protected UUID cache_id;

    public DataCache()
    {
        super();

        cache_id = UUID.randomUUID();
    }

    public DataCache(UUID id)
    {
        super();

        if (id!=null)
            cache_id = id;
        else
            cache_id = UUID.randomUUID();
    }

    public UUID getID()
    {
        return cache_id;
    }

    public void setID(UUID newID)
    {
        if(newID!=null)
            cache_id = newID;
    }

    public void flush() {
        this.clear();
        System.gc();
    }

}
