/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sourceforge.ondex.parser.sbml2a;

/**
 *
 * @author Christian
 */
public enum TypeOfConcept {
    COMPARTMENT("cellComp"),
    SPECIES("molecule"),
    REACTION("process");

    private String id;

    private  TypeOfConcept(String i){
        id = i;
    }

    public String getId(){
        return id;
    }

    public String getDescription(){
        return id;
    }
}
