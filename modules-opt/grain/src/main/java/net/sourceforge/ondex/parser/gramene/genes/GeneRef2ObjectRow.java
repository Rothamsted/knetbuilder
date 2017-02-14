package net.sourceforge.ondex.parser.gramene.genes;

/**
 * INDEX for expected data table
 * 0 `dbxref_to_object_id` int(11) NOT NULL default '0' PRIMARY KEY,
 * 1 `table_name` varchar(50) NOT NULL default '',
 * 2 `record_id` int(11) NOT NULL default '0',
 * 3 `dbxref_id` int(11) NOT NULL default '0',
 * 4 `dbxref_value` varchar(50) NOT NULL default '',
 * 
 * Object representing information on a Gene xref
 * @author hoekmanb
 *
 */
public class GeneRef2ObjectRow {

	private int recordId;

	private String tableName;

	private int dbXRefId;

	private String dbXRefValue;


	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}

	public void setdbXRefId(int dbXRefId) {
		this.dbXRefId = dbXRefId;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setDBXRefValue(String dbXRefValue) {
		this.dbXRefValue = dbXRefValue;
	}

	public int getRecordId() {
		return recordId;
	}

	public int getdbXRefId() {
		return dbXRefId;
	}

	public String getTableName() {
		return tableName;
	}

	public String getDBXRefValue() {
		return dbXRefValue;
	}
}
