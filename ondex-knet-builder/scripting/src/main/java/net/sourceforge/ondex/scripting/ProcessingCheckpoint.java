package net.sourceforge.ondex.scripting;

import java.util.Set;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;

public interface ProcessingCheckpoint {

	public abstract void processingStarted();

	public abstract void processingFinished();
	
	public abstract void setSelectedSubset(ONDEXGraph graph, Set<ONDEXConcept> cs, Set<ONDEXRelation> rs);

}