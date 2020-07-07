package net.sourceforge.ondex.parser.aracyc.parse;

import net.sourceforge.ondex.parser.aracyc.objects.AbstractNode;
import net.sourceforge.ondex.parser.aracyc.objects.Compound;
import net.sourceforge.ondex.parser.aracyc.objects.Enzyme;
import net.sourceforge.ondex.parser.aracyc.objects.Reaction;
import net.sourceforge.ondex.parser.aracyc.objects.SinkFactory;
/**
 * Parser for the enzyme.* files(s)
 * @author peschr
 */
public class EnzymeParser extends AbstractParser {
	private Enzyme enzyme;
	@Override
	public void distribute(String key, String value) throws Exception {
		if(key.equals("REACTION")){
			this.addReaction(value);
		}
		else if(//key.equals("INHIBITORS") ||
				key.equals("INHIBITORS-ALLOSTERIC") ||
				key.equals("INHIBITORS-COMPETITIVE")		||
				key.equals("INHIBITORS-IRREVERSIBLE") ||
				key.equals("INHIBITORS-NONCOMPETITIVE")		||
				key.equals("INHIBITORS-OTHER") ||
				key.equals("INHIBITORS-UNCOMPETITIVE")		||
				key.equals("INHIBITORS-UNKMECH")   
				){
			this.addInhibitor(value);
		}
		else if(//key.equals("ACTIVATORS") ||
				key.equals("ACTIVATORS-ALLOSTERIC") ||
				key.equals("ACTIVATORS-NONALLOSTERIC") ||
				key.equals("ACTIVATORS-UNKMECH")  	
				){
			this.addActivator(value);
		}else if(key.equals("COFACTORS")) {
			this.addCofactor(value);
		}
		else if( key.equals("REACTION") ){
			this.addReaction(value);
		}
	}
	private void addCofactor(String value) {
		AbstractNode node = null;
		try{
			node = SinkFactory.getInstance().findByUniqueId( value);
		}catch(Exception e){
			try{
				node = SinkFactory.getInstance().create(Compound.class, value);
			}catch(Exception a){
				//log.fatal("couldn't find or create AbstractNode with ID;" + value);
			}
		}
		enzyme.addCofactor(node);
	}
	public void addInhibitor(String value){
		AbstractNode node = null;
		try{
			node = SinkFactory.getInstance().findByUniqueId( value);
		}catch(Exception e){
			try{
				node = SinkFactory.getInstance().create(Compound.class, value);
			}catch(Exception a){
				//log.fatal("couldn't find or create AbstractNode with ID;" + value);
			}
		}
		enzyme.addInhibitor(node);
	}
	public void addActivator(String value){
		AbstractNode node = null;
		try{
			node = SinkFactory.getInstance().findByUniqueId( value);
		}catch(Exception e){
			try{
				node = SinkFactory.getInstance().create(Compound.class, value);
			}catch(Exception a){
				//log.fatal("couldn't find or create AbstractNode with ID;" + value);
			}
		}
		enzyme.addActivator(node);
	}
	public void addReaction(String value) throws Exception{
		 Reaction reaction = (Reaction) SinkFactory.getInstance().create(Reaction.class, value);
		 enzyme.setCatBy(reaction);
		 reaction.addGetsCatBy(enzyme);
	}
	public AbstractNode getNode() {
		return enzyme;
	}

	public void start(String uniqueId) throws Exception {
		this.enzyme = (Enzyme) SinkFactory.getInstance().create(Enzyme.class, uniqueId);
	}

}
