package net.sourceforge.ondex.ovtk2.ui.popup.custom.itemeditor;

import java.io.FileNotFoundException;
import java.text.ParseException;

/**
 * re-useable item editor
 * 
 * @author Martin Rittweger
 */

public interface ItemEditHandler {

	/** list of editable items */
	public String[] getItemNames();

	/**
	 * load item into editor
	 * 
	 * @throws FileNotFoundException
	 * @throws ParseException
	 */
	public void loadItem(String name) throws FileNotFoundException, ParseException;

	/** did the user change some item property since last load/save? */
	public boolean isChanged();

	/**
	 * save item to backing data structure
	 * 
	 * @throws FileNotFoundException
	 */
	public void saveItem() throws FileNotFoundException;

	/**
	 * create new, default item with the given name.
	 */
	public void newItem(String name);

	/** delete an item */
	public void deleteItem();

	/**
	 * set changed flag When implementing this interface, you should only care
	 * about setting the flag true when a change occours.
	 */
	public void setChanged(boolean changed);

	/** item currently edited, null of nothing is edited */
	public Object getItemName();

	/**
	 * no item should be edited now, such that, for instance,
	 * {@link #getItemName()} does return null
	 */
	public void clearItem();
}
