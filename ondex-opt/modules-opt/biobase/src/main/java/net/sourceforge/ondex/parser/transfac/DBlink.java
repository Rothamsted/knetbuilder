package net.sourceforge.ondex.parser.transfac;



/**
 * Represents a database link with accession and DataSource.
 * 
 * @author taubertj
 *
 */
public class DBlink {
	
	// contained accesion
	private String acc;
	
	// contained DataSource
	private String cv;
	
	// precalculated hash code
	private int hash;
	
	public DBlink(String acc, String cv) {
		this.acc = acc;
		this.cv = cv;
		this.hash = acc.concat(cv).hashCode();
	}
	
	/**
	 * Returns stored database accession.
	 * 
	 * @return String
	 */
	public String getAcc() {
		return this.acc;
	}
	
	/**
	 * Returns stored database DataSource.
	 * 
	 * @return String
	 */
	public String getCv() {
		return this.cv;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o instanceof DBlink) {
			DBlink link = (DBlink) o;
			return this.acc.equals(link.acc) 
				&& this.cv.equals(link.cv);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hash;
	}
}
