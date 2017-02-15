package net.sourceforge.ondex.scripting;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;

import java.util.Set;

public interface ProcessingCheckpoint {

	public abstract void processingStarted();

	public abstract void processingFinished();
	
	public abstract void setSelectedSubset(ONDEXGraph graph, Set<ONDEXConcept> cs, Set<ONDEXRelation> rs);

}