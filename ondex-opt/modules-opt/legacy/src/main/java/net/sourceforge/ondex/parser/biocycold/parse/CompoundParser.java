package net.sourceforge.ondex.parser.biocycold.parse;

import net.sourceforge.ondex.parser.biocycold.objects.AbstractNode;
import net.sourceforge.ondex.parser.biocycold.objects.Compound;
import net.sourceforge.ondex.parser.biocycold.objects.SinkFactory;
/**
 * Parser for the compounds.* files(s)
 * @author peschr
 */
public class CompoundParser extends AbstractParser {
	private AbstractNode node;
	@Override
	public void distribute(String key, String value) throws Exception {
		
		if(key.equals("COFACTORS-OF")){
			this.addCofactorOf(value);
		}
		else if(key.equals("SMILES") && node instanceof Compound){
			((Compound)node).setSmiles(value);
		}
		else if(key.equals("SYSTEMATIC-NAME") && node instanceof Compound)
			((Compound)node).setSystematicName(value);
	}
	public void addCofactorOf(String value){
		try{
			AbstractNode node = SinkFactory.getInstance().findByUniqueId(value);
			if (node instanceof Compound) 
				((Compound)this.node).addCofactorOf(node);
		}catch(Exception e){
			//log.error("couldn't find cofactor:" + value);
		}
	}
	public AbstractNode getNode() {
		return node;
	}

	public void start(String uniqueId) throws Exception {
		node = SinkFactory.getInstance().create(Compound.class, uniqueId);
	}
}
