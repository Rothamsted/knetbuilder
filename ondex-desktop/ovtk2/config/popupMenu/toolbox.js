// create relation between two concepts
function createRelation(graph, g1, g2, relationName) {

	var r=graph.getRelation(g1,g2,relationName)
	if(r.unwrap()!=null){
		setVisibility(r, true)
		return
	}

	var et = createEvidenceType("NR", "not recorded", "desc")
	
	var r = graph.createRelation(g1,g2,relationName,et)

}

// create relation between two concepts, and (at least) make them visible
function createAndShowRelation(c1, c2, relationName) {
	setVisibility(c1, true)
	setVisibility(c2, true)
	createSimpleRelationType(relationName)
	
	var r=getActiveGraph().getRelation(c1,c2,relationName)
	if(r.unwrap()!=null) {
		setVisibility(r, true)
		return
	}

	var et = createEvidenceType("NR", "not recorded", "desc")
	var r = getActiveGraph().createRelation(c1,c2,relationName,et)
	setVisibility(r, true)
	return r
}

function createSimpleRelationType(relationName) {
	createSimpleRelationTypeInGraph(getActiveGraph(), relationName)
}

function createSimpleRelationTypeInGraph(graph, relationName) {
	if(null==graph.getMetaData().getRelationType(relationName).unwrap()) {
		// id, full name, description, inverse name, anti, refl, symm, trans, spec.Of
		graph.getMetaData().createRelationType(relationName,relationName,"","",false,false,false,false,"Thing")
	}
}

function getConceptAccessionById(concept, id) {
	var accs = concept.getConceptAccessions()
	var i = accs.iterator()
	while(i.hasNext()) {
		var acc = i.next()
		if(acc.getElementOf().getId()==id)
			return acc.getAccession()
	}
	return null
}

// cc1,cc2 are ConceptClass strings
function nameBasedMapping(graph, strCC1, strCC2, relationType) {
	var cc1 = getActiveGraph().getMetaData().getConceptClass(strCC1)
	var cc2 = getActiveGraph().getMetaData().getConceptClass(strCC2)
	if(cc1.unwrap()==null || cc2.unwrap()==null) return
	var allGenes = graph.getConceptsOfConceptClass(cc1)
	var allProteins = graph.getConceptsOfConceptClass(cc2)

	// name to protein mapping
	var proteinMap=Array()
	var j = allProteins.iterator()
	while(j.hasNext()) {
		var protein = j.next()
		var name = protein.getConceptName().getName()
		proteinMap[name] = protein
	}

	// use mapping to find same names quickly
	createSimpleRelationTypeInGraph(graph, relationType)
	var i = allGenes.iterator()
	while(i.hasNext()) {
		var gene = i.next();
		var protein = proteinMap[gene.getConceptName().getName()]
		// protein with same concept name found?
		if( protein != null ) {
			createRelation(graph, gene, protein, relationType);
		}
	}

}

function createConcept(parserID, annotation, desc, dataSource, conceptClass) {
	
	// create data source
	var cv = getActiveGraph().getMetaData().getDataSource(dataSource)
	if(cv.unwrap()==null)
		cv=getActiveGraph().getMetaData().createDataSource(dataSource, dataSource,"")
                          
	// evidence type
	var et = createEvidenceType("NR", "not recorded", "desc")
	
	// concept class
	var cc = getActiveGraph().getMetaData().getConceptClass(conceptClass)
	if(cc.unwrap()==null)
		cc=getActiveGraph().getMetaData().createConceptClass(conceptClass,conceptClass,"", "Thing")

	return getActiveGraph().createConcept(parserID, annotation, desc, cv, cc, et)
	
}

function createEvidenceType(id, name, desc) {
	var list = java.util.ArrayList()
	var et=getActiveGraph().getMetaData().getEvidenceType(id)
	if(et.unwrap()==null)
		et=getActiveGraph().getMetaData().createEvidenceType(id,name,desc)
	list.add(et.unwrap())
	return list
}

// Parameter types: String, View
function doMergeAndLayout(cc, concepts) {

	doMerge(cc)
	
	// repair visibility and position of concepts and relations destroyed by the merge plugin
	var i = concepts.iterator()
	while(i.hasNext()) {
		var concept = i.next()

		setVisibilityOfAssociatedRelationsToCC(concept, cc, true)

		var associated = getAssociatedConceptsOfType(concept, false, cc)
		for each (var asso in associated) {
			var j = getActiveGraph().getRelationsOfConcept(asso).iterator()
			while(j.hasNext()) {
				var r = j.next()
				if(isVisible(getAssociatedConcept(asso,r)))
					setVisibility(r, true)
			}
		}
		
		// proper layout
		layoutNeighbours(concept.getId(), getAssociatedConceptIDsOfType(concept, false, cc))
	}

}

// merge publication concepts
function doMerge(cc) {

	doLink(cc,"equ")

	// collapse equal relation
	applyPlugin(
		"net.sourceforge.ondex.transformer.relationcollapser.Transformer",
		getActiveGraph(),
		"equ",
		cc+","+cc,
		null,
		"true",
		null,
		null);
	
}

// relationName = null -> "equ"
function doLink(cc, relationName) {
	// equal concepts - relation
	applyPlugin(
		"net.sourceforge.ondex.mapping.lowmemoryaccessionbased.Mapping",
		getActiveGraph(),
		null,
		null,
		null,
		"true",
		relationName,
		"true",
		cc,
		null);
}

// returns a list of concept IDs associated to the given concept by some relation
function getAssociatedConceptIDs(concept, onlyVisible) {
	return getAssociatedConceptIDsOfType(concept, onlyVisible, null)
}

function getAssociatedConceptIDsOfType(concept, onlyVisible, cc) {
	var conceptArray = getAssociatedConceptsOfType(concept, onlyVisible, cc)
	var idArray = Array()
	var i=0
	for each (var c in conceptArray)
		idArray[i++] = c.getId()
	return idArray
}
	
// returns a list of concepts associated to the given concept by some relation
function getAssociatedConcepts(concept, onlyVisible) {
	var relations = getAssociatedRelationsToCC(concept, onlyVisible, null)
	
	var conceptArray = Array()
	var j = 0
	for(var i in relations) {
		conceptArray[j++] = getAssociatedConcept(concept, relations[i])
	}
	
	return conceptArray
}

function getAssociatedRelationsToCC(concept, onlyVisible, cc) {
	var relationArray = Array()
	var j = 0
	var i = getActiveGraph().getRelationsOfConcept(concept).iterator()
	while(i.hasNext()) {
		var r = i.next()
		if(!onlyVisible || onlyVisible && isVisible(r))
			if(cc==null || getAssociatedConcept(concept, r).getOfType().getId()==cc)
				relationArray[j++] = r
	}
	return relationArray	
}

// returns the concept on the othe side of a relation
function getAssociatedConcept(concept, relation) {
	if(relation.getFromConcept().getId()!=concept.getId()) return relation.getFromConcept()
	if(relation.getToConcept().getId()!=concept.getId()) return relation.getToConcept()
}

function getAssociatedConceptsOfType(concept, onlyVisible, cc) {
	var conceptArray = getAssociatedConcepts(concept, onlyVisible)
	for(var i in conceptArray) {
		if(cc!=null && conceptArray[i].getOfType().getId()!=cc)
			delete conceptArray[i]
	}
	return conceptArray
}

function setVisibilityOfAssociatedRelationsToCC(concept, cc, visibility) {
	var relations = getAssociatedRelationsToCC(concept, false, cc)
	for(var i in relations) {
		setVisibility(getAssociatedConcept(concept, relations[i]), visibility)
		setVisibility(relations[i], visibility)
	}
}
