/**
 * This is one of the scripts that the Console is able to run.
 */
var inpath = "/Users/brandizi/Documents/Work/RRes/ondex_tests/scripting_test_plugin/src/test/resources/ppi_example.tsv";
java.lang.System.out.println ( "Going to parse:" + inpath );
pp = new PathParser( getActiveGraph(), new DelimitedFileReader ( inpath, "\t" ) );
ccProtL = pp.newConceptPrototype ( defAccession ( 0, "UNIPROTKB" ), defCC ( "Protein" ) );
ccProtR = pp.newConceptPrototype ( defAccession ( 1, "UNIPROTKB" ), defCC ( "Protein" ) );
pp.newRelationPrototype ( ccProtL, ccProtR );
rval = pp.parse ();
java.lang.System.out.println ( "DONE! Result from parse(): " + rval );
