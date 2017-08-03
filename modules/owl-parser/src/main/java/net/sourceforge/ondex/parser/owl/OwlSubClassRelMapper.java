//package net.sourceforge.ondex.parser.owl;
//
//import java.util.stream.Stream;
//
//import org.apache.jena.ontology.OntClass;
//
//import info.marcobrandizi.rdfutils.jena.JenaGraphUtils;
//import net.sourceforge.ondex.core.utils.RelationTypePrototype;
//
///**
// * The mapper that follows the tree of rdfs:subClassOf relations from a root OWL class, which is taken from 
// * {@link #getConceptClassMapper()}.
// * 
// * @see OWLMapper.
// *
// * @author brandizi
// * <dl><dt>Date:</dt><dd>12 Apr 2017</dd></dl>
// *
// */
//public class OwlSubClassRelMapper extends OwlRecursiveRelMapper
//{
//	public OwlSubClassRelMapper ()
//	{
//		super ();
//		
//		this.setRelationTypePrototype ( RelationTypePrototype.IS_A_PROTOTYPE );
//	}
//	
//	protected Stream<OntClass> getRelatedClasses ( OntClass fromCls )
//	{
//		return JenaGraphUtils.JENAUTILS.toStream ( fromCls.listSubClasses ( true ), true )
//		.filter ( cls -> !cls.isAnon () ); // These are usually other restrictions and we catch them elsewhere
//
//		// That's it! All the rest, namely following the hierarchy and mapping the nodes met, is done by the Java parent class.
//	}
//}
