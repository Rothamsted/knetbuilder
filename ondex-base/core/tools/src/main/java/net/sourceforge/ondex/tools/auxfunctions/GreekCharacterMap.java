package net.sourceforge.ondex.tools.auxfunctions;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class GreekCharacterMap {
	private GreekCharacterMap(){}
	
	public static final Map<String, String> map = new HashMap<String, String>();
	public static final Map<String, String> map1 = new HashMap<String, String>();
	
	static {
		map.put("&alpha;","\u03B1");
		map.put("&beta;","\u03B2");
		map.put("&gamma;","\u03B3");
		map.put("&delta;","\u03B4");
		map.put("&epsilon;","\u03B5");
		map.put("&zeta;","\u03B6");
		map.put("&eta;","\u03B7");
		map.put("&theta;","\u03B8");
		map.put("&iota;","\u03B9");
		map.put("&kappa;","\u03BA");
		map.put("&lambda;","\u03BB");
		map.put("&mu;","\u03BC");
		map.put("&nu;","\u03BD");
		map.put("&xi;","\u03BE");
		map.put("&omicron;","\u03BF");
		map.put("&pi;","\u03C0");
		map.put("&rho;","\u03C1");
		map.put("&sigma;","\u03C3");
		map.put("&tau;","\u03C4");
		map.put("&upsilon;","\u03C5");
		map.put("&phi;","\u03C6");
		map.put("&chi;","\u03C7");
		map.put("&psi;","\u03C8");
		map.put("&omega;","\u03C9");
		map.put("&Alpha;","\u0391");
		map.put("&Beta;","\u0392");
		map.put("&Gamma;","\u0393");
		map.put("&Delta;","\u0394");
		map.put("&Epsilon;","\u0395");
		map.put("&Zeta;","\u0396");
		map.put("&Eta;","\u0397");
		map.put("&Theta;","\u0398");
		map.put("&Iota;","\u0399");
		map.put("&Kappa;","\u039A");
		map.put("&Lambda;","\u039B");
		map.put("&Mu;","\u039C");
		map.put("&Nu;","\u039D");
		map.put("&Xi;","\u039E");
		map.put("&Omicron;","\u039F");
		map.put("&Pi;","\u03A0");
		map.put("&Rho;","\u03A1");
		map.put("&Sigma;","\u03A3");
		map.put("&Tau;","\u03A4");
		map.put("&Upsilon;","\u03A5");
		map.put("&Phi;","\u03A6");
		map.put("&Chi;","\u03A7");
		map.put("&Psi;","\u03A8");
		map.put("&Omega;","\u03A9");
		
		map1.put("&alpha;","alpha");
		map1.put("&beta;","beta");
		map1.put("&gamma;","gamma");
		map1.put("&delta;","delta");
		map1.put("&epsilon;","epsilon");
		map1.put("&zeta;","zeta");
		map1.put("&eta;","eta");
		map1.put("&theta;","theta");
		map1.put("&iota;","iota");
		map1.put("&kappa;","kappa");
		map1.put("&lambda;","lambda");
		map1.put("&mu;","mu");
		map1.put("&nu;","nu");
		map1.put("&xi;","xi");
		map1.put("&omicron;","omicron");
		map1.put("&pi;","pi");
		map1.put("&rho;","rho");
		map1.put("&sigma;","sigma");
		map1.put("&tau;","tau");
		map1.put("&upsilon;","upsilon");
		map1.put("&phi;","phi");
		map1.put("&chi;","chi");
		map1.put("&psi;","psi");
		map1.put("&omega;","omega");
		map1.put("&Alpha;","Alpha");
		map1.put("&Beta;","Beta");
		map1.put("&Gamma;","Gamma");
		map1.put("&Delta;","Delta");
		map1.put("&Epsilon;","Epsilon");
		map1.put("&Zeta;","Zeta");
		map1.put("&Eta;","Eta");
		map1.put("&Theta;","Theta");
		map1.put("&Iota;","Iota");
		map1.put("&Kappa;","Kappa");
		map1.put("&Lambda;","Lambda");
		map1.put("&Nu;","Nu");
		map1.put("&Xi;","Xi");
		map1.put("&Omicron;","Omicron");
		map1.put("&Pi;","Pi");
		map1.put("&Rho;","Rho");
		map1.put("&Sigma;","Sigma");
		map1.put("&Tau;","Tau");
		map1.put("&Upsilon;","Upsilon");
		map1.put("&Phi;","Phi");
		map1.put("&Chi;","Chi");
		map1.put("&Psi;","Psi");
		map1.put("&Omega;","Omega");
	}
	
	/**
	 * Converts a string with HTML Greek letter tags to UTF-8 complaint encoding
	 * @param s
	 * @return
	 */
	public static final String convertToUnicode(String s){
		for(Entry<String, String> ent: map.entrySet()){
			s = s.replaceAll(ent.getKey(), ent.getValue());
		}
		return s;
	}
	
	public static final String convertToText(String s){
		for(Entry<String, String> ent: map1.entrySet()){
			s = s.replaceAll(ent.getKey(), ent.getValue());
		}
		return s;
	}
	
	/**
	 * Converts a string with UTF-8 Greek letters to tags HTML tags
	 * @param s
	 * @return
	 */
	public static final String convertToHtml(String s){
		for(Entry<String, String> ent: map.entrySet()){
			s = s.replaceAll(ent.getValue(), ent.getKey());
		}
		return s;
	}
}
