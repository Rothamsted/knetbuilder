package net.sourceforge.ondex.parser.uniprot.sink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.parser.uniprot.MetaData;

/**
 * 
 * @author peschr
 * 
 */
public class Protein {

	private boolean manuallyCurated = true;
	private static Protein instance;

	public static Protein getInstance(boolean newInstance,
			boolean manuallyCurated) {
		if (instance == null || newInstance == true) {
			instance = new Protein();
			instance.setManuallyCurated(manuallyCurated);
		}
		return instance;
	}

	public static Protein getInstance() {
		return getInstance(false, false);
	}

	private Map<String, List<String>> accessions = new HashMap<String, List<String>>();
	private Set<String> names = new HashSet<String>();
	private Set<String> preferedNames = new HashSet<String>();
	private String taxId;
	private List<Publication> publication = new ArrayList<Publication>();
	private Set<DbLink> dbReferences = new HashSet<DbLink>();
	private String sequence;
	private String dataset;
	private String entryStats;
	private String disruptionPhenotype;

	public String getDisruptionPhenotype() {
		return disruptionPhenotype;
	}

	public void setDisruptionPhenotype(String disruptionPhenotype) {
		this.disruptionPhenotype = disruptionPhenotype;
	}

	public String getEntryStats() {
		return entryStats;
	}

	public void setEntryStats(String entryStats) {
		this.entryStats = entryStats;
	}

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	public void addDbReference(DbLink dblink) {
		dbReferences.add(dblink);
	}

	public void addPublication(Publication pub) {
		publication.add(pub);
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence.replaceAll("\\s*", "");
	}

	public String getTaxId() {
		return taxId;
	}

	public void setTaxId(String taxId) {
		this.taxId = taxId.intern();
	}

	public void addAccession(String database, String accession) {
		if (!this.accessions.containsKey(database))
			this.accessions.put(database, new ArrayList<String>(1));
		this.accessions.get(database).add(accession);
	}

	public Map<String, List<String>> getAccessions() {
		return this.accessions;
	}

	public Set<String> getNames() {
		return names;
	}

	public Set<String> getPreferedNames() {
		return preferedNames;
	}

	public void addName(String name) {
		this.names.add(name);
	}

	public void addPreferedName(String name) {
		this.preferedNames.add(name);
	}

	public Set<DbLink> getDbReferences() {
		return dbReferences;
	}

	public List<Publication> getPublication() {
		return publication;
	}

	public String toString() {
		return accessions.toString();
	}

	public boolean isManuallyCurated() {
		return manuallyCurated;
	}

	public void setManuallyCurated(boolean manuallyCurated) {
		this.manuallyCurated = manuallyCurated;
	}

	public String getPID() {
		if (accessions.containsKey(MetaData.CV_UniProt)) {
			return accessions.get(MetaData.CV_UniProt).get(0);
		}
		return "";
	}
}
