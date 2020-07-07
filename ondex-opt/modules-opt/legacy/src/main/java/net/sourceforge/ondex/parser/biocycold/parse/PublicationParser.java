package net.sourceforge.ondex.parser.biocycold.parse;

import net.sourceforge.ondex.parser.biocycold.objects.AbstractNode;
import net.sourceforge.ondex.parser.biocycold.objects.Publication;
import net.sourceforge.ondex.parser.biocycold.objects.SinkFactory;
/**
 * Parser for the publication.* files(s)
 * @author peschr
 */
public class PublicationParser extends AbstractParser {
	private Publication pub;
	@Override
	public void distribute(String key, String value) throws Exception {
		if(key.equals("PUBMED-ID"))
			this.addPubMedId(value);
		if(key.equals("TITLE"))
			pub.setTitle(value);
	}
	public void addPubMedId(String value){
		int pubMedId;
		try{
			pubMedId = Integer.valueOf(value);
			pub.setPubMedId(pubMedId);
		}catch(Exception e){
			e.printStackTrace();
		//	log.error("strange PubMedId (" + value + ")");
		}
	}
	public AbstractNode getNode() {
		return pub;
	}

	public void start(String uniqueId) throws Exception {
		pub = (Publication) SinkFactory.getInstance().create(Publication.class,uniqueId.toUpperCase());
	}
}
