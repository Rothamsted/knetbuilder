package net.sourceforge.ondex.parser.wordnet;

/**
 * Accession Object
 * 
 * stores all details for a Accession (number) which has been
 * parsed out by an ONDEX parser
 * 
 * @author rwinnenb
 *
 */
public class Accession {


	//stores DataSource name as a String
	private String cv;

	//stores the acyual accession (number) as String
	private String acc;
	
	//flag for setting accession number to ambiguous
	private boolean ambiguous = false;

	/**
	 * Create Accession Object
	 * 
	 * @param accession = accession number as String (e.g. "1.2.3.4")
	 * @param cv = DataSource name as String (e.g. "EC")
	 * @param amb = 0 for not 1 for ambiguous
	 */
	public Accession(String accession, String cv, int amb) {

		this(accession,cv);
		if (amb==0)
			this.ambiguous = false;
		else
			this.ambiguous = true;
	}
	
	/**
	 * Create Accession Object
	 * 
	 * ambigiuous will be set automatically to false
	 * 
	 * @param accession = accession number as String (e.g. "1.2.3.4")
	 * @param cv = DataSource name as String (e.g. "EC")
	 */
	public Accession(String accession, String cv) {
		
		this.acc= accession;
		this.cv = cv;		
	}

	/**
	 * Retrieve accession number
	 * 
	 * @return accession number for this Accession
	 */
	public String getAccession() {
		
		return acc;
	}
	
	/**
	 * Retrieve ambiguous status of Accession
	 * 
	 * @return flag true/false
	 */
	public boolean getAmbiguous() {
		
		return ambiguous;
	}
	
	/**
	 * Retrieve DataSource of Accession
	 * 
	 * @return the DataSource as String
	 */
	public String getCV() {
		
		return this.cv;
	}
	
	/**
	 * Set DataSource of Accession
	 * 
	 * @param cv = the DataSource name as String
	 */
	public void setCV(String cv) {

		this.cv=cv;
	}
}
