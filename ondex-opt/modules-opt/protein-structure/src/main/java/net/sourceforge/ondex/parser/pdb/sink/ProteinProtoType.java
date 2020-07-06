package net.sourceforge.ondex.parser.pdb.sink;

import java.util.ArrayList;

/**
 * 
 * @author peschr, taubertj
 */
public class ProteinProtoType {

	private String accessionNr;

	private ArrayList<DbLink> dblinks;

	private String ecNumber;

	private String pdbFileName;

	public ProteinProtoType() {
		this.dblinks = new ArrayList<DbLink>();
	}

	public void addDblinks(String dbName, String accession1, String accession2) {
		dblinks
				.add(new DbLink(dbName, new String[] { accession1, accession2 }));
	}

	public String getAccessionNr() {
		return accessionNr;
	}

	public ArrayList<DbLink> getDblinks() {
		return dblinks;
	}

	public String getEcNumber() {
		return ecNumber;
	}

	public String getPdbFileName() {
		return pdbFileName;
	}

	public void setAccessionNr(String accessionNr) {
		this.accessionNr = accessionNr;
	}

	public void setEcNumber(String ecNumber) {
		this.ecNumber = ecNumber;
	}

	public void setPdbFileName(String pdbFileName) {
		this.pdbFileName = pdbFileName;
	}

	public String toString() {
		String result = getEcNumber();
		if (result != null)
			result = result + " - " + getAccessionNr();
		else
			result = getAccessionNr();
		return result;

	}
}
