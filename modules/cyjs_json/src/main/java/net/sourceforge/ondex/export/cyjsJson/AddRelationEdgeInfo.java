package net.sourceforge.ondex.export.cyjsJson;

import java.util.Set;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXRelation;
import org.json.simple.JSONObject;

/**
 * Build edge json objects using their various attributes.
 * @author Ajit Singh
 * @version 04/02/17
 */
public class AddRelationEdgeInfo {
    
 private String defaultVisibility= null;
 
 public AddRelationEdgeInfo() {
  defaultVisibility= ElementVisibility.none.toString();
 }

 public JSONObject getEdgeJson(ONDEXRelation rel) {
  JSONObject edge= new JSONObject();
  JSONObject edgeData= new JSONObject();

  String relationID= String.valueOf(rel.getId()); // relation ID.
  String sourceConcept= String.valueOf(rel.getFromConcept().getId()); // relation source ID.
  String targetConcept= String.valueOf(rel.getToConcept().getId()); // relation target ID.
  String edgeLabel= rel.getOfType().getFullname(); // relation type label.
  if(edgeLabel.equals("")) {
     edgeLabel= rel.getOfType().getId();
    }
//  System.out.println("AddRelationEdgeInfo: Relation ID: e"+ relationID +", label: "+ edgeLabel +", from: "+ sourceConcept +", to: "+ targetConcept);

  edgeData.put(/*"id"*/JSONAttributeNames.ID, "e"+relationID);
  edgeData.put("source", sourceConcept);
  edgeData.put("target", targetConcept);
  edgeData.put("label", edgeLabel);
  
  // Set the edge color for this Relation.
  String edgeColour= determineEdgeColour(edgeLabel);
  edgeData.put("relationColor", edgeColour);

  String relationSize= "1px"; // default.
  String relationVisibility= defaultVisibility; // default (element, i.e., true).
  // Set relation visibility & relation size (width) from Attributes.
  String attrID, visibility;
  int rel_size;
  Set<Attribute> rel_attributes= rel.getAttributes(); // get all relation Attributes.
  for(Attribute attr : rel_attributes) {
      attrID= attr.getOfType().getId(); // Attribute ID.
      if(attrID.equals("")) {
         attrID= attr.getOfType().getFullname();
        }

      if(attrID.equals("visible")) { // set visibility.
         visibility= attr.getValue().toString();
         if(visibility.equals("false")) {
            relationVisibility= ElementVisibility.none.toString();
           }
         else {
           relationVisibility= ElementVisibility.element.toString();
          }
        }
      else if(attrID.equals("size")) { // set size.
              rel_size= Integer.parseInt(attr.getValue().toString());
              if(rel_size > 2) {
                 rel_size= 3;
                }
//              relationSize= attr.getValue().toString() +"px";
              relationSize= String.valueOf(rel_size) +"px";
             }
     }

  edgeData.put("relationDisplay", relationVisibility);
  edgeData.put("relationSize", relationSize);

  edge.put("data", edgeData); // the edge's data.
  edge.put("group", "edges"); // Grouping edges together

  return edge;
 }

 private String determineEdgeColour(String edgeLbl) {
  String colour= EdgeColour.black.toString(); // default.

  // Determine the colour for this relation based on the edge label.
  if(edgeLbl.equals(EdgeLabel.published_in.toString())) {
     colour= EdgeColour.orange.toString();
    }
  else if(edgeLbl.equals(EdgeLabel.encodes.toString())) {
     colour= EdgeColour.grey.toString();
    }
  else if(edgeLbl.equals(EdgeLabel.participates_in.toString())) {
     colour= EdgeColour.teal.toString();
    }
  else if(edgeLbl.equals(EdgeLabel.has_similar_sequence.toString())) {
     colour= EdgeColour.fireBrick.toString();
    }
  else if((edgeLbl.equals(EdgeLabel.cooccurs_with.toString()))) {
     colour= EdgeColour.blue.toString();
    }
  else if(edgeLbl.equals(EdgeLabel.has_protein_domain.toString())) {
     colour= EdgeColour.crimson.toString();
    }
  else if(edgeLbl.equals(EdgeLabel.interacts_with.toString())) {
     colour= EdgeColour.steelBlue.toString();
    }
  else if(edgeLbl.equals(EdgeLabel.located_in.toString())) {
     colour= EdgeColour.springGreen.toString();
    }
  else if(edgeLbl.equals(EdgeLabel.has_function.toString())) {
     colour= EdgeColour.purple.toString();
    }
  else if(edgeLbl.equals(EdgeLabel.orthologue.toString())) {
     colour= EdgeColour.red.toString();
    }
  else if(edgeLbl.equals(EdgeLabel.is_p.toString())) {
     colour= EdgeColour.darkGrey.toString();
    }
  else if(edgeLbl.equals(EdgeLabel.has_observed_phenotype.toString())) {
     colour= EdgeColour.greenYellow.toString();
    }
  else if(edgeLbl.equals(EdgeLabel.control.toString())) {
     colour= EdgeColour.lightGrey.toString();
    }
  else if(edgeLbl.equals(EdgeLabel.control.toString())) {
     colour= EdgeColour.lightGrey.toString();
    }
  else if((edgeLbl.equals(EdgeLabel.has_variation.toString())) || 
          (edgeLbl.equals(EdgeLabel.has_target.toString()))) {
     colour= EdgeColour.navy.toString();
    }
  else if(edgeLbl.equals(EdgeLabel.activated_by.toString())) {
     colour= EdgeColour.magenta.toString();
    }
  else if(edgeLbl.equals(EdgeLabel.consumed_by.toString())) {
     colour= EdgeColour.brown.toString();
    }
  else if((edgeLbl.equals(EdgeLabel.produced_by.toString())) || 
          (edgeLbl.equals(EdgeLabel.is_involved_in.toString()))) {
     colour= EdgeColour.limeGreen.toString();
    }
  else if((edgeLbl.equals(EdgeLabel.catalyzed_by.toString())) || 
          (edgeLbl.equals(EdgeLabel.is_a.toString())) || 
          (edgeLbl.equals(EdgeLabel.part_of.toString())) || 
          (edgeLbl.equals(EdgeLabel.is_part_of.toString()))) {
     colour= EdgeColour.salmon.toString();
    }
  else if(edgeLbl.equals(EdgeLabel.part_of_catalyzing_class.toString())) {
     colour= EdgeColour.pink.toString();
    }
  else if(edgeLbl.equals(EdgeLabel.is_equivalent_to.toString())) {
     colour= EdgeColour.lightBlue.toString();
    }

  return colour;
 }

}
