package net.sourceforge.ondex.export.sbmlp2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.CompressResultsArguementDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.export.ONDEXExport;
import net.sourceforge.ondex.export.sbml.ArgumentNames;
import net.sourceforge.ondex.tools.functions.StandardFunctions;
import net.sourceforge.ondex.tools.ondex.MdHelper;


/**
 * Exporter for the SBML format level 2.1
 * 
 * @date 24.04.2013
 */
public class Export extends ONDEXExport implements ArgumentNames {

	public static final String COMPARTMENT = "compartment";
	public static final String REACTION = "reaction";
	public static final String SPECIES = "species";
	public static final String LISTOFCOMPARTMENTS = "listOfCompartments";
	public static final String LISTOFREACTIONS = "listOfReactions";
	public static final String LISTOFSPECIES = "listOfSpecies";
	public static final String LISTOFREACTANTS = "listOfReactants";
	public static final String LISTOFPRODUCTS = "listOfProducts";
	public static final String LISTOFMODIFIERS = "listOfModifiers";
	public static final String SPECIESREFERENCE = "speciesReference";
	public static final String MODIFIERSPECIESREFERENCE = "modifierSpeciesReference";
	public static final String SBOTERM = "sboTerm";
	private static final String SBML_NS = "http://www.sbml.org/sbml/level2/version3";
	private static final String W3_NS = "http://www.w3.org/1999/xhtml";
	private static final String NOTES = "notes";
	private static final String BODY = "body";
	private static final String P = "p";
	
	/**
	 * @return Returns arguments for the exporter
	 */
	
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition[] {
			new FileArgumentDefinition(FileArgumentDefinition.EXPORT_FILE,	"SBML export file", true, false, false, false),
			new CompressResultsArguementDefinition(EXPORT_AS_ZIP_FILE,	EXPORT_AS_ZIP_FILE_DESC, false, false),
			new BooleanArgumentDefinition("Name_as_id", "Will use the concept name as id for SBML entities", false, false),
			new BooleanArgumentDefinition("Modifer_list_in_notes", "Will use the concept name as id for SBML entities", false, false)
		};
	}


	/**
	 * @return Returns exporter name
	 */
	@Override
	public String getName() {
		return "SBML-p2";
	}

	/**
	 * @return SBML level
	 */
	public String getSBMLLevel() {
		return "2";
	}

	@Override
	public String getId() {
		return "sbml-p2";
	}

	/**
	 * @return SBML version
	 */
	public String getSBMLVersion() {
		return "1";
	}


	/**
	 * @return Exporter producer version
	 */
	@Override
	public String getVersion() {
		return "24.04.2013";
	}


	/**
	 * @return true if requires indexing flase otherwise
	 */
	public boolean requiresIndexedGraph() {
		return false;
	}

	/**
	 * Starts the export process.
	 */
	@Override
	public void start() throws Exception {
		XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
		File file = new File((String) args.getUniqueValue(FileArgumentDefinition.EXPORT_FILE));
		// compress SBML?
		Boolean packed = ((Boolean) args.getUniqueValue(EXPORT_AS_ZIP_FILE));
		if (packed == null) {
			packed = false;
		}

		OutputStream outStream = null;
		if (packed) {
			// use zip compression
			String zipname = file.getAbsolutePath() + ".gz";
			outStream = new GZIPOutputStream(new FileOutputStream(zipname));
		} else {
			// output file writer
			outStream = new FileOutputStream(file);
		}
		
		Boolean nameAsId = ((Boolean) args.getUniqueValue("Name_as_id"));
		if (nameAsId == null) {
			nameAsId = false;
		}
		
		Boolean mnotes = ((Boolean) args.getUniqueValue("Modifer_list_in_notes"));
		if (mnotes == null) {
			mnotes = false;
		}


		XMLStreamWriter xmlw = new IndentingXMLStreamWriter(xmlof.createXMLStreamWriter(outStream, "UTF-8"));
		// XMLStreamWriter xmlStreamWriter = (XMLStreamWriter)
		// xmlw.createXMLStreamWriter(outStream, "UTF-8");

		// start document build here
		xmlw.writeStartDocument("UTF-8", "1.0");
		xmlw.writeStartElement("sbml");
		xmlw.writeDefaultNamespace(SBML_NS); // this will be xmlns prefix

		xmlw.writeAttribute("level", "2");
		xmlw.writeAttribute("version", "1");
		xmlw.writeStartElement("notes");

		xmlw.writeStartElement("body");
		xmlw.writeDefaultNamespace(W3_NS);
		xmlw.writeAttribute("lang", "en");

		// some provenance info here
		xmlw.writeStartElement("p");
		xmlw.writeCharacters("Written as part of an ONDEX (url http://ondex.org/) export");

		xmlw.writeEndElement();// p
		xmlw.writeEndElement();// body
		xmlw.writeEndElement();// notes
		
		xmlw.writeStartElement("model");
		xmlw.writeAttribute("id", "ONDEX_Export");
		xmlw.writeAttribute("name", "ONDEX Export");

		// only one default compartment
		// list contains only one element
		xmlw.writeStartElement(LISTOFCOMPARTMENTS);
		xmlw.writeStartElement(COMPARTMENT);
		xmlw.writeAttribute("id", "default");
		xmlw.writeAttribute("size", "1");
		xmlw.writeEndElement();// compartment
		xmlw.writeEndElement();// compartments

		// write all species first
		listOfSpecies(xmlw, graph, nameAsId, mnotes);

		// write all reactions next
		listOfReactions(xmlw, graph, nameAsId, mnotes);

		xmlw.writeEndElement();// model
		
		xmlw.flush();
		xmlw.close();

		outStream.flush();
		outStream.close();


		//xmlw.writeEndElement(); // ondex
		xmlw.writeEndDocument();
		System.err.println("End");
	}

	private static void listOfSpecies(XMLStreamWriter xmlw, ONDEXGraph graph, boolean nameAsId, boolean mnotes) throws Exception{
		Set<ONDEXConcept> processed = new HashSet<ONDEXConcept>();
		xmlw.writeStartElement(LISTOFSPECIES);
		RelationType prod_by = MdHelper.createRT(graph, "pd_by");
		RelationType cons_by = MdHelper.createRT(graph, "cs_by");
		RelationType cat_by = MdHelper.createRT(graph, "ca_by");
		RelationType is_a = MdHelper.createRT(graph, "is_a");
		RelationType en_by = MdHelper.createRT(graph, "en_by");
		ConceptClass reaction = MdHelper.createCC(graph, "Reaction");
		for(ONDEXConcept c : graph.getConceptsOfConceptClass(reaction)){
			for(ONDEXConcept c1 : StandardFunctions.getOtherNodes(graph, c, prod_by, cons_by)){
				if(processed.contains(c1)){
					continue;
				}
				processed.add(c1);
				String id = nameAsId==true?getName(c1, "m"):getId(c1, "m");
				String name = getName(c1, "m");
				
				xmlw.writeStartElement(SPECIES);
				xmlw.writeAttribute("id", id);
				xmlw.writeAttribute("name", name);
				xmlw.writeAttribute("compartment", "default");
				xmlw.writeAttribute("initialAmount", "0");
				xmlw.writeAttribute("boundaryCondition", "true");
				xmlw.writeEndElement();// species
			}
			
			if(! mnotes){
				for(ONDEXConcept c3 : StandardFunctions.getOtherNodes(graph, c, cat_by)){
					for(ONDEXConcept c2 : StandardFunctions.getOtherNodes(graph, c3, is_a)){
						for(ONDEXConcept c4 : StandardFunctions.getOtherNodes(graph, c2, en_by)){
							if(processed.contains(c4)){
								continue;
							}
							processed.add(c4);
							String id = nameAsId==true?getName(c4, "m"):getId(c4, "m");
							String name = getName(c4, "m");
							xmlw.writeStartElement(SPECIES);
							xmlw.writeAttribute("id", id);
							xmlw.writeAttribute("name", name);
							xmlw.writeAttribute("compartment", "default");
							xmlw.writeAttribute("initialAmount", "0");
							xmlw.writeAttribute("boundaryCondition", "true");
							xmlw.writeEndElement();// species
						}
					}
				}
			}
		}

		xmlw.writeEndElement();// listOfSpecies
	}

	private static void listOfReactions(XMLStreamWriter xmlw, ONDEXGraph graph, boolean nameAsId, boolean mnotes) throws Exception{
		xmlw.writeStartElement(LISTOFREACTIONS);
		RelationType prod_by = MdHelper.createRT(graph, "pd_by");
		RelationType cons_by = MdHelper.createRT(graph, "cs_by");
		RelationType cat_by = MdHelper.createRT(graph, "ca_by");
		RelationType en_by = MdHelper.createRT(graph, "en_by");
		RelationType is_a = MdHelper.createRT(graph, "is_a");
		ConceptClass reaction = MdHelper.createCC(graph, "Reaction");
		for(ONDEXConcept c : graph.getConceptsOfConceptClass(reaction)){
			// reaction specific info
			String rid =  nameAsId==true?getName(c, "r"):getId(c, "r");
			String rname = getName(c, "r");
			xmlw.writeStartElement(REACTION);
			xmlw.writeAttribute("id", rid);
			xmlw.writeAttribute("name", rname);
			xmlw.writeAttribute("reversible", "false");
			
			Set<String> geneIds = new HashSet<String>();
			for(ONDEXConcept c3 : StandardFunctions.getOtherNodes(graph, c, cat_by)){
				for(ONDEXConcept c2 : StandardFunctions.getOtherNodes(graph, c3, is_a)){
					for(ONDEXConcept c4 : StandardFunctions.getOtherNodes(graph, c2, en_by)){
						geneIds.add(nameAsId==true||mnotes?getName(c4, "m"):getId(c4, "m"));
					}
				}
			}
			
			if(mnotes && geneIds.size() >0){
				xmlw.writeStartElement(NOTES);
				xmlw.writeStartElement(BODY);
				xmlw.writeAttribute("xmlns", "http://www.w3.org/1999/xhtml");
				xmlw.writeStartElement(P);
				StringBuffer sb = new StringBuffer();
				sb.append("GENE_ASSOCIATION: (");
				boolean first = true;
				for(String geneId : geneIds){
					if(!first){
						sb.append(" or ");
					}
					sb.append(geneId);
					first = false;
				}
				sb.append(")");
				xmlw.writeCharacters(sb.toString());
				xmlw.writeEndElement();
				xmlw.writeEndElement();
				xmlw.writeEndElement();
			}

			Set<String> reacIds = new HashSet<String>();
			for(ONDEXConcept c1 : StandardFunctions.getOtherNodes(graph, c, cons_by)){
				reacIds.add(nameAsId==true?getName(c1, "m"):getId(c1, "m"));
			}	

			if(reacIds.size()>0){
				xmlw.writeStartElement("LISTOFREACTANTS");
				for(String reacId: reacIds){
					xmlw.writeStartElement(SPECIESREFERENCE);
					xmlw.writeAttribute("species", reacId);
					xmlw.writeEndElement();
				}
				xmlw.writeEndElement();
			}
			
			Set<String> prodIds = new HashSet<String>();
			for(ONDEXConcept c1 : StandardFunctions.getOtherNodes(graph, c, prod_by)){
				prodIds.add(nameAsId==true?getName(c1, "m"):getId(c1, "m"));
			}
			
			if(prodIds.size() > 0){
				xmlw.writeStartElement("LISTOFPRODUCTS");
				for(String prodId: prodIds){
					xmlw.writeStartElement(SPECIESREFERENCE);
					xmlw.writeAttribute("species", prodId);
					xmlw.writeEndElement();	
				}
				xmlw.writeEndElement();
			}


			if(!mnotes){
				if(geneIds.size() >0){
					xmlw.writeStartElement("LISTOFMODIFIERS");
					for(String geneId : geneIds){
						xmlw.writeStartElement(MODIFIERSPECIESREFERENCE);
						xmlw.writeAttribute("species", geneId);
						xmlw.writeEndElement();
					}
					xmlw.writeEndElement();
				}
			}

			// write default kinetic element
			xmlw.writeStartElement("kineticLaw");
			xmlw.writeStartElement("math");
			xmlw.writeAttribute("xmlns", "http://www.w3.org/1998/Math/MathML");
			xmlw.writeStartElement("cn");
			xmlw.writeAttribute("type", "integer");
			xmlw.writeCharacters("1");

			xmlw.writeEndElement();// cn
			xmlw.writeEndElement();// math
			xmlw.writeEndElement();// kLaw
			xmlw.writeEndElement();// reaction
		}
		xmlw.writeEndElement();// reactions
	}
	
	public static String getName(ONDEXConcept c, String prefix){
		String name;
		ConceptName cn = c.getConceptName();
		if(cn != null ){
			name = c.getConceptName().getName();
			if(name == null || name.trim().equals("")){
				name = prefix+c.getId();
			}
		}
		else{
			name = prefix+c.getId();	
		}
		return name;
	}
	
	public static String getId(ONDEXConcept c, String prefix){
		return prefix+c.getId();
	}


	/**
	 * @return a list of required validators as string tags
	 */
	@Override
	public String[] requiresValidators() {
		return new String[0];
	}
}
