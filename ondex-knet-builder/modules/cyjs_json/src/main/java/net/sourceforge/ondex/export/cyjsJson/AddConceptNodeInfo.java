package net.sourceforge.ondex.export.cyjsJson;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.util.GraphLabelsUtils;
import uk.ac.ebi.utils.regex.RegEx;

/**
 * Build node json objects using their various attributes.
 * @author Ajit Singh
 * @version 16/07/18
 */
public class AddConceptNodeInfo {

 private String defaultVisibility= null;

 public AddConceptNodeInfo() {
  defaultVisibility= ElementVisibility.none.toString();
 }

 public JSONObject getNodeJson(ONDEXConcept con, Set<Integer> conceptsUsedInRelations) {

  JSONObject node= new JSONObject();
  JSONObject nodeData= new JSONObject();
  int conId= con.getId(); // concept ID.
  String conceptID= String.valueOf(conId);
  String conceptType= con.getOfType().getFullname(); // conceptType.
  if(conceptType.equals("")) {
//     conceptType= ConceptType.Phenotype.toString(); // default.
     conceptType= con.getOfType().getId();
    }

  if(conceptType.equalsIgnoreCase("Comp")) { // added:27-04-2020
     conceptType= ConceptType.Compound.toString();
    }

  // For conceptType "Compound", change to "SNP" (added:15-10-15), (disabled:27-04-20).
/*  if(conceptType.equalsIgnoreCase(ConceptType.Compound.toString())) {
     conceptType= ConceptType.SNP.toString();
    } */

//  System.out.println("Current "+ conceptType +" Name: "+ conceptName);
  /* Fetch the Set of all concept names and retain only the preferred ones, to later choose the 
   * "best" concept name to display from amongst them, for Genes. */
  
  String conceptName = GraphLabelsUtils.getBestConceptLabel ( con, true, 0 );
  
  String conceptShape;
  String conceptColour;
  String conceptSize= "18px"; // default.
  String conceptVisibility= defaultVisibility; // default (element, i.e., true).

  /* Check if concept Name (value) is highlighted via HTML span tags.
   * If yes (html span tag present), strip html <span> tags and highlight background */
  // TODO: How is it possible!?!?
  
  String val= conceptName;
  String concept_text_bgColor= "black", concept_text_bgOpacity= "0";
  
  if(conceptName.contains("<span")) 
  {
     //val= "<html>"+ conceptName +"</html>";
     concept_text_bgColor= "gold";
     
     // if a color is already provided within the <span> tag, use that HEX colour code
     Matcher hexRe = RegEx.of ( 
        // style="background-color:#0123FF"
    		".*style\\s*=\\s*[\\\",']\\s*background-color\\s*:\\s*(#[0-9,A-F]{6})\\s*[\\\",'].*", 
    		Pattern.CASE_INSENSITIVE )
    		.matcher ( conceptName );
     if ( hexRe.matches () )
    	 concept_text_bgColor = hexRe.group ( 1 );
     
     concept_text_bgOpacity= "1";

     // remove all html content (including <span> tags) from conceptName to be displayed
     Document doc = Jsoup.parse(val);
     val= doc.text(); //doc.select("span").remove().toString();
  }
  
  // TODO: getBestConceptLabel() normally cut at 63 (including dots)
  // TODO: this is completely ignoring the case where name is like "...<span...><b>keyword</b></span>", we
  // should move the formatted name into a dedicated attribute and leave the original name untouched
  val = StringUtils.abbreviate ( val, 33 );
  
  nodeData.put(JSONAttributeNames.ID, conceptID);
  nodeData.put(JSONAttributeNames.VALUE, conceptName);
  nodeData.put("conceptType", conceptType); // conceptType ("ofType").  
  nodeData.put(JSONAttributeNames.PID, con.getPID());
  nodeData.put("displayValue", val);
  nodeData.put("conceptTextBGcolor", concept_text_bgColor);
  nodeData.put("conceptTextBGopacity", concept_text_bgOpacity);
  nodeData.put(JSONAttributeNames.ANNOTATION, con.getAnnotation().replaceAll("(\\r|\\n)", " "));
  // Set the shape, color & visibility attributes for this Concept.
  String[] nodeAttributes= determineNodeColourAndShape(conceptType);
  conceptShape= nodeAttributes[0];
  conceptColour= nodeAttributes[1];

  nodeData.put("conceptShape", conceptShape);
  nodeData.put("conceptColor", conceptColour);

/*  if(conceptsUsedInRelations.contains(conId)) {
     conceptVisibility= ElementVisibility.element.toString();
//     System.out.println("ConceptID: "+ conId +" , visibleDisplay: "+ conceptVisibility);
    }
  else {
     conceptVisibility= ElementVisibility.none.toString();
//     System.out.println("ConceptID: "+ conId +" , visibleDisplay: "+ conceptVisibility);
    }*/

  // Set concept visibility, concept size (height & width) & flagged status from Attributes.
  String attrID, visibility, flagged= "false";
  int con_size;
  Set<Attribute> concept_attributes= con.getAttributes(); // get all concept Attributes.
  for(Attribute attr : concept_attributes) {
      attrID= attr.getOfType().getId(); // Attribute ID.
      if(attrID.equals("")) {
         attrID= attr.getOfType().getFullname();
        }

      if(attrID.equals("visible")) { // set visibility.
         visibility= attr.getValue().toString();
         if(visibility.equals("false")) {
            conceptVisibility= ElementVisibility.none.toString();
           }
         else {
           conceptVisibility= ElementVisibility.element.toString();
          }
        }
      else if(attrID.equals("size")) { // set size.
              con_size= Integer.parseInt(attr.getValue().toString());
              if(con_size > 18 && con_size <= 30) {
                 con_size= 22;
                }
              else if(con_size > 30) {
                      con_size= 26;
                     }
//              conceptSize= attr.getValue().toString() +"px";
              conceptSize= String.valueOf(con_size) +"px";
             }

      else if(attrID.equals("flagged")) { // set flagged status.
              flagged= attr.getValue().toString(); // true
             }
     }
  // Flagged gene: visual attributes
  String concept_borderStyle= "solid", concept_borderWidth= "1px", concept_borderColor= "black";
  if(flagged.equals("true")) {
     concept_borderStyle= "double";
     concept_borderWidth= "3px";
     concept_borderColor= "navy";
    }

  nodeData.put("conceptDisplay", conceptVisibility);
  nodeData.put("conceptSize", conceptSize);
  nodeData.put("flagged", flagged);
  nodeData.put("conceptBorderStyle", concept_borderStyle);
  nodeData.put("conceptBorderWidth", concept_borderWidth);
  nodeData.put("conceptBorderColor", concept_borderColor);

  node.put("data", nodeData); // the node's data.
  node.put("group", "nodes"); // Grouping nodes together
 
  return node;
 }

 private String[] determineNodeColourAndShape(String conType) {
  String[] attr= new String[2];
  String shape= ConceptShape.rectangle.toString(); // default (for concept Type: 'Phenotype').
  String colour= ConceptColour.greenYellow.toString(); // default (for concept Type: 'Phenotype').

  // Determine the shape & colour attributes for this concept based on the concept type.
  if(conType.equals(ConceptType.Biological_Process.toString())) {
     shape= ConceptShape.pentagon.toString();
     colour= ConceptColour.teal.toString();
    }
  else if(conType.equals(ConceptType.Gene.toString())) { // Gene
     shape= ConceptShape.triangle.toString();
     colour= ConceptColour.lightBlue.toString();
    }
  else if(conType.equals(ConceptType.Cellular_Component.toString())) {
     shape= ConceptShape.pentagon.toString();
     colour= ConceptColour.springGreen.toString();
    }
  else if(conType.equals("Protein Domain")) {
     shape= ConceptShape.pentagon.toString();
     colour= ConceptColour.lightGrey.toString();
    }
  else if(conType.equals(ConceptType.Drug.toString())) {
     shape= ConceptShape.ellipse.toString();
     colour= ConceptColour.slateBlue.toString();
    }
  else if(conType.equals(ConceptType.Disease.toString())) {
     shape= ConceptShape.triangle.toString();
     colour= ConceptColour.lightGreen.toString();
    }
  else if(conType.equals(ConceptType.DGES.toString())) {
     shape= ConceptShape.triangle.toString();
     colour= ConceptColour.tan.toString();
    }
  else if(conType.equals(ConceptType.Pathway.toString())) {
     shape= ConceptShape.star.toString();
     colour= ConceptColour.springGreen.toString();
    }
  else if(conType.equals(ConceptType.Reaction.toString())) {
     shape= ConceptShape.star.toString();
     colour= ConceptColour.greenYellow.toString();
    }
  else if(conType.equals(ConceptType.Publication.toString())) {
     shape= ConceptShape.rectangle.toString();
     colour= ConceptColour.orange.toString();
    }
  else if(conType.equals(ConceptType.Protein.toString())) {
     shape= ConceptShape.ellipse.toString();
     colour= ConceptColour.red.toString();
    }
  else if(conType.equals(ConceptType.Enzyme.toString())) {
     shape= ConceptShape.star.toString();
     colour= ConceptColour.salmon.toString();
    }
  else if(conType.equals(ConceptType.Molecular_Function.toString())) {
     shape= ConceptShape.pentagon.toString();
     colour= ConceptColour.purple.toString();
    }
  else if((conType.equals(ConceptType.Enzyme_Classification.toString())) || (conType.equals("Enzyme Classification"))) {
     shape= ConceptShape.pentagon.toString();
     colour= ConceptColour.pink.toString();
    }
  else if(conType.equals("Trait Ontology")) {
     shape= ConceptShape.pentagon.toString();
     colour= ConceptColour.greenYellow.toString();
    }
  else if(conType.equals("Quantitative Trait Locus")) {
     shape= ConceptShape.triangle.toString();
     colour= ConceptColour.red.toString();
    }
  else if(conType.equals(ConceptType.Scaffold.toString())) {
     shape= ConceptShape.triangle.toString();
     colour= ConceptColour.blue.toString();
    }
  else if(conType.equals(ConceptType.Trait.toString())) {
     shape= ConceptShape.pentagon.toString(); //ConceptShape.triangle.toString();
     colour= ConceptColour.greenYellow.toString();
    }
  else if(conType.equals(ConceptType.SNP.toString())) {
     shape= ConceptShape.star.toString();
     colour= ConceptColour.teal.toString();
    }
  else if((conType.equals(ConceptType.Compound.toString())) || (conType.equals("Comp"))) {
     shape= ConceptShape.heptagon.toString();
     colour= ConceptColour.springGreen.toString();
    }
  else if(conType.equals(ConceptType.Allele.toString())) {
     shape= ConceptShape.star.toString();
     colour= ConceptColour.purple.toString();
    }
  else if(conType.equals(ConceptType.Phenotype.toString())) {
     shape= ConceptShape.rectangle.toString();
     colour= ConceptColour.greenYellow.toString();
    }
  else if(conType.equals(ConceptType.Chromosome.toString())) {
     shape= ConceptShape.octagon.toString();
     colour= ConceptColour.blue.toString();
    }
  else if(conType.equals(ConceptType.RNA.toString())) {
     shape= ConceptShape.hexagon.toString();
     colour= ConceptColour.teal.toString();
    }
  else if((conType.equals(ConceptType.Protein_Complex.toString())) || (conType.equals("Protein Complex")) || (conType.equals(ConceptType.Protcmplx.toString()))) {
     shape= ConceptShape.roundrectangle.toString();
     colour= ConceptColour.red.toString();
    }
  else if(conType.equals(ConceptType.Transport.toString())) {
     shape= ConceptShape.diamond.toString();
     colour= ConceptColour.lightBlue.toString();
    }
  else if(conType.equals(ConceptType.CoExpStudy.toString())) {
     shape= ConceptShape.rhomboid.toString();
     colour= ConceptColour.tan.toString();
    }
  else if(conType.equals(ConceptType.CoExpCluster.toString())) {
     shape= ConceptShape.vee.toString();
     colour= ConceptColour.tan.toString();
    }
  else if(conType.equals(ConceptType.PlantOntologyTerm.toString())) {
     shape= ConceptShape.pentagon.toString();
     colour= ConceptColour.lightGreen.toString();
    }
  else if(conType.equals(ConceptType.SNPEffect.toString())) {
     shape= ConceptShape.diamond.toString();
     colour= ConceptColour.slateBlue.toString();
    }
  
  // Set the determined attribute values;
  attr[0]= shape;
  attr[1]= colour;

  return attr;
 }
}
