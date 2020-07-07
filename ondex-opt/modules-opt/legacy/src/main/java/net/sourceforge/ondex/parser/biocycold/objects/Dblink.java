package net.sourceforge.ondex.parser.biocycold.objects;

/**
 * Represents a database link. Don't create Dblink objects with the SinkFactory!
 * @author peschr
 * 
 */
public class Dblink extends AbstractNode{
	private DBName dbName;
	private String Accession;
	
	public String getAccession() {
		return Accession;
	}
	public void setAccession(String accession) {
		Accession = accession;
	}
	public DBName getDbName() {
		return dbName;
	}
	public void setDbName(DBName dbName) {
		this.dbName = dbName;
	}
}
