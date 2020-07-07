package net.sourceforge.ondex.parser.ecocyc.parse;

import net.sourceforge.ondex.parser.ecocyc.objects.AbstractNode;
import net.sourceforge.ondex.parser.ecocyc.objects.Gene;
import net.sourceforge.ondex.parser.ecocyc.objects.Protein;
import net.sourceforge.ondex.parser.ecocyc.objects.SinkFactory;
/**
 * Parser for the gene.* files(s)
 * @author peschr
 */
public class GeneParser extends AbstractParser {
	private Gene gene;


	public void addSwissProtId(String name) {
		gene.setSwissProtId(name);
	}

	public void addSynonym(String synonym) {
		gene.addSynonym(synonym);
	}

	public void addProduct(String product) throws Exception {
		Protein protein = (Protein) SinkFactory.getInstance().create(
				Protein.class, product);
		//if (protein.getEncodedBy() != null && protein.getEncodedBy() != gene)
		//	log.error("inconsistence links");
		gene.setProduct(protein);
	}

	public void distribute(String key, String value) throws Exception {
		//super.distributeCore(key, value);
		if (key.equals("SWISS-PROT-ID"))
			this.addSwissProtId(value);
		else if (key.equals("PRODUCT")) {
			this.addProduct(value);
		}
	}

	public AbstractNode getNode() {
		return gene;
	}
	public void start(String uniqueId) throws Exception {
		gene = (Gene) SinkFactory.getInstance().create(Gene.class, uniqueId);
	}
}
