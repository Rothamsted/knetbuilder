package net.sourceforge.ondex.parser.pdb.sink;

public class DbLink{
	private String dbName;
	private String[] accession;
	public DbLink(String dbName, String[] accession ){
		this.dbName = dbName;
		this.accession = accession;
	}
	public String[] getAccession() {
		return accession;
	}

	public String getDbName() {
		return dbName;
	}
}
