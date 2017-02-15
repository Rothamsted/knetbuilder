package net.sourceforge.ondex.parser.genericobo.chebi;

//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.File;

//java.util.StringTokenizer;

import net.sourceforge.ondex.parser.genericobo.OboConcept;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

//just used to determine if a concept is a compound.

/**
 * @author hoekmanb
 */
public class Compound {

    public static void main(final String[] args) {
        boolean test;
        String formula = "C9H10NO3";
        test = isFormula(formula);
        System.out.println("test Fomula: " + test);
    }

    public static boolean isCompound(OboConcept obo) {
        boolean isCompound = false;

        for (int j = 0; j < obo.getRefs().size(); j++) {
            String xref = obo.getRefs().get(j);

            if (xref.contains("Registry Number") || xref.contains("KEGG")) {
                isCompound = true;
            }
        }
        if (isCompound == false) {
            // synonym type is ignored in this loop!
            for (Set<String> synTypeSet : obo.getSynonyms().values()) {
                for (String syn_name : synTypeSet) {
                    if (isCompound == false) {
                        isCompound = isInChI(syn_name);
                    }
                    if (isCompound == false) {
                        isCompound = isFormula(syn_name);
                    }
                }
            }

        }
        if (isCompound == false) {
            String name = obo.getName();
            isCompound = isInChI(name);
            if (isCompound == false) {
                isCompound = isFormula(name);
            }
        }

        return isCompound;
    }

    public static boolean isInChI(String line) {
        boolean isInChI = false;
        if (line.startsWith("InChI=")) {
            isInChI = true;
        }
        return isInChI;
    }

    public static boolean isFormula(String line) {
        boolean isFormula = false;

        String formulaPattern1 = "C\\d+H\\d+O\\d*"; // C5H2O2
        String formulaPattern2 = "C\\d+H\\d+N\\d*"; // C5H2N2

        Pattern fP1 = Pattern.compile(formulaPattern1);
        Pattern fP2 = Pattern.compile(formulaPattern2);

        if (fP1.matcher(line).lookingAt()) {
            isFormula = true;
        }

        if (fP2.matcher(line).lookingAt()) {
            isFormula = true;
        }

        if (line.toLowerCase().contains("r")) // R -> r : depicts rest group
        // which can be anything.
        {
            isFormula = false;
        }

        return isFormula;
    }

    /*
      * DOES NOT HELP public static Vector<String> loadListOfCompounds (String
      * filePath) throws Exception { Vector<String> compounds = new Vector<String>();
      *
      * String filename = filePath + File.separator + "compounds.csv";
      * BufferedReader in = new BufferedReader( new FileReader(filename) );
      *
      * String inputLine = in.readLine();
      *
      * while (inputLine != null) { String[] st = inputLine.split("\t");
      * if(st.length > 3) { if(!(compounds.contains(st[2].trim()))) {
      * if(st[1].toLowerCase().equals("c")) //check compound
      * {compounds.add(st[2].trim());} // } }
      *
      * inputLine = in.readLine(); } in.close(); return compounds; }
      */

    public static boolean isCompoundBasedOnRelations(OboConcept obo) {
        boolean isCompound = false;
        for (int j = 0; j < obo.getRelations().size(); j++) {
            List<String> relation = obo.getRelations().get(j); // type_of_relation,
            // accession
            String rel_type_orig = relation.get(0);

            if (rel_type_orig.contains("is_conjugate_base_of")
                    || rel_type_orig.contains("is_conjugate_acid_of")
                    || rel_type_orig.contains("is_enantiomer_of")
                    || rel_type_orig.contains("is_tautomer_of")) {
                isCompound = true;
            }
        }
        return isCompound;
    }
}
