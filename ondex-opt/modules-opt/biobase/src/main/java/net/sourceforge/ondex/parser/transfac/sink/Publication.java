package net.sourceforge.ondex.parser.transfac.sink;

/**
 * Publication object for TF.
 * 
 * @author taubertj
 *
 */
public class Publication {

	// pubmed id is accession
	private final String pmid;
	
	// title for this publication
	private String title;
	
	// source of this publication
	private String source;
	
	// authors of this publication
	private String authors;
	
	/**
	 * Constructor for unique pubmed id.
	 * 
	 * @param pmid
	 */
	public Publication(String pmid) {
		this.pmid = pmid;
	}
	
	/**
	 * Returns pubmed id.
	 * 
	 * @return String
	 */
	public String getPmid() {
		return pmid;
	}
	
	/**
	 * Returns authors line.
	 * 
	 * @return String
	 */
	public String getAuthors() {
		return authors;
	}
	
	/**
	 * Sets authors line.
	 * 
	 * @param authors - String
	 */
	public void setAuthors(String authors) {
		this.authors = authors;
	}
	
	/**
	 * Returns source line.
	 * 
	 * @return String
	 */
	public String getSource() {
		return source;
	}
	
	/**
	 * Sets source line.
	 * 
	 * @param source - String
	 */
	public void setSource(String source) {
		this.source = source;
	}
	
	/**
	 * Returns title line.
	 * 
	 * @return String
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Sets title line.
	 * 
	 * @param title - String
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o instanceof Publication) {
			return this.pmid.equals(((Publication)o).pmid);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
    	return this.pmid.hashCode();
	}
}
