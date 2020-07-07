var inpath = "/Users/brandizi/Documents/Work/RRes/ondex_git/modules/tab-parser-2/src/test/resources/multi_attr_test/protDomain.tsv";
java.lang.System.out.println ( "Going to parse:" + inpath );
pp = new PathParser( getActiveGraph(), new DelimitedFileReader ( inpath, "\t" ) );
prot = pp.newConceptPrototype ( defAccession ( 1, "ENSEMBL" ), defCC ( "Protein" ), defDataSource ( "ENSEMBL" ) );
protDomain = pp.newConceptPrototype ( 
	defName ( 2, "false" ), 
	defAccession ( 3, "IPRO" ),
	defName ( 4, "true" ),
	defAttribute ( 5, "Description", "TEXT", "false" ),
	defCC ( "ProtDomain" ), defDataSource ( "ENSEMBL" ) 
);
pp.newRelationPrototype ( prot, protDomain, defRT ( "has_domain" ) );
rval = pp.parse ();
java.lang.System.out.println ( "DONE! Result from parse(): " + rval );
