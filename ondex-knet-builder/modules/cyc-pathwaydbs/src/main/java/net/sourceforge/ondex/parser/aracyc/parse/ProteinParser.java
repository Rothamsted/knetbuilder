package net.sourceforge.ondex.parser.aracyc.parse;

import net.sourceforge.ondex.parser.aracyc.objects.AbstractNode;
import net.sourceforge.ondex.parser.aracyc.objects.Enzyme;
import net.sourceforge.ondex.parser.aracyc.objects.Protein;
import net.sourceforge.ondex.parser.aracyc.objects.SinkFactory;
/**
 * Parser for the protein.* files(s)
 * @author peschr
 */
public class ProteinParser extends AbstractParser{
	private Protein protein;
	public void distribute(String key, String value) throws Exception {	
		if(key.equals("CATALYZES")){
			this.addCatalyzes(value);
		}
		else if(key.equals("COMPONENT-OF") || key.equals("COMPONENTS"))
			this.addComponentOf(value);
		else if(key.equals("TYPES") && value.equals("Protein-Complexes")){
			protein.setComplex(true);
		}
	}
	public void addComponentOf(String value) throws Exception{
		Protein component = (Protein) SinkFactory.getInstance().create(Protein.class, value);
		protein.addComponent(component);
	}
	public void addCatalyzes(String value) throws Exception{
		try{
			Enzyme enzyme = (Enzyme) SinkFactory.getInstance().create(Enzyme.class, value);
			if ( enzyme.addIs_a(protein) == true){
				protein.addIsMemberOf(enzyme);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public AbstractNode getNode() {
		return protein;
	}

	public void start(String uniqueId) throws Exception {
		protein = (Protein) SinkFactory.getInstance().create(Protein.class, uniqueId);
	}
}
