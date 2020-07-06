function setupWorkingGraph() {
	workingGraph = createGraph("workingGraph")
	workingConceptIds  = new Array()
	originalConceptIds = new Array()

	j=0
	i = getPickedConcepts().iterator()
	while(i.hasNext()) {
		var c = i.next()
		originalConceptIds[j] = c.getId()
		workingConceptIds [j] = cloneConcept(c, getActiveGraph(), workingGraph)
		j++
	}
}

function finishWorkingGraph(mergeConceptClasses) {
	// copy relevant nodes+edges back to original graph from workingGraph
	var clonedConceptIds = new Array()
	for(var i=0; i<workingConceptIds.length; i++) {

		var c = workingConceptIds[i];
		var concept = workingGraph.getConcept(c)
		if(concept.unwrap()==null) {
			out("ERROR: concept id not found in working Graph: "+c);
			continue;
		}
	
		// "relevant" concepts are those connected by any relation from a selected concept (workingConceptIds)
		var relations = workingGraph.getRelationsOfConcept(concept).iterator()
		clonedConceptIds[i] = new Array()
		j=0
		while(relations.hasNext()) {
			var g = getActiveGraph()
			r = relations.next()
			from = r.getFromConcept()
			to   = r.getToConcept()
			if(from.getId()!=c) { clonedConceptIds[i][j] = cloneConcept(from, workingGraph, getActiveGraph()); setVisibility(g.getConcept(clonedConceptIds[i][j]), true); }
			if(  to.getId()!=c) { clonedConceptIds[i][j] = cloneConcept(to,   workingGraph, getActiveGraph()); setVisibility(g.getConcept(clonedConceptIds[i][j]), true); }
			// cloneRelation(r,   workingGraph, getActiveGraph())

			// some plugins use native Types instead of scripted.Type -- wrap them
			var t
			if(!(r.getOfType() instanceof Type)) {
				t = new Type
				t.wrap(r.getOfType())
			}
			else
				t = r.getOfType()

			var newr = g.createRelation(
					g.getConcept(originalConceptIds[i]),
					g.getConcept(clonedConceptIds  [i][j]),
					t,
					r.getEvidence().unwrap());
			setVisibility(newr, true);
			j++
		}
	}

	// do a proper layout
	for(var i=0; i<workingConceptIds.length; i++)
		layoutNeighbours(originalConceptIds[i], clonedConceptIds[i])

	// merge equal concepts only if there are any of that type
	var ccFound=false
	if(mergeConceptClasses!=null) {
		var ccs = mergeConceptClasses.split("\n")
		for(var i = 0; i<ccs.length; i++)
			if(getActiveGraph().getMetaData().getConceptClass(ccs[i]).unwrap()!=null)
				ccFound=true
	}

	if(ccFound) {
		// equal concepts - relation
		applyPlugin(
				"net.sourceforge.ondex.mapping.lowmemoryaccessionbased.Mapping",
				getActiveGraph(),
				null,
				null,
				null,
				"true",
				null,
				"true",
				mergeConceptClasses,
				null);

		// var it = getActiveGraph().getRelationsOfRelationType("equ").iterator()
		// while(it.hasNext()) {
			// var r = it.next()
			// r.getFromConcept()
			// r.getToConcept()
		// }

		// collapse equal relation
		applyPlugin(
				"net.sourceforge.ondex.transformer.relationcollapser.Transformer",
				getActiveGraph(),
				"equ",
				null,
				null,
				"true",
				null,
				null);
	}

	// delete global variables
	workingGraph = null
	workingConceptIds  = null
	originalConceptIds = null
	
	refreshMetaGraphLabels()

}
