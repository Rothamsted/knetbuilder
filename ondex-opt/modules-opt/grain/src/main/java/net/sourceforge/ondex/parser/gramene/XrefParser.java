package net.sourceforge.ondex.parser.gramene;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *  INDEX for expected data table
 * 
 * 0 `dbxref_id` int(11) NOT NULL default '0' PRIMARY KEY,
 * 1 `xref_key` varchar(255) NOT NULL default '',
 * 2 `xref_keytype` varchar(32) default NULL,
 * 3 `xref_dbname` varchar(64) NOT NULL default '',
 * 4 `xref_desc` varchar(255) default NULL
 * 
 * Constructs an index on the xref table with external references
 * 
 * @author hindlem
 * @see net.sourceforge.ondex.parser.gramene.Xref.AccessionReference
 */
public class XrefParser {

	private Map<Integer, AccessionReference> xrefIndex = new HashMap<Integer, AccessionReference>();

	/**
	 * Constructs an index on the xref table with external references using the specified table (see doc)
	 * @param xrefFile file containing table in tab form
	 */
	public XrefParser(String xrefFile) {
		try {
			BufferedReader input = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(xrefFile),"UTF8"));
			Pattern tabPattern = Pattern.compile("\t");
			
			while (input.ready()) {
				String inputLine = input.readLine();

				String[] columns = tabPattern.split(inputLine);
				if (columns.length < 4) { //can be 5 with a description but this is optional
					continue;
				}
				
				AccessionReference ar = new AccessionReference(columns[3].trim().toUpperCase(), columns[1].trim(), columns[2].trim().toUpperCase());	
				if (columns.length >= 5) {
					String desc = columns[4].trim();
					if (desc.length() > 0 && !desc.equalsIgnoreCase("Not available")) {
						ar.setDescription(desc);
					}
				}
				
				xrefIndex.put(Integer.parseInt(columns[0].trim()), ar);
			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads accession reference from constructed index
	 * @param id xref id
	 * @return accession reference
	 * @see net.sourceforge.ondex.parser.gramene.Xref.AccessionReference
	 */
	public AccessionReference getAccessionReference(int id) {
		return xrefIndex.get(id);
	}

	/**
	 * Storage object for xrefs
	 * @author hindlem
	 *
	 */
	public class AccessionReference {

		private String dbType;
		private String accession;
		private String description;
		private String type;
		
		/**
		 * 
		 * @param dbType the name of the database
		 * @param accession the accession or in some cases id
		 * @param type "acc" or "id"
		 */
		public AccessionReference(String dbType, String accession, String type) {
			this.dbType = dbType.intern();
			this.type = type.intern();
			this.accession = accession;
		}

		/**
		 * 
		 * @return a description of the object referenced (may not exist)
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * 
		 * @param description a description of the object referenced (may not exist)
		 */
		public void setDescription(String description) {
			this.description = description;
		}

		/**
		 * 
		 * @return the source of the accession or id
		 */
		public String getDbType() {
			return dbType;
		}

		/**
		 *  
		 * @return the accession the source of the accession or id
		 */
		public String getAccession() {
			return accession;
		}

		/**
		 * 
		 * @return usualy either the accession or id indicated by "id" or "acc"
		 */
		public String getType() {
			return type;
		}

	}
}
