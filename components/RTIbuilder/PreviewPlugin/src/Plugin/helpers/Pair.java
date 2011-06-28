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

package Plugin.helpers;

/**
 * Generic container for a pair of values. Can be useful in many circumstances,
 * like when you need to return more than one value, especially since you can
 * nest Pair objects within Pair objects to effectively construct binary trees.
 * Such "abuse" is not recommended, though, as it becomes unwieldy.
 *
 * @author Rui Costa
 */
public class Pair<T1,T2> {

    public T1 first;
    public T2 last;

    /**
     * Constructor that initializes the values
     *
     * @param a Inits first
     * @param b Inits last
     */
    public Pair (T1 a, T2 b)
    {
        first = a;
        last = b;
    }

    /**
     * Empty constructors, does NO initialization whatsoever. Only exists for
     * convenience, be careful using it.
     */
    public Pair()
    {

    }
}
