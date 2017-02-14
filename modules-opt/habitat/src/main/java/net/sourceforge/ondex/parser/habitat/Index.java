package net.sourceforge.ondex.parser.habitat;

import java.util.HashMap;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;

public class Index extends HashMap<String,Integer> {
	
	private static final long serialVersionUID = -1355245622471884439L;

	private ONDEXGraph og;
	
	private DataSource dataSource;
	
	private ConceptClass cc;
	
	private EvidenceType et;
	
	public Index(ONDEXGraph og, DataSource dataSource, ConceptClass cc, EvidenceType et) {
		this.og = og;
		this.dataSource = dataSource;
		this.cc = cc;
		this.et = et;
	}
	
	public ONDEXConcept getOrCreate(String key) {
		Integer id = get(key);
		ONDEXConcept c;
		if (id == null) {
			c = og.getFactory().createConcept(key, dataSource, cc, et);
			put(key, c.getId());
		} else {
			c = og.getConcept(id);
		}
		return c;
	}
}
