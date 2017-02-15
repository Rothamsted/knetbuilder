package net.sourceforge.ondex.parser.uniprot.sink;

import java.util.HashSet;
import java.util.Set;

/**
 * @author peschr
 */
public class DbLink {

	private String dbName;
	private String accession;
	private Set<String> evidences = null;

	public String getAccession() {
		return accession;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName.intern();
	}

	public String toString() {
		return dbName + ' ' + accession;
	}

	public void addEvidence(String evidence) {
		if (evidences == null) {
			evidences = new HashSet<String>(3);
		}
		evidences.add(evidence);
	}

	public Set<String> getEvidence() {
		if (evidences == null) {
			evidences = new HashSet<String>(1);
		}
		return evidences;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return dbName.hashCode() + 13 * accession.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DbLink)
			return ((DbLink) obj).dbName.equals(dbName)
					&& ((DbLink) obj).accession.equals(accession);
		else
			return false;
	}

}
