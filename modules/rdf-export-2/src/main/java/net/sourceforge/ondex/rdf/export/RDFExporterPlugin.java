package net.sourceforge.ondex.rdf.export;

import static net.sourceforge.ondex.args.FileArgumentDefinition.EXPORT_FILE;
import static net.sourceforge.ondex.rdf.export.RDFFileExporter.DEFAULT_X_LANG;

import org.apache.commons.lang3.Validate;

import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.export.ONDEXExport;

/**
 * The plugin wrapper, that makes the RDF exporter an ONDEX export plugin.
 * 
 * The new RDF Exporter generates RDF that represent the current ONDEX graph according to the 
 * <a href = "https://github.com/Rothamsted/bioknet-onto">BK-Onto ontology</a>.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>21 Mar 2018</dd></dl>
 *
 */
@Status( status = StatusType.STABLE )
public class RDFExporterPlugin extends ONDEXExport
{

	@Override
	public String getId () {
		return "RDF-Exporter-2";
	}

	@Override
	public String getName ()
	{
		return "RDF Exporter 2";
	}

	@Override
	public String getVersion ()
	{
		return "1.0-SNAPSHOT";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions ()
	{
		return new ArgumentDefinition[] 
		{
      new FileArgumentDefinition ( 
      		EXPORT_FILE, 
      		"The destination RDF file",
      		true, // required
      		false, // preExists
      		false, // isDir
      		false // canBeMultiple
      	),
      new StringArgumentDefinition ( 
		    "rdfLang", 
				"The RDF format to produce. Accepts values from either <a href = 'https://goo.gl/XVQBHi'>RDFFormat</a> " +
				"or <a href = 'https://goo.gl/gbp6bL'>Lang</a>. The default " + DEFAULT_X_LANG + " writes Turtle in an efficient way.", 
				true, // required
				DEFAULT_X_LANG, // default
				false // canBeMultiple
      	)
    	};
	}

	@Override
	public void start () throws Exception
	{
		ONDEXPluginArguments args = this.getArguments ();
		
		String rdfPath = (String) args.getUniqueValue ( EXPORT_FILE );
		Validate.notEmpty ( rdfPath, "Output file not specified" );
		
		String rdfLang = (String) args.getUniqueValue ( "rdfLang" );
		Validate.notEmpty ( rdfLang, "Output RDF language not specified" );
		
		RDFFileExporter fx = new RDFFileExporter ();
		fx.export ( this.graph, rdfPath, rdfLang );		
	}

	@Override
	public boolean requiresIndexedGraph () {
		return false;
	}

	@Override
	public String[] requiresValidators () {
		return new String [ 0 ];
	}

}
