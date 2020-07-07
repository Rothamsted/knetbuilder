package net.sourceforge.ondex.parser.bcsdb;

import net.sourceforge.ondex.core.DataSource;

/**
 * Class stores pair of an external reference and the DataSource for it.
 * 
 * @author taubertj
 *
 */
public class DBRef {

	public String reference;

	public DataSource dataSource;

	public DBRef(String reference, DataSource dataSource) {
		this.reference = reference;
		this.dataSource = dataSource;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DBRef) {
			DBRef dbref = (DBRef) obj;
			return dataSource.equals(dbref.dataSource) && reference.equals(dbref.reference);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return dataSource.hashCode() + reference.hashCode();
	}
}
