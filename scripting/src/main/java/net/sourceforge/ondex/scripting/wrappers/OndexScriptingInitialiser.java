package net.sourceforge.ondex.scripting.wrappers;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.base.AbstractConcept;
import net.sourceforge.ondex.core.base.AbstractONDEXGraphMetaData;
import net.sourceforge.ondex.core.base.AbstractRelation;
import net.sourceforge.ondex.core.memory.MemoryONDEXConcept;
import net.sourceforge.ondex.core.memory.MemoryONDEXRelation;
import net.sourceforge.ondex.core.persistent.BerkeleyConcept;
import net.sourceforge.ondex.core.persistent.BerkeleyONDEXGraph;
import net.sourceforge.ondex.core.persistent.BerkeleyRelation;
import net.sourceforge.ondex.scripting.*;
import net.sourceforge.ondex.scripting.base.JavaProxyTemplate;
import net.sourceforge.ondex.scripting.base.UniversalProxyTemplateBuilder;
import net.sourceforge.ondex.scripting.javascript.JSInterpreter;
import net.sourceforge.ondex.scripting.ui.ScriptingShell;
import net.sourceforge.ondex.tools.subgraph.*;
import net.sourceforge.ondex.tools.tab.importer.DataReader;
import net.sourceforge.ondex.tools.tab.importer.DelimitedReader;
import net.sourceforge.ondex.tools.tab.importer.PathParser;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class OndexScriptingInitialiser extends AbstractScriptingInitialiser {

    protected boolean isRAvailable = false;
    private static boolean isInitialized = false;
    private static ContextualReferenceResolver<ONDEXGraph> graphResolver;

    /**
     * Resets everything and causes the next calls to initialisation methods to redo their job.
     * Marco Brandizi: I cannot make the scripting engine to work twice in the same run without this.
     */
    public static void reset ()
    {
    	isInitialized = false;
    }
    
    public static ScriptingShell getScriptingShell() {

        ScriptingShell cmd = new ScriptingShell("OVTK>", "Command console v1.0 Sorry - no support documentation available at the moment.");
        mainOutputPrinter = cmd;

        OndexScriptingInitialiser.initialiseProxyTemplateBuilder();
        JavaProxyTemplate proxyTemplate = getProxyTemplateWithDoc();
        InterpretationController ic = new BasicInterpretationController(new TemplateBuilder[0], new ProxyTemplate[]{proxyTemplate}, new CommandInterpreter[]{new JSInterpreter()});
        cmd.addCommandListener(ic);

        return cmd;

    }

    /**
     * Creates Scripting_ref.htm, if not already there.
     * @return proxyTemplateBuilder.getProxyTemplate()
     */
    public static JavaProxyTemplate getProxyTemplateWithDoc() {
        JavaProxyTemplate proxyTemplate;
        File f = new File("Scripting_ref.htm");
        if (f.exists()){
        	System.out.println("Scripting doc already exists at: "+f.getAbsolutePath());
            proxyTemplate = proxyTemplateBuilder.getProxyTemplate();
        } else {
        	System.out.println("New scripting doc was generated at: "+f.getAbsolutePath());
            proxyTemplate = proxyTemplateBuilder.getTemplateWithDoc();
        }
        return proxyTemplate;
    }

    public synchronized static UniversalProxyTemplateBuilder getProxyTemplateBuilder() {
        if (!isInitialized) {
            initialiseProxyTemplateBuilder();
        }
        return proxyTemplateBuilder;
    }

    protected synchronized static void initialiseProxyTemplateBuilder() {
        if (isInitialized) return;
        AbstractScriptingInitialiser.initialiseProxyTemplateBuilder();

        /* Application base*/
        // AbstractONDEXGraph
        try{
            proxyTemplateBuilder.addFunctionMethodByName(OndexScriptingInitialiser.class, "getAbstractONDEXGraph", "getActiveGraph"); 	
        }
        catch (Exception e){e.printStackTrace();}

        proxyTemplateBuilder.addAllMethodsAsFunctions(General.class);
        //Standard out
        try{
            proxyTemplateBuilder.addFunctionMethodByName(OndexScriptingInitialiser.class, "out", "out");     	
        }
        catch (Exception e){e.printStackTrace();}

        try{
        	proxyTemplateBuilder.addRootMethod(UniversalProxyTemplateBuilder.getMethodByName(OndexScriptingInitialiser.class, "getMetaData"));	
        }
        catch (Exception e){e.printStackTrace();}
       


        //Global functions//
        try{
        	proxyTemplateBuilder.addConvinience(DataSource.class, String.class, UniversalProxyTemplateBuilder.getMethodByName(ONDEXGraphMetaData.class, "getDataSource"));
        }
        catch (Exception e){e.printStackTrace();}
        
        try{
            proxyTemplateBuilder.addConvinience(AttributeName.class, String.class, UniversalProxyTemplateBuilder.getMethodByName(ONDEXGraphMetaData.class, "getAttributeName")); 	
        }
        catch (Exception e){e.printStackTrace();}

        try{
            proxyTemplateBuilder.addConvinience(ConceptClass.class, String.class, UniversalProxyTemplateBuilder.getMethodByName(ONDEXGraphMetaData.class, "getConceptClass"));   	
        }
        catch (Exception e){e.printStackTrace();}

        try{
            proxyTemplateBuilder.addConvinience(EvidenceType.class, String.class, UniversalProxyTemplateBuilder.getMethodByName(ONDEXGraphMetaData.class, "getEvidenceType"));	
        }
        catch (Exception e){e.printStackTrace();}

        try{
            proxyTemplateBuilder.addConvinience(RelationType.class, String.class, UniversalProxyTemplateBuilder.getMethodByName(ONDEXGraphMetaData.class, "getRelationType"));	
        }
        catch (Exception e){e.printStackTrace();}

        try{
            proxyTemplateBuilder.addConvinience(Unit.class, String.class, UniversalProxyTemplateBuilder.getMethodByName(ONDEXGraphMetaData.class, "getUnit"));
        }
        catch (Exception e){e.printStackTrace();}

        
        proxyTemplateBuilder.addAllMethodsAsFunctions(DefConst.class);
        proxyTemplateBuilder.addAllMethodsAsFunctions(General.class);


        //methods that will be hidden from the scripting environment//
        Set<String> toExclude = new HashSet<String>();
        toExclude.add("serialise");
        toExclude.add("deserialise");
        toExclude.add("getONDEXONDEXListeners");
        toExclude.add("fireUpdate");
        toExclude.add("removeUpdateListener");
        toExclude.add("setUpdateListener");
        toExclude.add("fireEventOccurred");
        toExclude.add("addONDEXONDEXListener");
        toExclude.add("removeONDEXONDEXListener");
        toExclude.add("getUpdateListener");
        toExclude.add("setPermissions");
        toExclude.add("getPermissions");
        toExclude.add("getSID");
        toExclude.add("getActionListeners");

        ProxyTemplateBuilder tGraph = proxyTemplateBuilder.spawnTemplate("Graph", ONDEXGraph.class, false);

        tGraph.autoAddExclusive(toExclude);
        tGraph.addSource(BerkeleyONDEXGraph.class);
        tGraph.addSource(ONDEXGraph.class);

        ProxyTemplateBuilder tView = proxyTemplateBuilder.spawnTemplate("View", Set.class, true);
        tView.addSource(Set.class);
        ProxyTemplateBuilder metadata = proxyTemplateBuilder.spawnTemplate("Metadata", ONDEXGraphMetaData.class, true);
        metadata.addSource(AbstractONDEXGraphMetaData.class);
//		proxyTemplateBuilder.spawnTemplate("OndexIterator", Set.class, true);

        ProxyTemplateBuilder tEvidenceType = proxyTemplateBuilder.spawnTemplate("Evidence", EvidenceType.class, false);
        tEvidenceType.autoAddExclusive(toExclude);

        //ConceptAccession
        ProxyTemplateBuilder tConceptAccession = proxyTemplateBuilder.spawnTemplate("Accession", ConceptAccession.class, false);
        tConceptAccession.autoAddExclusive(toExclude);

        //AttributeName
        ProxyTemplateBuilder tAttributeName = proxyTemplateBuilder.spawnTemplate("AttributeName", AttributeName.class, false);
        tAttributeName.addEmbeddedClass(AbstractONDEXGraphMetaData.class);
        tAttributeName.autoAddExclusive(toExclude);

        //ConceptClass

        ProxyTemplateBuilder tConceptClass = proxyTemplateBuilder.spawnTemplate("ConceptClass", ConceptClass.class, false);
        tConceptClass.addFunction("acc", "createConceptAccession");

        tConceptClass.autoAddExclusive(toExclude);

        //ConceptName
        ProxyTemplateBuilder tConceptName = proxyTemplateBuilder.spawnTemplate("ConceptName", ConceptName.class, false);
        tConceptName.autoAddExclusive(toExclude);

        //RelationKey
        ProxyTemplateBuilder tRelationKey = proxyTemplateBuilder.spawnTemplate("RKey", RelationKey.class, false);
        tRelationKey.autoAddExclusive(toExclude);

        //RelationType
        ProxyTemplateBuilder tRelationType = proxyTemplateBuilder.spawnTemplate("Type", RelationType.class, false);
        tRelationType.autoAddExclusive(toExclude);

        //Unit
        ProxyTemplateBuilder tUnit = proxyTemplateBuilder.spawnTemplate("Unit", Unit.class, false);
        tUnit.autoAddExclusive(toExclude);


        ProxyTemplateBuilder tCV = proxyTemplateBuilder.spawnTemplate("DataSource", DataSource.class, false);
        tCV.autoAddExclusive(toExclude);

        ProxyTemplateBuilder tGDS = proxyTemplateBuilder.spawnTemplate("Attribute", Attribute.class, false);
        tGDS.autoAddExclusive(toExclude);
        //tGDS.addSource(ConceptAttribute.class);
        tGDS.addSource(Attribute.class);
        //tGDS.addSource(RelationAttribute.class);

        ProxyTemplateBuilder tConcept = proxyTemplateBuilder.spawnTemplate("Concept", AbstractConcept.class, false);
        tConcept.addEmbeddedClass(AbstractONDEXGraphMetaData.class);
        tConcept.addFunction("acc", "createConceptAccession");
        tConcept.autoAddExclusive(toExclude);
        tConcept.addSource(BerkeleyConcept.class);
        tConcept.addSource(ONDEXConcept.class);
        tConcept.addSource(MemoryONDEXConcept.class);

        ProxyTemplateBuilder tRelation = proxyTemplateBuilder.spawnTemplate("Relation", AbstractRelation.class, false);
        tRelation.addEmbeddedClass(AbstractONDEXGraphMetaData.class);
        tRelation.autoAddExclusive(toExclude);
        tRelation.addSource(BerkeleyRelation.class);
        tRelation.addSource(MemoryONDEXRelation.class);
        tRelation.addSource(ONDEXRelation.class);
        //proxyTemplateBuilder.spawnTemplate("Instances", Instances.class, true);
        proxyTemplateBuilder.spawnTemplate("AttributePrototype", AttributePrototype.class, true);
        proxyTemplateBuilder.spawnTemplate("ConceptPrototype", ConceptPrototype.class, true);
        proxyTemplateBuilder.spawnTemplate("RelationPrototype", RelationPrototype.class, true);
        proxyTemplateBuilder.spawnTemplate("Subgraph", Subgraph.class, true);

        proxyTemplateBuilder.spawnTemplate("DelimitedFileReader", DelimitedReader.class, true);
        proxyTemplateBuilder.spawnTemplate("DataReader", DataReader.class, true);
        proxyTemplateBuilder.spawnTemplate("PathParser", PathParser.class, true);
        isInitialized = true;
        //datareader.addSource(TestDataReader.class);
    }

    public static void out(Object v) {
        System.out.println(v);
    }

    //TODO this needs a cleaner and solution.
    public static ContextualReferenceResolver<ONDEXGraph> getGraphResolver() {
        return graphResolver;
    }

    public static ONDEXGraphMetaData getMetaData() throws Exception {
        return OndexScriptingInitialiser.getAbstractONDEXGraph().getMetaData();
    }

    //TODO this needs a cleaner and solution.
    public static void setGraphResolver(
            ContextualReferenceResolver<ONDEXGraph> graphResolver) {
        OndexScriptingInitialiser.graphResolver = graphResolver;
    }

    public static net.sourceforge.ondex.core.ONDEXGraph getAbstractONDEXGraph() throws Exception {
        if (graphResolver != null) {
            return graphResolver.resolveRef(null);
        }
        throw new FunctionException("Incorrect context setting to resolve this call!", -1);
    }
}
