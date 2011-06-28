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

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

/**
 * The iDataCache interface defines a data storage mechanism by piggybacking on the
 * available ConcurrentMap and related interfaces.
 *
 * @author rcosta
 */
public interface iDataCache<K,V> extends Map<K,V>, ConcurrentMap<K, V>
{

    /**
     * Get the ID of the DataCache instance. Useful to check whether the cache
     * has been changed.
     *
     * @return Current ID
     */
    public UUID getID();

    /**
     * Set a new ID for the DataCache instance.
     *
     * @param newID New ID
     */
    public void setID(UUID newID);

    /**
     * Clear the cache <i>and call the garbage collector</i>.
     *
     * Don't use if you do not want the garbage collector to fire. Equivalent to:
     * this.clear();
     * System.gc();
     */
    public void flush();

}
