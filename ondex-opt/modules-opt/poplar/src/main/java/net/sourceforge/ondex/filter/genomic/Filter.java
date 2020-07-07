package net.sourceforge.ondex.filter.genomic;

import java.util.BitSet;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.algorithm.relationneighbours.DepthSensitiveRTValidator;
import net.sourceforge.ondex.algorithm.relationneighbours.NotValidator;
import net.sourceforge.ondex.algorithm.relationneighbours.RelationNeighboursSearch;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.RangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;

/**
 * This Filter accepts as input a genomic interval which is defined by chromosome,
 * start and stop region. It then pulls out the genes within this region and uses
 * them as a seed for an adapted neighbourhood search of depth 4 (relation number),
 * where on each depth only particular Relation Types will be made visible.
 *
 * @author keywan
 */
public class Filter extends ONDEXFilter implements ArgumentNames{

    private static final String NEIGHBOURHOOD = "NEIGHBOURHOOD";
    private static final String GOANNOTATION = "GOA";
    private static final String PATHWAY = "PATHWAY";
    

	private Set<ONDEXConcept> concepts;

    private Set<ONDEXRelation> relations;

    @Override
    public void copyResultsToNewGraph(ONDEXGraph exportGraph) {
        ONDEXGraphCloner graphCloner = new ONDEXGraphCloner(graph, exportGraph);
        for (ONDEXConcept c : concepts) {
            graphCloner.cloneConcept(c);
        }
        for (ONDEXRelation r : relations) {
            graphCloner.cloneRelation(r);
        }
    }

    @Override
    public Set<ONDEXConcept> getVisibleConcepts() {
        return BitSetFunctions.unmodifiableSet(concepts);
    }

    @Override
    public Set<ONDEXRelation> getVisibleRelations() {
        return BitSetFunctions.unmodifiableSet(relations);
    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        RangeArgumentDefinition<Integer> context_arg = new RangeArgumentDefinition<Integer>(
                ArgumentNames.CHROMOSOME_CONCEPT_ARG, ArgumentNames.CHROMOSOME_CONCEPT_ARG_DESC,
                true, null, 1, Integer.MAX_VALUE, Integer.class);
        RangeArgumentDefinition<Integer> from_arg = new RangeArgumentDefinition<Integer>(
                ArgumentNames.FROM_REGION_ARG, ArgumentNames.FROM_REGION_ARG_DESC,
                true, null, 0, Integer.MAX_VALUE, Integer.class);
        RangeArgumentDefinition<Integer> to_arg = new RangeArgumentDefinition<Integer>(
                ArgumentNames.TO_REGION_ARG, ArgumentNames.TO_REGION_ARG_DESC,
                true, null, 0, Integer.MAX_VALUE, Integer.class);
        StringArgumentDefinition algorithm_arg = new StringArgumentDefinition(ArgumentNames.ALGORITHM_ARG, ArgumentNames.ALGORITHM_ARG_DESC, false, "Neighbourhood", false);
        StringArgumentDefinition keyword_arg = new StringArgumentDefinition(ArgumentNames.KEYWORD_ARG, ArgumentNames.KEYWORD_ARG_DESC, false, null, false);

        return new ArgumentDefinition<?>[]{context_arg, from_arg, to_arg, algorithm_arg, keyword_arg};
    }


    @Override
    public String getName() {
        return "Genomic Filter";
    }

    @Override
    public String getVersion() {
        return "05/02/2009";
    }

    @Override
    public String getId() {
        return "genomicBackend";
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws InvalidPluginArgumentException {
        
    	ONDEXGraphMetaData md = graph.getMetaData();
    	AttributeName attStart = md.getAttributeName("BEGIN");
    	RelationType rtEncodes = md.getRelationType("enc");
    	RelationType rtSeqSim = md.getRelationType("h_s_s");
    	RelationType rtOrtho = md.getRelationType("ortho");
    	RelationType rtPara = md.getRelationType("para");
    	RelationType rtPartOf = md.getRelationType("part_of");
    	RelationType rtHasDomain = md.getRelationType("has_domain");
    	RelationType rtISA = md.getRelationType("is_a");
    	RelationType rtISP = md.getRelationType("is_p");
    	RelationType rtReg = md.getRelationType("regulates");
    	RelationType rtPosReg = md.getRelationType("pos_reg");
    	RelationType rtMF = md.getRelationType("has_function");
    	RelationType rtBP = md.getRelationType("participates_in");
    	RelationType rtCC = md.getRelationType("located_in");
    	RelationType rtNotMF = md.getRelationType("not_function");
    	RelationType rtNotBP = md.getRelationType("participates_not");
    	RelationType rtNotCC = md.getRelationType("not_located_in");
    	RelationType rtPubIn = md.getRelationType("pub_in");
    	RelationType rtCatC = md.getRelationType("cat_c");
    	RelationType rtCaBy = md.getRelationType("ca_by");
    	RelationType rtRegBy = md.getRelationType("rg_by");
    	RelationType rtConsBy = md.getRelationType("cs_by");
    	RelationType rtProdBy = md.getRelationType("pd_by");
    	RelationType rtInteractsWith = md.getRelationType("it_wi");
    	RelationType rtCoocWith = md.getRelationType("cooc_wi");
    	RelationType rtOccIn = md.getRelationType("occ_in");
    	RelationType rtPhenotype = md.getRelationType("has_observ_pheno");

        //get all concepts of given chromosome context
        Integer cId = (Integer) args.getUniqueValue(ArgumentNames.CHROMOSOME_CONCEPT_ARG);
        Set<ONDEXConcept> conceptsOfContext = null;
        if (cId != null) {
            ONDEXConcept chromosome = graph.getConcept(cId);
            conceptsOfContext = graph.getConceptsOfTag(chromosome);
        }

        //check if concepts within the min/max region
        int min = (Integer) args.getUniqueValue(ArgumentNames.FROM_REGION_ARG);
        int max = (Integer) args.getUniqueValue(ArgumentNames.TO_REGION_ARG);
        int geneCount = 0;
        BitSet genes = new BitSet();
        for(ONDEXConcept con : conceptsOfContext){
            Integer start = (Integer) con.getAttribute(attStart).getValue();

            if (start != null && start >= min && start <= max) {
                genes.set(con.getId());
                geneCount++;
            }
        }

        Set<ONDEXConcept> seeds = BitSetFunctions.create(graph, ONDEXConcept.class, genes);
        
        String algorithm = args.getUniqueValue(ArgumentNames.ALGORITHM_ARG).toString();
        

        
        if(algorithm.toUpperCase().equals(GOANNOTATION)){

        	//GO relation neighbours
	        RelationNeighboursSearch rn = new RelationNeighboursSearch(graph);
	        DepthSensitiveRTValidator validator = new DepthSensitiveRTValidator();
	        
	        validator.addRelationTypeConstraint(1, rtEncodes);
	        validator.addRelationTypeConstraint(1, rtMF);
	        validator.addRelationTypeConstraint(1, rtNotMF);
	        validator.addRelationTypeConstraint(1, rtBP);
	        validator.addRelationTypeConstraint(1, rtNotBP);
	        validator.addRelationTypeConstraint(1, rtCC);
	        validator.addRelationTypeConstraint(1, rtNotCC);
	        
	        validator.addRelationTypeConstraint(2, rtSeqSim);
	        validator.addRelationTypeConstraint(2, rtOrtho);
	        validator.addRelationTypeConstraint(3, rtMF);
	        validator.addRelationTypeConstraint(3, rtNotMF);
	        validator.addRelationTypeConstraint(3, rtBP);
	        validator.addRelationTypeConstraint(3, rtNotBP);
	        validator.addRelationTypeConstraint(3, rtCC);
	        validator.addRelationTypeConstraint(3, rtNotCC);
	        
	        validator.addRelationTypeConstraint(1, rtPhenotype);
	        
	        rn.setValidator(validator);
	    	
	        rn.search(seeds, 3);
	        concepts = rn.getFoundConcepts();
	        relations = rn.getFoundRelations();
	        rn.shutdown();
        	
        }else if(algorithm.toUpperCase().equals(PATHWAY)){
        	
        	//pathway relation neighbours
	        RelationNeighboursSearch rn = new RelationNeighboursSearch(graph);
	        DepthSensitiveRTValidator validator = new DepthSensitiveRTValidator();
	        
	        validator.addRelationTypeConstraint(1, rtEncodes);
	        validator.addRelationTypeConstraint(2, rtISA);
	        validator.addRelationTypeConstraint(3, rtCatC);
	        validator.addRelationTypeConstraint(3, rtCaBy);
	        validator.addRelationTypeConstraint(4, rtPartOf);
	        validator.addRelationTypeConstraint(4, rtConsBy);
	        validator.addRelationTypeConstraint(4, rtProdBy);
	        validator.addRelationTypeConstraint(4, rtPubIn);
	        
	        rn.setValidator(validator);
	    	
	        rn.search(seeds, 4);
	        concepts = rn.getFoundConcepts();
	        relations = rn.getFoundRelations();
        	rn.shutdown();
	        
        }else{
        	
	        //default relation neighbours
	        RelationNeighboursSearch rn = new RelationNeighboursSearch(graph);
	        DepthSensitiveRTValidator validator = new DepthSensitiveRTValidator();
	        
	        //DO NOT display these relation types at given depth
	        //validator.addRelationTypeConstraint(1, rtPubIn); //QTL to Publication
	        
	        //NOT: gene->GO<-gene
	        validator.addRelationTypeConstraint(2, rtMF);
	        validator.addRelationTypeConstraint(2, rtNotMF);
	        validator.addRelationTypeConstraint(2, rtBP);
	        validator.addRelationTypeConstraint(2, rtNotBP);
	        validator.addRelationTypeConstraint(2, rtCC);
	        validator.addRelationTypeConstraint(2, rtNotCC);
	        validator.addRelationTypeConstraint(2, rtISA);
	        validator.addRelationTypeConstraint(2, rtISP);
	        validator.addRelationTypeConstraint(2, rtPartOf);
	        validator.addRelationTypeConstraint(2, rtReg);
	        validator.addRelationTypeConstraint(2, rtPosReg);
	        validator.addRelationTypeConstraint(2, rtInteractsWith);
	        validator.addRelationTypeConstraint(2, rtPubIn);
	        //NOT: gene->prot->prot<-prot
	        //NOT: gene->prot->prot<-gene
	        validator.addRelationTypeConstraint(3, rtSeqSim);
	        validator.addRelationTypeConstraint(3, rtOrtho);
	        validator.addRelationTypeConstraint(3, rtPara);
	        validator.addRelationTypeConstraint(3, rtPartOf);
	        validator.addRelationTypeConstraint(3, rtReg);
	        validator.addRelationTypeConstraint(3, rtPosReg);
	        validator.addRelationTypeConstraint(3, rtHasDomain);
	        validator.addRelationTypeConstraint(3, rtEncodes);
	        validator.addRelationTypeConstraint(3, rtISA);
	        validator.addRelationTypeConstraint(3, rtISP);
	        validator.addRelationTypeConstraint(3, rtRegBy);
	        //NOT: gene->prot->prot->GO<-prot
	        //NOT: gene->prot->prot->prot->GO
	        validator.addRelationTypeConstraint(4, rtMF);
	        validator.addRelationTypeConstraint(4, rtNotMF);
	        validator.addRelationTypeConstraint(4, rtBP);
	        validator.addRelationTypeConstraint(4, rtNotBP);
	        validator.addRelationTypeConstraint(4, rtCC);
	        validator.addRelationTypeConstraint(4, rtNotCC);
	        validator.addRelationTypeConstraint(4, rtPubIn); //TODO: show publications of PoplarCyc
	        validator.addRelationTypeConstraint(4, rtCatC);
	        validator.addRelationTypeConstraint(4, rtCaBy);
	        validator.addRelationTypeConstraint(4, rtISA);
	        validator.addRelationTypeConstraint(4, rtISP);
	        validator.addRelationTypeConstraint(4, rtPartOf);
	        validator.addRelationTypeConstraint(4, rtReg);
	        validator.addRelationTypeConstraint(4, rtPosReg);
	        validator.addRelationTypeConstraint(4, rtRegBy);
	        validator.addRelationTypeConstraint(4, rtCoocWith);
	        validator.addRelationTypeConstraint(4, rtOccIn);
	        validator.addRelationTypeConstraint(4, rtSeqSim);
	        validator.addRelationTypeConstraint(4, rtOrtho);
	        validator.addRelationTypeConstraint(4, rtPara);
	        validator.addRelationTypeConstraint(4, rtEncodes);
	        validator.addRelationTypeConstraint(4, rtInteractsWith);
	        validator.setInclusivePresumption(false);
	        NotValidator notVal = new NotValidator(validator);
	
	        rn.setValidator(notVal);
	
	        rn.search(seeds, 4);
	        concepts = rn.getFoundConcepts();
	        relations = rn.getFoundRelations();
	        //rn.shutdown();
	        
	        //for web version
//	        if(args.getUniqueValue(KEYWORD_ARG) != null){
//	        	AttributeName attSize = md.getFactory().createAttributeName("size", Integer.class);
//	        	String keyword = args.getUniqueValue(KEYWORD_ARG).toString();
//		        Pattern p = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
//		        
//		        for (ONDEXConcept c : concepts) {
//		        	if(OndexSearch.find(c, p, keyword)){
//		        		c.createAttribute(attSize, new Integer(100), false);
//		        	}
//				}
//	        }
        }
        
        System.out.println(algorithm+" found " + geneCount + " genes. Neighbourhood has " + concepts.size() + " concepts; " + relations.size() + " relations.");

    }

}


