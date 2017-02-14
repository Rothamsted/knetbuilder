package net.sourceforge.ondex.emolecules.utils;

import java.util.BitSet;

/**
 *
 * @author grzebyta
 */
public class BitSetUtils {

    /**
     * Convert a BitSet to String.
     *
     * @param bits bitset representation
     * @return
     */
    public static String toString(BitSet bits) {
        if (bits.isEmpty()) {
            return "0";
        }
        
        StringBuilder sb = new StringBuilder();
        
        for (int i=bits.length()-1; i>=0; i--) {
            if (bits.get(i)) {
                sb.append("1");
            } else {
                sb.append("0");
            }
        }
        
        return sb.toString();
    }

    /**
     * Convert String to BitSet.
     * 
     * @param b bytes array
     * @return 
     */
    public static BitSet fromString(String in) {
        if (in.isEmpty()) {
            return new BitSet();
        }
        StringBuilder inBuilder = new StringBuilder(in);
        String reversedIn = 
                inBuilder.reverse().toString();
        
        BitSet bits = new BitSet(in.length());
        
        for (int i=0; i<in.length(); i++) {
            if (reversedIn.charAt(i) == '1') {
                bits.set(i);
            }
        }
        
        return bits;
    }
}
