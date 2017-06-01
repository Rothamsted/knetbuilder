//package net.sourceforge.ondex.parser.owl;
//
//import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;
//
//import java.util.function.Consumer;
//import java.util.stream.Stream;
//
//import org.apache.jena.atlas.logging.Log;
//import org.apache.jena.ontology.OntClass;
//import org.apache.jena.ontology.OntModel;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import net.sourceforge.ondex.parser.owl.OWLAccsMapperFromAxiom.HelperMapper;
//
///**
// * TODO: comment me!
// *
// * @author brandizi
// * <dl><dt>Date:</dt><dd>31 May 2017</dd></dl>
// *
// */
//public class OwlAxiomRelExtractor implements Consumer<OntClass>
//{
//	
//	public static class HelperMapper extends OWLAxiomMapper<Void, Void>
//	{
//		private Logger log = LoggerFactory.getLogger ( this.getClass () );
//
//		@Override
//		public Void map ( OntClass ontCls, Void ondexTarget )
//		{
//			OntModel model = ontCls.getOntModel ();
//			
//			this.getMappedNodes ( ontCls )
//			.peek ( node -> log.info ( "owlAxiom with node {}", node.toString () ) )
//			.forEach ( node -> ontCls.addProperty ( model.getProperty ( getPropertyIri () ), node ) );
//			return null;
//		}
//	}
//	
//	private String propertyIri;
//
//	private HelperMapper helperMapper = new HelperMapper ();
//
//	
//	@Override
//	public void accept ( OntClass ontCls )
//	{
//		this.helperMapper.setPropertyIri ( this.getPropertyIri () );
//		this.helperMapper.map ( ontCls, null );
//	}
//
//	public String getPropertyIri ()
//	{
//		return propertyIri;
//	}
//
//	public void setPropertyIri ( String propertyIri )
//	{
//		this.propertyIri = propertyIri;
//	}
//	
//}
