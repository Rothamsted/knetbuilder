package net.sourceforge.ondex.tools.auxfunctions;

/**
 * Auxiliary functions for parsers
 * TabArrayObject
 * <p/>
 * Container for lines of tab delimited data
 * <p/>
 * Usage:
 * <p/>
 * TabArrayObject tao = new TabArrayObject(Object[] oa);
 * // set array to contain contents
 * TabArrayObject tao = new TabArrayObject(String[] oa, Class[] c);
 * // obtain an array of Strings, and attempt conversion into specified types
 * <p/>
 * tao.setElement(int index, Object a);
 * // store object in index prescribed
 * <p/>
 * Object b = tao.getElement(int index);
 * // get object at specified index
 * <p/>
 * int s = tao.size();
 * // get size of container
 * <p/>
 * Notes:
 * TabArrayObject starts counting at 0
 *
 * @author sckuo
 */

public class TabArrayObject {

    private Object[] tdo;
//	private Class<?>[] typeOfObject;

    public TabArrayObject(int i) {

        this.tdo = new Object[i];

    }

    public TabArrayObject(Object[] oa) {

        this.tdo = oa;

    }

    public TabArrayObject(String[] oa, Class<?>[] c) {

        this.tdo = new Object[oa.length];

        if (oa.length == c.length) {

            // Process Strings, Char, Double, Int, Float

            for (int i = 0; i < oa.length; i++) {

                if (c[i] == null) {

                    this.tdo[i] = null;

                } else if (c[i] == String.class) {

                    this.tdo[i] = oa[i];

                } else if (c[i] == Integer.class) {

                    this.tdo[i] = Integer.parseInt(oa[i]);

                } else if (c[i] == Double.class) {

                    this.tdo[i] = Double.parseDouble(oa[i]);

                } else if (c[i] == Float.class) {

                    this.tdo[i] = Float.parseFloat(oa[i]);

                } else if (c[i] == Character.class) {

                    this.tdo[i] = oa[i].charAt(0);
                } else {

                    // TODO: Throw errors?

                }

            }

        } else {

            // TODO: throw some kind of error

        }

    }

    public void setElement(int index, Object a) {

        this.tdo[index] = a;

    }

    public Object getElement(int i) {

        Object a = null;
        try {
            a = this.tdo[i];
        } catch (Exception e) {

        }

        return a;

    }

    public int size() {

        return this.tdo.length;

    }

    public void debug() {

        System.out.println();
        for (int i = 0; i < tdo.length; i++) {

            System.out.println(i + " - " + tdo[i]);

        }
        System.out.println();
    }

}
