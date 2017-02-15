package net.sourceforge.ondex.parser.medline;

import net.sourceforge.ondex.parser.medline.data.Abstract;

public class Filter {
	
	public static boolean check(Abstract a, ImportSession is) {
		
		boolean OK = true;
		
		if (is.isImportList()) {
			if (!is.importListContains(String.valueOf(a.getId())))
				OK = false;
		}
		
		return OK;
	}
}
