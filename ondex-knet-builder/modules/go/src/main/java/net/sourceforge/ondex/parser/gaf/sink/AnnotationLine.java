package net.sourceforge.ondex.parser.gaf.sink;

import java.util.ArrayList;
import java.util.HashSet;


/**
 * 
 * @author hoekmanb
 * 
 */
public class AnnotationLine {
	// Documentation:
	// http://www.geneontology.org/GO.annotation.shtml#file

	// Annotation File Fields
	//
	// The flat file format comprises 15 tab-delimited fields; red text denotes
	// required fields. See also the annotation fields page, with a table
	// showing the columns in order and example annotations.
	// Fields in the annotation file

	// Column Content Required? Example
	//	
	// 1 DB required SGD
	// 2 DB_Object_ID required S000000296
	// 3 DB_Object_Symbol required PHO3
	// 4 Qualifier optional NOT
	// 5 GO ID required GO:0003993
	// 6 DB:Reference (|DB:Reference) required PMID:2676709
	// 7 Evidence code required IMP
	// 8 With (or) From optional GO:0000346
	// 9 Aspect required F
	// 10 DB_Object_Name optional acid phosphatase
	// 11 DB_Object_Synonym (|Synonym) optional YBR092C
	// 12 DB_Object_Type required gene
	// 13 taxon(|taxon) required taxon:4932
	// 14 Date required 20010118
	// 15 Assigned_by required SGD

	private String DB;
	private String DB_Object_ID;
	private String DB_Object_Symbol;
	private String qualifier;
	private String GO_ID;
	private String evidenceCode;
	private String WF;
	private String aspect;
	private String DB_Object_Name;
	private String DB_Object_Type;
	private String date;
	private String assignedBy;

	HashSet<String> DBReferences;
	HashSet<String> DBObjectSynonyms;
	ArrayList<String> taxons;

	// To decide type:
	//	
	// DB_Reference
	// DB_Object_Synonym

	public AnnotationLine() {
		DBReferences = new HashSet<String>();
		DBObjectSynonyms = new HashSet<String>();
		taxons = new ArrayList<String>();
	}

	public void addDBReference(String DBRef) {
		DBReferences.add(DBRef.trim());
	}

	public void addDBObjectSynonym(String DBOS) {
		DBObjectSynonyms.add(DBOS.trim());
	}

	public void addTaxon(String taxon) {
		taxons.add(taxon.trim());
	}

	public void setDatabase(String DB) {
		this.DB = DB.trim();
	}

	public void setDBObjectID(String DBO) {
		this.DB_Object_ID = DBO.trim();
	}

	public void setDBObjectSymbol(String DBOS) {
		this.DB_Object_Symbol = DBOS.trim();
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier.trim();
	}

	public void setGOID(String GOID) {
		this.GO_ID = GOID.trim();
	}

	public void setEvidenceCode(String EviCode) {
		this.evidenceCode = EviCode.trim();
	}

	public void setWithFrom(String withFrom) {
		this.WF = withFrom.trim();
	}

	public void setAspect(String aspect) {
		this.aspect = aspect.trim();
	}

	public void setDBObjectName(String DBON) {
		this.DB_Object_Name = DBON.trim();
	}

	public void setDBObjectType(String DBOT) {
		this.DB_Object_Type = DBOT.trim();
	}

	public void setDate(String date) {
		this.date = date.trim();
	}

	public void setAssignedBy(String assignedBy) {
		this.assignedBy = assignedBy.trim();
	}

	/**
	 * Database in which did the annotation
	 */
	public String getDatabase() {
		return DB;
	}

	/**
	 * The ObjectName in the database which contributed the GOA file
	 */
	public String getDBObjectID() {
		return DB_Object_ID;
	}

	/**
	 * A "Accession"
	 */
	public String getDBObjectSymbol() {
		return DB_Object_Symbol;
	}

	/**
	 * returns the qualifier for this annotation line. be carefull as this
	 * qualifier can be "NOT"
	 * 
	 * @return String
	 */
	public String getQualifier() {
		return qualifier;
	}

	/**
	 * The GO ID
	 */
	public String getGOID() {
		return GO_ID;
	}

	/**
	 * The relation EvidenceCode;
	 */
	public String getEvidenceCode() {
		return evidenceCode;
	}

	/**
	 * 
	 * 
	 * @return String - or null when not present.
	 */
	public String getWithFrom() {
		return WF;
	}

	/**
	 * Which part of the GO three the GOID is part of
	 */
	public String getAspect() {
		return aspect;
	}

	/**
	 * 
	 */
	public String getDBObjectName() {
		return DB_Object_Name;
	}

	/**
	 * The type: gene, transcript, protein, protein_structure, complex
	 */
	public String getDBObjectType() {
		return DB_Object_Type;
	}

	/**
	 * Date when the annotation was made
	 */
	public String getDate() {
		return date;
	}

	/**
	 * The database which made the assignment
	 */
	public String getAssignedBy() {
		return assignedBy;
	}

	/**
	 * List of references for information about the relation between GO term and
	 * gene/protein/transcript etc
	 * 
	 * @return ObjectOpenHashSet<String>
	 */
	public HashSet<String> getDBReferences() {
		return DBReferences;
	}

	public HashSet<String> getDBObjectSynonyms() {
		return DBObjectSynonyms;
	}

	public ArrayList<String> getTaxons() {
		return taxons;
	}
}
