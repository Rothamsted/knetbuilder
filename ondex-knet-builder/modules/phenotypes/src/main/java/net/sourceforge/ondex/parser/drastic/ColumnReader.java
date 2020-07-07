package net.sourceforge.ondex.parser.drastic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import net.sourceforge.ondex.config.ValidatorRegistry;



/**
 * Reads one column of a line from the spreadsheet.
 * Analyses the column.
 * Extract all values of one column
 * (e.g. AV824251; AV785462) and giv them back in an array.
 * 
 * @author winnenbr
 *
 */
public class ColumnReader {
	
	static String[] readAccColumn(String col) {
		return readColumn(col,true);
	}
	static String[] readNameColumn(String col) {
		return readColumn(col,false);
	}
	
	/**
	 * reads a column and writes the values to an array
	 * (as there can be more than one value per column and line)
	 * 
	 * @param col the column as retrieved from the input file
	 * @param check: should accession numbers be checked with regular expresssion
	 * @return
	 */
	static private String[] readColumn(String col, boolean check) {
	
		String acc = "";
		
		/*
		 * First check if value is valid
		 * (and not any derivate of "unknown",
		 * like "not known", "no tknow".. ;) )
		 */
		if (Stoplist.check(col.trim())) {
			
			acc = col.trim();
		
			/*
			 * Second check if there are more than one values in column
			 */
			ArrayList<String> accA = new ArrayList<String>();
			accA.add(acc);
			String s = "";
			
			/*
			 * Find Separators: / ;
			 */
			//as long as there are separators in the column
			while (!(s=Stoplist.findSeparator(accA)).equals("")) {
				ArrayList<String> newAccA = new ArrayList<String>();
				Iterator<String> it = accA.iterator();
				
				while (it.hasNext()) {
					String toCheck = it.next();
					String[] tmp = toCheck.split(s);
					for (int i = 0; i<tmp.length;i++) {
						newAccA.add(tmp[i].trim());
					}
				}
				
				accA=newAccA;
			}
			
			/*
			 * Find Brackets: () ;
			 */
			accA=Stoplist.handleBrackets(accA);

			Vector<String> tmpVec = new Vector<String>();

			// for every entry in column
			Iterator<String> it = accA.iterator();	
			while (it.hasNext()) {
				String toCheck = it.next();

				// only if entry is not an empty String
				if (!toCheck.equals("")) {
					//only if accession number (check==true)
					if (check && toCheck.indexOf("-") > -1) {
						toCheck = toCheck.substring(0, toCheck.indexOf("-"));
					}

					//if check flag set:
					//add entry only to Vector
					//if there is an cv the id belongs to
					if (!check || (
							check && 
							(ValidatorRegistry.validators.get("cvregex").validate(toCheck)) != null)
									) {
						tmpVec.add(toCheck);
					}
				}
			}

			/*
			 * now copy the valid values (with a proper cv)
			 * from the Vector to output array
			 */
			String[] retArr = null;
			
			
			retArr = new String[tmpVec.size()];
			for (int i = 0; i < tmpVec.size(); i++) {
				retArr[i] = tmpVec.elementAt(i).trim();
			}
			
			//give back an array with all proper values for one column and line
			return retArr;
			
			
		} else {
			return new String[0];
		}
		
	}

}