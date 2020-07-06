package net.sourceforge.ondex.ovtk2.util.listmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;

import net.sourceforge.ondex.core.DataSource;

/**
 * Dynamic DataSource model.
 * 
 * @author hindlem, taubertj
 */
public class DataSourceListModel extends AbstractListModel {

	/**
	 * default
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * associated data sources
	 */
	private List<DataSource> dataSources = new ArrayList<DataSource>();

	/**
	 * Adds another DataSource to the list.
	 * 
	 * @param dataSource
	 *            the DataSource to add to the list
	 */
	public void addDataSource(DataSource dataSource) {
		dataSources.add(dataSource);
		Collections.sort(dataSources, new Comparator<DataSource>() {

			@Override
			public int compare(DataSource o1, DataSource o2) {
				return o1.getId().compareToIgnoreCase(o2.getId());
			}
		});
	}

	/**
	 * Clears this list.
	 */
	public void clearList() {
		dataSources.clear();
	}

	/**
	 * Returns DataSource at a given index.
	 * 
	 * @param index
	 *            list index
	 * @return DataSource at index
	 */
	public DataSource getDataSourceAt(int index) {
		if (index > -1) {
			DataSource dataSource = dataSources.get(index);
			return dataSource;
		}
		return null;
	}

	/**
	 * Returns a JLabel for the data source
	 */
	@Override
	public Object getElementAt(int index) {
		JLabel label = null;
		if (index > -1) {
			DataSource dataSource = dataSources.get(index);
			String name = dataSource.getFullname();
			if (name.trim().length() == 0)
				name = dataSource.getId();
			label = new JLabel(name);
			label.setName(dataSource.getId());
			label.setToolTipText("(" + dataSource.getId() + ") " + dataSource.getDescription());
		}
		return label;
	}

	@Override
	public int getSize() {
		return dataSources.size();
	}
}
