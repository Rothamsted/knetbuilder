pp = new PathParser(
	getActiveGraph(), 
	new DelimitedFileReader( "/Users/brandizi/Documents/Work/RRes/ondex-code/modules/tab-parser-2/src/test/resources/ondex_tutorial_2016/gene_example.tsv", "\\t+", 1)
);
c1 = pp.newConceptPrototype(defAccession(0,"ENSEMBL",false), defCC("Gene"), defName(0), defDataSource("ENSEMBL"), defAttribute("9606", "TAXID", "TEXT", false), defAttribute(2, "Chromosome", "INTEGER", false), defAttribute(3, "BEGIN", "INTEGER", false), defAttribute(4, "END", "INTEGER", false));
c2 = pp.newConceptPrototype(defAccession(1,"UNIPROTKB",false), defCC("Protein"), defDataSource("ENSEMBL"));
pp.newRelationPrototype(c1, c2, defRT("enc"));

pp.parse();