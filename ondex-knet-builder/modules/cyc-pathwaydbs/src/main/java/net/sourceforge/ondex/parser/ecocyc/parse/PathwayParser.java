package net.sourceforge.ondex.parser.ecocyc.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.ondex.parser.ecocyc.objects.AbstractNode;
import net.sourceforge.ondex.parser.ecocyc.objects.Pathway;
import net.sourceforge.ondex.parser.ecocyc.objects.SinkFactory;
/**
 * Parser for the pathway.* files(s)
 * @author peschr
 */
public class PathwayParser extends AbstractParser{
	private Pathway pathway;
	private static Pattern pathWayLink = Pattern.compile("([\\w-]*PWY[-\\w]*)");
	
	@Override
	public void distribute(String key, String value) throws Exception {
		if (key.equals("SUB-PATHWAYS")){
			this.addSubPathway(value);
		}
		else if(key.equals("PATHWAY-LINKS")){
			this.addPathWayLink(value);
		}
		else if(key.equals("SUPER-PATHWAYS")){
			this.addSuperPathWay(value);
		}
		else if(key.equals("TYPES") && value.equals("Super-Pathways") ){
			pathway.setSuperPathWay(true);
		}
		//else if(key.equals("REACTION-LIST")){ //need to be checked (the semantic)!
		//	this.addPathWayLink(value);
		//}
	}
	public void addSuperPathWay(String value) throws Exception{
		Pathway way = (Pathway) SinkFactory.getInstance().create(Pathway.class, value);
		pathway.setSuperPathWay(way);
	}
	public void addPathWayLink(String value) throws Exception{
		Matcher m = pathWayLink.matcher(value);

		while(m.find()){
			String  pathWayId = m.group(1);
			Pathway way = (Pathway) SinkFactory.getInstance().create(Pathway.class, pathWayId);		
			pathway.addPathwayLink(way);		
		}
	}
	public void addSubPathway(String value) throws Exception{
		Pathway way = (Pathway) SinkFactory.getInstance().create(Pathway.class, value);
		pathway.addSubPathWay(way);
	}
	public AbstractNode getNode() {
		return this.pathway;
	}

	public void start(String uniqueId) throws Exception {
		pathway = (Pathway) SinkFactory.getInstance().create(Pathway.class,uniqueId);	
	}
}
