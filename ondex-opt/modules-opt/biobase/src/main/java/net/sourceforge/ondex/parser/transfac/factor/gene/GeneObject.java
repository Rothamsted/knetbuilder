package net.sourceforge.ondex.parser.transfac.factor.gene;

import net.sourceforge.ondex.parser.transfac.factor.FactorObject;



/**
 * Class representing a Gene object in TF.
 * 
 * @author taubertj
 *
 */
public class GeneObject extends FactorObject {
	
	// location on a chromosom
    private String chromosomalLocation = null;
    
    // regulation properties
	private String regulation = null;
    
	/**
	 * Constructor for a unique TF accession.
	 * 
	 * @param accession - String
	 */
    public GeneObject(String accession) {
    	super(accession);
    }
    
    /**
     * Sets the location on the chromosom tag.
     * 
     * @param location - String
     */
   	protected void setChromosomalLocation(String location) {
		this.chromosomalLocation = location;
	}
	
   	/**
   	 * Sets the regulation properties tag.
   	 * 
   	 * @param regulation - String
   	 */
	protected void setRegulation(String regulation) {
		this.regulation = regulation;
	}
	
	@Override
	public String getDescription() {
		StringBuilder description = null;
		if (super.getDescription() != null) {
			description = new StringBuilder(super.getDescription());
		}
		
		if (chromosomalLocation != null) {
			if (description == null) {
				description = new StringBuilder("Chromosomal Loaction: "
						+chromosomalLocation);
			} else {
				description.append(" Chromosomal Loaction: "
						+chromosomalLocation);
			}
		}
		if (regulation != null) {
			if (description == null) {
				description = new StringBuilder("Regulation: "
						+regulation);
			} else {
				description.append(" Regulation: "
						+regulation);
			}
		}
		if (description != null)
			return description.toString();
		else
			return "";
	}
	
}
