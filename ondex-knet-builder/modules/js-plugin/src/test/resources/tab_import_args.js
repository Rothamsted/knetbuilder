/**
 * This is the usual import code, but the file to parse is taken from scriptArgs, passed by the workflow
 * configuration.
 */
var out = java.lang.System.out;
var inpath = scriptArgs [ 0 ];

out.println ( "Going to parse:" + inpath );

pp = new PathParser( getActiveGraph(), new DelimitedFileReader ( inpath, "\t" ) );
ccProtL = pp.newConceptPrototype ( defAccession ( 0, "UNIPROTKB" ), defCC ( "Protein" ) );
ccProtR = pp.newConceptPrototype ( defAccession ( 1, "UNIPROTKB" ), defCC ( "Protein" ) );
pp.newRelationPrototype ( ccProtL, ccProtR );
rval = pp.parse ();

out.println ( "DONE! Result from parse(): " + rval );
