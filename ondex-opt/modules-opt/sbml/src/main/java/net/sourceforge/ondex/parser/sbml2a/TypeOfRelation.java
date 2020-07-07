/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sourceforge.ondex.parser.sbml2a;

/**
 *
 * @author Christian
 */
public enum TypeOfRelation {
    REACTANT("taken","consumed_by"),
    PRODUCT("modifies","produced_by"),
    MODIFIER("given","modifies");

    private String modifier;

    private String id;

    private  TypeOfRelation(String m, String i){
        modifier = m;
        id = i;
    }

    public String getModifier(){
        return modifier;
    }
    
    public String getId(){
        return id;
    }

    public String getDescription(){
        return id;
    }
}
