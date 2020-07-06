package net.sourceforge.ondex.ovtk2.util.listmodel;

public interface MutableListModel {

	/**
	 * Inserts non destructively the specified element at the specified position
	 * in this list.
	 * <p>
	 * Throws an <code>ArrayIndexOutOfBoundsException</code> if the index is out
	 * of range (<code>index &lt; 0 || index &gt; size()</code>).
	 * 
	 * @param index
	 *            index at which the specified element is to be inserted
	 * @param element
	 *            element to be inserted
	 */
	public void add(int index, Object element);

	/**
	 * Removes the element at the specified position in this list. Returns the
	 * element that was removed from the list.
	 * <p>
	 * Throws an <code>ArrayIndexOutOfBoundsException</code> if the index is out
	 * of range (<code>index &lt; 0 || index &gt;= size()</code>).
	 * 
	 * @param index
	 *            the index of the element to removed
	 */
	public void remove(int index);

	/**
	 * The number of objects in this list
	 */
	public int getSize();

}
