/*
 *  Copyright (C) 2011 Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ncl.cs.nogold;

/**
 * A mathematical set of size two.
 *
 * @author Jochen Weile, M.Sc.
 */
public final class SetOfTwo<T extends Comparable<T>> implements Comparable<SetOfTwo<T>> {

    /**
     * An element of the set.
     */
    private final T a, b;

    /**
     * constructor with to elements
     * @param a
     * @param b
     */
    public SetOfTwo(T a, T b) {
        if(a == null || b == null) throw new NullPointerException("Can't construct a pair from null nodes");
        if (a.compareTo(b) >= 0) {
            this.a = a;
            this.b = b;
        } else {
            this.a = b;
            this.b = a;
        }
    }

    /**
     * gets an an element
     * @return
     */
    public T getA() {
        return a;
    }

    /**
     * gets the other element
     * @return
     */
    public T getB() {
        return b;
    }

    /**
     * returns whether this set equals another object
     * @param obj whether this set equals another object
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SetOfTwo<T> other = (SetOfTwo<T>) obj;
        if (!this.a.equals(other.a)) {
            return false;
        }
        if (!this.b.equals(other.b)) {
            return false;
        }
        return true;
    }

    /**
     * generates a hashcode for this set.
     * @return
     */
    @Override
    public int hashCode() {
        int hash = this.a.hashCode();
        hash = 59 * hash + this.b.hashCode();
        return hash;
    }

    /**
     * generates a string representation of this two-elemented set.
     * @return
     */
    @Override
    public String toString() {
        return "{"+a.toString()+","+b.toString()+"}";
    }

    @Override
    public int compareTo(SetOfTwo<T> o) {
        int ca = a.compareTo(o.a);
        return ca == 0 ? b.compareTo(o.b) : ca;
    }
}
