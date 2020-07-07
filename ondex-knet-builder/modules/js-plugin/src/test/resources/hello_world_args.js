var args = scriptArgs;
var out = java.lang.System.out;
out.println ( "Hello, World!" );
out.println ( "The args received from scriptArgs" );
for ( var i = 0; i < args.length; i++ )
	out.println ( "" + i + ": " + args [ i ] )
