/**
 * ONDEX RDF Export (version 2)
 * 
 * This is based on the <a href = 'https://github.com/EBIBioSamples/java2rdf'>java2rdf</a> library and it's
 * written so that there is an {@link net.sourceforge.ondex.rdf.export.RDFExporter abstract exporter} 
 * creating chunks of RDF triples, which are sent to export handlers that can do something specific with
 * the exported RDF. We provide the an handler for the
 * {@link net.sourceforge.ondex.rdf.export.RDFFileExporter file exporter}.
 * 
 * The mapping from ONDEX to RDF is based on the <a href = "https://github.com/Rothamsted/bioknet-onto">bioknet ontology</a>.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 Nov 2017</dd></dl>
 *
 */
package net.sourceforge.ondex.rdf.export;