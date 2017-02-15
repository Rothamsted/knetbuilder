package net.sourceforge.ondex.export.json;

import java.util.Set;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.ONDEXConcept;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Build node json objects using their various attributes.
 * @author Ajit Singh
 * @version 04/02/17
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
  String conceptName= " ";
  if(con.getConceptName() != null) {
     if(con.getConceptName().getName() != null) {
        conceptName= con.getConceptName().getName().trim(); // concept name.
       }
    }
  String conceptType= con.getOfType().getFullname(); // conceptType.
  if(conceptType.equals("")) {
//     conceptType= ConceptType.Phenotype.toString(); // default.
     conceptType= con.getOfType().getId();
    }

  // For concept Type: "SNP".
  if(conceptType.equalsIgnoreCase(ConceptType.Compound.toString())) {
     conceptType= ConceptType.SNP.toString();
    }

//  System.out.println("Current "+ conceptType +" Name: "+ conceptName);
  /* Fetch the Set of all concept names and retain only the preferred ones, to later choose the 
   * "best" concept name to display from amongst them, for Genes. */
  if(conceptType.equals(ConceptType.Gene.toString()) || conceptType.equals(ConceptType.Protein.toString())) {
     // For Genes and Proteins.
     // Get the shortest, preferred concept name for this Concept.
     String shortest_coname= getShortestPreferredConceptName(con.getConceptNames());
     // Get the shortest, non-ambiguous concept accession for this Concept.
     String shortest_acc= getShortestNotAmbiguousConceptAccession(con.getConceptAccessions());
     
//     int shortest_acc_length= 100000, shortest_coname_length= 100000; // default values.
     if(!shortest_coname.equals(" ")) {
//        shortest_coname_length= shortest_coname.length();
        conceptName= shortest_coname; // use the shortest, preferred concept name.
       }
     else {
       if(!shortest_acc.equals(" ")) {
//          shortest_acc_length= shortest_acc.length();
          conceptName= shortest_acc; // use the shortest, non-ambiguous concept accession.
         }
      }
/*     if(shortest_acc_length < shortest_coname_length) {
        conceptName= shortest_acc; // use shortest, non-ambiguous concept accession.
       }
     else {
      conceptName= shortest_coname; // use shortest, preferred concept name.
     }*/
//     System.out.println("\t \t Selected (preferred) concept Name: "+ conceptName +"\n");
    }
  else if(conceptType.equals(ConceptType.Phenotype.toString())) {
          if(conceptName.equals(" ")) {
             Set<Attribute> attributes= con.getAttributes(); // get all concept Attributes.
             for(Attribute attr : attributes) {
                 if(attr.getOfType().toString().equals("Phenotype")) {
                    conceptName= attr.getValue().toString().trim(); // use Phenotype as the preferred concept name instead.
                   }
                }
//             System.out.println("\t \t Phenotype: Selected Name: "+ conceptName +"\n");
            }
         }
  else {
    if(!getShortestPreferredConceptName(con.getConceptNames()).equals(" ")) {
       conceptName= getShortestPreferredConceptName(con.getConceptNames());
      }
    else {
      if(!getShortestNotAmbiguousConceptAccession(con.getConceptAccessions()).equals(" ")) {
         conceptName= getShortestNotAmbiguousConceptAccession(con.getConceptAccessions());
        }
     }
//    System.out.println("\t \t Selected (preferred) concept Name: "+ conceptName +"\n");
   }
  System.out.println("AddConceptNodeInfo: conceptID: "+ conceptID +", type: "+ conceptType +", name: "+ conceptName);

  String conceptShape;
  String conceptColour;
  String conceptSize= "18px"; // default.
  String conceptVisibility= defaultVisibility; // default (element, i.e., true).

  /* Check if concept Name (value) is highlighted via HTML span tags.
   * If yes (html span tag present), strip html <span> tags and highlight background */
  String val= conceptName;
  String concept_text_bgColor= "black", concept_text_bgOpacity= "0";
  if(conceptName.contains("<span")) {
     //val= "<html>"+ conceptName +"</html>";
     concept_text_bgColor= "gold";
     concept_text_bgOpacity= "1";
     // remove all html content (including <span> tags) from conceptName to be displayed
     Document doc = Jsoup.parse(val);
     val= doc.text(); //doc.select("span").remove().toString();
    }
  // Trim the label's (conceptName) length.
  if(val.length()>30) { val= val.substring(0, 29) +"...";}
  System.out.println("concept: trimmed displayValue: "+ val);
  
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
     colour= ConceptColour.darkGreen.toString();
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
     shape= ConceptShape.triangle.toString();
     colour= ConceptColour.greenYellow.toString();
    }
  else if((conType.equals(ConceptType.Compound.toString())) || (conType.equals(ConceptType.SNP.toString()))) {
     shape= ConceptShape.star.toString();
     colour= ConceptColour.teal.toString();
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
  else if((conType.equals(ConceptType.Protein_Complex.toString())) || (conType.equals("Protein Complex"))) {
     shape= ConceptShape.roundrectangle.toString();
     colour= ConceptColour.red.toString();
    }
  else if(conType.equals(ConceptType.Transport.toString())) {
     shape= ConceptShape.diamond.toString();
     colour= ConceptColour.lightBlue.toString();
    }
  
  // Set the determined attribute values;
  attr[0]= shape;
  attr[1]= colour;

  return attr;
 }

    private String getShortestPreferredConceptName(Set<ConceptName> conames) {
     String shortest_coname=" ";
     int length= 100000;
     for(ConceptName coname : conames) {
//         System.out.println("\t coname: "+ coname.getName().trim() +", isPreferred: "+ coname.isPreferred());
         if((coname.isPreferred()) && (coname.getName() != null)) {
//            if((coname.getName().trim().length() >= 3) && (coname.getName().trim().length() <= 6)) {
            if(coname.getName().trim().length() <= length) {
               shortest_coname= coname.getName().trim(); // use this preferred concept name instead.
               length= shortest_coname.length();
              }
           }
        }
//     System.out.println("\t shortest_coname: "+ shortest_coname);
     return shortest_coname;
    }

    private String getShortestNotAmbiguousConceptAccession(Set<ConceptAccession> co_accs) {
     String shortest_acc=" ";
     int length= 100000;
     for(ConceptAccession acc : co_accs) {
//         System.out.println("\t acc: "+ acc.getAccession().trim() +", isAmbiguous: "+ acc.isAmbiguous());
         if(!(acc.isAmbiguous()) && (acc.getAccession().trim().length() <= length)) {
            shortest_acc= acc.getAccession().trim();
	    length= shortest_acc.length();
           }
        }
//     System.out.println("\t shortest_acc: "+ shortest_acc);
     return shortest_acc;
    }

}
