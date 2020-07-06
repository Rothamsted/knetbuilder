package net.sourceforge.ondex.parser.customtab;

import java.io.File;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.tools.subgraph.DefConst;
import net.sourceforge.ondex.tools.tab.importer.ConceptPrototype;
import net.sourceforge.ondex.tools.tab.importer.DelimitedReader;
import net.sourceforge.ondex.tools.tab.importer.PathParser;

public class Parser extends ONDEXParser
{

	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[] { new FileArgumentDefinition(
				FileArgumentDefinition.INPUT_FILE,
				"custom delimited file to import", true, true, false, false) };
	}

	public String getName() {
		return "tab";
	}

	public String getVersion() {
		return "v1.0";
	}

	@Override
	public String getId() {
		return "customtab";
	}

	public String[] requiresValidators() {
		return new String[0];
	}

	public void start() throws Exception {
		File file = new File((String) args
				.getUniqueValue(FileArgumentDefinition.INPUT_FILE));
		PathParser pp = new PathParser(graph, new DelimitedReader(file
				.getAbsolutePath(), "	", 27));
		ConceptPrototype c1 = pp.newConceptPrototype(DefConst.defAccession(1,
				"SGD"), DefConst.defCC("Protein"), DefConst.defName(2),
				DefConst.defAttribute(9, "Description"));
		ConceptPrototype c2 = pp.newConceptPrototype(DefConst.defAccession(4,
				"GO"), DefConst.defCC("Thing"), DefConst.defName(3));
		pp.newRelationPrototype(c1, c2, DefConst.defRT("is_a"));
		pp.parse();
	}

}
