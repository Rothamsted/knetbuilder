package net.sourceforge.ondex.parser.drastic;

import java.util.ArrayList;
import java.util.HashSet;

import net.sourceforge.ondex.config.ValidatorRegistry;

public class DraEntity {
	
	private final static String CV_PREF = "TAIR";
	
	String annotation = "";

	HashSet<String> geneNames = new HashSet<String>();

	HashSet<String> accNr = new HashSet<String>();
	
	String keyID = "";
	
	String prefTreatment = "";
	
	String prefName = "";

	boolean reg = false;
	
	HashSet<String> treatments = new HashSet<String>();
	
	String treatment_description ="";

	String compatibility = "";

	String specie = "";

	String taxID = "";

	int refID = 0;

	public void setGeneName(String s) {
		
		// gName stores the gene name like (SEB2)
		String gName = "";

		gName = s.trim();

		// if the gene name is valid set the gName
		if (Stoplist.check(gName)) {

			String[] acc = ColumnReader.readNameColumn(gName);
			
			for (int i = 0; i < acc.length; i++) {
				
				if (i == 0 && prefName.equals("")) {
					
					prefName = acc[i];
					
				} else {
					geneNames.add(acc[i].trim());
				}
				
				if (i == 0 && keyID.equals("")) {
					keyID = acc[0];
				}
			}
			
		}
	}
	
	public void setAnnotation(String s) {
		String anno = "";
		// sometimes the name ist in "", cut them off
		if (s.indexOf("\"") > -1) {
			
			anno = s.substring(s.indexOf("\"") + 1, s.lastIndexOf("\"")).trim();
		} else {
			anno = s.trim();
		}

		if (Stoplist.check(anno))
			annotation = anno;
	}
	
	
	public void setReg(String s) {
		/*
		 * parse column given by c_reg = regulation
		 * (up or down -> true or false)
		 */
		reg = false;
		
		if (s.toLowerCase().equals("up")) {
			reg = true;
		}
	}
	
	
	public void setSpecies(String s1, String s2) {
		/*
		 * parse species (and get taxonomy ID) consists of two column second
		 * colum may contain more than one word only take first word of second
		 * column
		 */
		String spe = "";

		// chop brackets
		if (s1.indexOf("\"") > -1) {
			spe = s1.substring(s1.indexOf("\"") + 1, s1.lastIndexOf("\""))
					.trim();
		} else {
			spe = s1.trim();
		}

		String tax = "";

		if (s2.indexOf("\"") > -1) {
			tax = s2.substring(s2.indexOf("\"") + 1, s2.lastIndexOf("\""))
					.trim();
		} else {
			tax = s2.trim();
		}

		
		String[] subName = tax.split(" ");

		// there are entries like "Brascia not known", so chop this off
		if (subName[0].equals("not")) {
			tax = spe;
		}
		// I don't know if its OK but up to now I consider only the
		// first two parts of the speciesname
		else {
			tax = spe + " " + subName[0];

		}
		// String taxID = (String) taxonomy.get(tax.toLowerCase());
		specie = tax;
		taxID = (String) ValidatorRegistry.validators.get("taxonomy").validate(tax.toLowerCase());
	}
	
	
	public void setAccessions(String[] s) {
	/*
	 * (2) parse column given by c_acc = Acc Numbers (may be more than one)
	 * if it the Acc Number is part of the preferred DataSource, write also! to variable key!
	 * 
	 */
		ArrayList<String> a = new ArrayList<String>();
	
		for (int i =0;i<s.length;i++) {	
			//gives back empty array if no valid accession could be found
			String[] acc = ColumnReader.readAccColumn(s[i]);
			
			for (int j =0;j<acc.length;j++) {
				a.add(acc[j]);
			}
		}
	
		for (int i=0; i<a.size();i++) {
			
			
			accNr.add(a.get(i));
			
			String tmp_cv = (String) ValidatorRegistry.validators.get("cvregex").validate(a.get(i));
			
			//If there is no ATG number so far,
			//use any other valid acc as key
			if(tmp_cv != null && keyID.equals("")) {
				keyID = a.get(i);
			}
			
			//If there a ATG number,
			//use this as key!
            if(tmp_cv!=null && tmp_cv.equals(CV_PREF)) {
            	keyID = a.get(i);
            }
		}
	}
	
	public void setTreatment(String s) {
		if (Stoplist.check(s.trim())) {
			
			String[] acc = ColumnReader.readNameColumn(s.trim());
			
			for (int i = 0; i < acc.length; i++) {
				
				if (i == 0 && prefTreatment.equals("")) {
					
					prefTreatment = acc[i];
				
				} else {
				
				treatments.add(acc[i].trim());
				}
			}
		}
	}
	
	public void setTreatmentDescription(String s) {
		if (Stoplist.check(s.trim()))
		treatment_description=s.trim();
	}
	
	public String getTreatmentDescription() {
		return treatment_description;
	}
	
	public void setCompatibility(String s) {
		compatibility=s.trim();
	}
	public void setRefID(int i) {
		refID=i;
	}
		
	/**
	 * Treatment Name Getter
	 * 
	 * @return treatment name as a String
	 */
	public HashSet<String> getTreatments() {
		
		return treatments;
	}
	
	public String getTreatment() {
		
		return prefTreatment;
	}

	public boolean getRegulation() {
		return reg;
	}

	public String getDescription() {
		return annotation;
	}

	public HashSet<String> getGeneNames() {
		
		return geneNames;
	}

	public String getTaxID() {
		
		return taxID;
	}

	public String getKey() {
		
		if (!keyID.equals("")) {
			return keyID;
		} else
			return null;
	}
	
	/*
	 * Key will be set automatically, if acc matches preferred DataSource
	 * (e.g. ATG) but can be set by hand
	 */
	public void setKey(String s) {
		keyID = s; 
	}

	public boolean hasKey() {
		if (!keyID.equals("")) 
		return true; 
		else return false; 
	}
	
	
	//returns the object's accession numbers
	public HashSet<String> getAccs() {
	
		return accNr;
	}
	
}
