package net.sourceforge.ondex.core.base;

import net.sourceforge.ondex.core.DataSource;

/**
 * Convenience implementation of a DataSource.
 * 
 * @author taubertj
 */
public class DataSourceImpl extends AbstractMetaData implements DataSource {

	/**
	 * Constructor which sets the id and the description of this DataSource.
	 * 
	 * @param sid
	 *            unique id
	 * @param id
	 *            id of this DataSource
	 * @param fullname
	 *            fullname of this DataSource
	 * @param description
	 *            description of this DataSource
	 */
	protected DataSourceImpl(long sid, String id, String fullname,
			String description) {
		super(sid, id, fullname, description);
	}
}
