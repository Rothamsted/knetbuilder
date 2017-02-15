package net.sourceforge.ondex.mapping.tmbased;

import net.sourceforge.ondex.core.searchable.LuceneEnv;

import org.apache.lucene.analysis.StopAnalyzer;

import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * This Class provides various utilities to process text
 * for text mining applications
 *
 * @author Keywan
 */
public class TextProcessing {

    private final static Pattern patternNonWordChars = Pattern.compile("[\\W_]");
    private final static Pattern doubleSpaceChars = Pattern.compile("\\s+");


    //a period followed by any number of whitespaces and then a capital letter
    private final static Pattern sentenceSplit = Pattern.compile("\\.\\s+(?=[A-Z])");

    //not significant words in GO terms
    private final static Pattern weakGOwords = Pattern.compile("activity|protein");


    /**
     * Lowercasing and Replacing of non-words and double-spaces
     *
     * @param text original text
     * @return text cleaned up
     */
    public static String stripText(String text) {

        // replace all non-word characters with space
        text = patternNonWordChars.matcher(text).replaceAll(" ");

        // replace double spaces by single ones
        text = doubleSpaceChars.matcher(text).replaceAll(" ");

        // trim and lower case
        text = text.trim().toLowerCase();

        return text;
    }

    /**
     * Deletes all STOP words (see Lucene StopAnalyzer.ENGLISH_STOP_WORDS) from a given string
     *
     * @param text
     * @return string without any stopwords
     */
    public static String removeStopWords(String text) {
        for (Object stopWord : StopAnalyzer.ENGLISH_STOP_WORDS_SET) {
            if (text.contains(stopWord.toString())) {
                text = text.replaceAll("\\b" + stopWord + "\\b", "");

            }
        }
        text = stripText(text);

        return text;
    }

    /**
     * remove not significant words in GO terms
     *
     * @param text
     * @return
     */
    public static String removeWeakWords(String text) {

        text = weakGOwords.matcher(text).replaceAll("");

        text = stripText(text);

        return text;
    }

    /**
     * Finds sentences in a text that are relevant to a query.
     * Also called evidence sentences.
     *
     * @param text  is split into sentences
     * @param query should occur in the sentences
     * @return HashSet with evidence sentences
     */
    public static HashSet<String> searchEvidenceSentence(String text, String query) {
        //split abstract into sentences
        query = LuceneEnv.stripText(query);
        query = TextProcessing.removeStopWords(query);
        String sentences[] = TextProcessing.sentenceSplit.split(text);
        HashSet<String> evidences = new HashSet<String>();
        for (String sentence : sentences) {
            String originalSen = sentence;
            sentence = TextProcessing.stripText(sentence);
            sentence = TextProcessing.removeStopWords(sentence);
            //find all sentences of abstract that match the pattern
            if (sentence.contains(query)) {
                //add a point to the end of sentence if not there yet
                if (!originalSen.trim().endsWith(".")) {
                    originalSen = originalSen + ".";
                    //System.out.println("QUERY:"+query+" TEXT:"+originalSen);
                }
                evidences.add(originalSen);
			}
		}
		return evidences;
	}


}
