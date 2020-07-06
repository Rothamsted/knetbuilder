package net.sourceforge.ondex.parser.aracyc.objects;

/**
 * Represents a database link. Don't create Dblink objects with the SinkFactory!
 * @author peschr
 * 
 */
public class Dblink extends AbstractNode{
	private DBName dbName;
	private String accession;
	
	public String getAccession() {
		return accession;
	}
	public void setAccession(String accession) {
		this.accession = accession;
	}
	public DBName getDbName() {
		return dbName;
	}
	public void setDbName(DBName dbName) {
		this.dbName = dbName;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Dblink) {
			Dblink l = (Dblink) obj;
			return l.dbName.equals(dbName) && l.accession.equals(accession);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return dbName.hashCode();
	}
}
