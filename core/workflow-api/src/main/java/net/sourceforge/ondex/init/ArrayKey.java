package net.sourceforge.ondex.init;

import java.util.Arrays;

/**
 * @author lysenkoa
 *         Wrapper for arrays so they can be used as hash map keys correctly
 *         (equality and hash of arrays is based on their content)
 */
public class ArrayKey<T extends Object> {
    private T[] array = null;

    /**
     *
     */
    public ArrayKey() {
    }

    /**
     * @param array
     */
    public ArrayKey(T[] array) {
        this.array = array;
    }

    /**
     * @return
     */
    public T[] getArray() {
        return array;
    }

    /**
     * @param array
     */
    public void setArray(T[] array) {
        this.array = array;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);
    }

    /**
     * @return
     */
    public int size() {
        return array.length;
    }

    @Override
    public boolean equals(Object key) {
        return (key instanceof ArrayKey && Arrays.equals(array, ((ArrayKey) key).getArray()));
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("ArrayKey");
        sb.append("{array=").append(array == null ? "null" : Arrays.asList(array).toString());
        sb.append('}');
        return sb.toString();
    }
}
