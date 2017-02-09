// this script file has depencies -- for now covered by the xml's library information
// a cleaner way would be something like this:
// var c = net.sourceforge.ondex.ovtk2.ui.console.OVTKScriptingInitialiser.getCommandLine();


/* contents
- load protein sequences from aspgd fasta flat file
- load protein sequences bioMart ensembl fungi
- NCBI Blast (EBI web service)
- bidirectional blast (local run)
*/


/****************************************************************************
	load protein sequences from aspgd fasta flat file
*****************************************************************************/
// c.executeJavaScriptFile("config/popupMenu/workingGraph.js");	
// c.executeJavaScriptFile("config/popupMenu/toolbox.js");		

function mainLoadAniAspgdFasta() {

	var concepts = getPickedConcepts()
	
	setupWorkingGraph();

	workingGraph.getMetaData().createDataSource("CADREGeneID", "CADREGeneID", "")
	applyPlugin(
		"net.sourceforge.ondex.parser.fasta.Parser",
		workingGraph,
		"data/importdata/fasta/A_nidulans_FGSC_A4_version_s06-m02-r11_orf_trans_all.fasta",
		"simple",
		227321,
		"Protein",
		"CADREGeneID",
		null,
		"AA",
		null, null);

	applyPlugin(
		"net.sourceforge.ondex.mapping.lowmemoryaccessionbased.Mapping",
		workingGraph,
		"Protein,Gene",
		null,
		null,
		"true",
		"is_translated_to",
		"false",
		"Gene",
		"CADREGeneID")
		
	//nameBasedMapping(workingGraph, "Gene", "Protein", "is_translated_to")
	
	finishWorkingGraph(null);	// "Protein" would do a merge
	doMergeAndLayout("Protein", concepts)

}




/****************************************************************************
	load protein sequences bioMart ensembl fungi
*****************************************************************************/
// c.executeJavaScriptFile("config/popupMenu/toolbox.js");		
// c.executeJavaScriptFile("config/popupMenu/monitor.js");	

importPackage(java.net)
importPackage(java.io)

function mainLoadAniBioMart(genes) {
	getActiveGraph().getMetaData().createDataSource("Alias", "Alias", "")
	downloadFasta(createGeneArray(genes))
	doMergeAndLayout("Protein", genes)
}

// returns null, if the resulting biomart url is too long
function createGeneArray(genes) {

	startMonitor("load protein sequences from BioMart (Ensembl Fungi Database)", genes.size()+1+1)
	
	var geneArray = Array()
	var i = genes.iterator()
	while(i.hasNext()) {
		var gene = i.next()
		var name = getConceptAccessionById(gene,"EnsemblGeneID")
		geneArray[name]=gene
	}
	
	// check URL length
	if(null==getBioMartURL(geneArray))
		return null
	
	return geneArray
}

function downloadFasta(geneArray) {
	
	// download fasta
	if(!nextState("connecting to BioMart...")) return
	var url = getBioMartURL(geneArray);
	if(url == null) {
		monitorComplete()
		alert("Request URL too long. Select less concepts.")
		return
	}
	var connection = new URL(url).openConnection();
	connection.setDoInput(true);
	var inStream = connection.getInputStream();
	var input = new BufferedReader(new InputStreamReader(inStream));
	
	// evaluate fasta
	var seq=""
	var c=null
	var line = "";
	while ((line = input.readLine()) != null) {
		
		if(line.substring(0,1)==">") {
			// add sequence of last protein concept
			if(c!=null)
				c.createAttribute("AA",seq,false)
			
			// create new protein with name and description
			seq=""
			var name = line.substring(1,line.indexOf("|"))
			var desc = line.substring(line.indexOf("|")+1)
			if(!nextState("streaming sequence for "+name)) return
			c = createConcept("",desc,"","Ensembl_BioMart","Protein")
			c.createConceptName(name,true)
			c.createConceptAccession(name, "EnsemblGeneID", false)
			c.createAttribute("TAXID","227321",false)
			if (typeof(geneArray[name]) == 'undefined'){
				alert(name+" was not in search result")
				//assumption at this point is that every gene concept has an EnsemblGeneID which is used for Biomart
			} else {
				createAndShowRelation(geneArray[name], c, "is_translated_to")
				var a = Array(); a[0]=c.getId()
				layoutNeighbours(geneArray[name].getId(), a)
			}
			
		} else {
			// put together sequence line by line
			seq+=line
		}
		
	}
	
	// add sequence of last protein concept
	if(c!=null)
		c.createAttribute("AA",seq,false)
	
	monitorComplete()
		
}

function getBioMartURL(geneArray) {
	var ids=""
 	for(gene in geneArray) {
 		ids+=","+gene
 	}
 	ids=ids.substring(1)
 	var url = 'http://www.biomart.org/biomart/martservice?query=<?xml%20version="1.0"%20encoding="UTF-8"?><!DOCTYPE%20Query><Query%20%20virtualSchemaName%20=%20"default"%20formatter%20=%20"FASTA"%20header%20=%20"0"%20uniqueRows%20=%20"0"%20count%20=%20""%20datasetConfigVersion%20=%20"0.7"%20><Dataset%20name%20=%20"anidulans_eg_gene"%20interface%20=%20"default"%20><Filter%20name%20=%20"ensembl_gene_id"%20value%20=%20"'+ids+'"/><Attribute%20name%20=%20"peptide"%20/><Attribute%20name%20=%20"ensembl_gene_id"%20/><Attribute%20name%20=%20"description"%20/></Dataset></Query>'
 	if(url.length>8000)
 		return null
 	return url
}

function an2ania(an) {
	var ania=an
	if(an.substring(0,2)=="AN") {
		var number = java.lang.Integer.valueOf(an.substring(2))
		ania = java.lang.String.format("ANIA_%05d", number)
	}
	return ania
}



/****************************************************************************
	NCBI Blast (EBI web service)
*****************************************************************************/
// c.executeJavaScriptFile("config/popupMenu/toolbox.js");	
// c.executeJavaScriptFile("config/popupMenu/monitor.js");	
// c.executeJavaScriptFile("config/popupMenu/xml.js");	
// c.executeJavaScriptFile("config/popupMenu/ebi.js");	

importPackage(Packages.uk.ac.ebi.webservices.axis1)

MAX_HITS = 10

function mainDoNcbiBlast(proteins) {
		
	// accession type
	getActiveGraph().getMetaData().createDataSource("BLAST", "BLAST", "")

	// attribute names (for relations)
	getActiveGraph().getMetaData().createAttributeName("identity","identity","","%",java.lang.Double,"Thing")
	getActiveGraph().getMetaData().createAttributeName("positives","positives","","%",java.lang.Double,"Thing")
	
	try {
		var client = NCBIBlastClient()
	} catch (e if e instanceof ReferenceError) {
		var message = e.name + "  " + e.message
		message+="\n\nThis error most likely occoured, because the NCBI BLAST library is missing."
		message+="\nDownload the NCBIBlast_Axis .jar client and required libraries and put them into the ondex lib/ path."
		message+="\nExclude the file xercesImpl.jar to prevent version conflicts. Ondex provides a newer version."
		message+="\nAt the time of writing, you could download all you need at"
		message+="\nhttp://www.ebi.ac.uk/Tools/webservices/services/sss/ncbi_blast_soap" 
		alert(message)
		return
	}
	
	downloadPNG = false // confirm("Download visualized results (png)?\n(Takes longer, not needed for bidirectional blast)")
	
	var params = Packages.uk.ac.ebi.webservices.axis1.stubs.ncbiblast.InputParameters()
	params.setStype("protein")
	params.setProgram("blastp")	// alternative: blastx
	params.setDatabase(Array("uniprotkb"))	// there are many alternatives...
	params.setAlignments(java.lang.Integer(MAX_HITS));
	runJobPool(client, params, "NCBI BLAST (EBI)",  proteins)
	
	// merge, visibility, layout
	if(!nextState("Mergin... ")) return
	doMergeAndLayout("Protein_blast", proteins)

	monitorComplete()

}

function evaluateFinishedJob(ipr, jobPool, index) {
	
	// problems with image link
	// WinVis: sub pathes do not work as href link in item info window
	// linux: X11 error with basePath=""
	var basePath = "data/ncbi-blast/"
	
	// job and corresponding protein
	var job = jobPool[index][0]
	var protein = jobPool[index][1]
		
	// read png
	var link=""
	if(downloadPNG) {
		var pathpng = getJobResults(ipr, jobPool, index, "visual-png", basePath)
		link = "visual-png see "+pathpng //"<a href='"+pathpng+"'>"+job+"</a>" // file://
	}
	
	// read xml results
	var pathxml = getJobResults(ipr, jobPool, index, "xml", basePath)
	var doc = createDocumentBuilderFactory().newDocumentBuilder().parse(pathxml).getDocumentElement()
	
	var nodeList = doc.getElementsByTagName("hit")
	var conceptIDList=Array()
	for(var i=0; i<MAX_HITS && i<nodeList.getLength(); i++) {
		
		// concept infos
		var item = nodeList.item(i)

		// ... OS=... (...) GN=... PE=...
		var anno = item.getAttribute("description")
		var ergebnis = anno.search(/Putative uncharacterized protein+/)
		if (ergebnis != -1) continue
		ergebnis = anno.search(/Predicted protein+/)
		if (ergebnis != -1) continue
		ergebnis = anno.search(/Uncharacterized protein+/)
		if (ergebnis != -1) continue
		var desc = anno+"\n"+link
		var os = anno.indexOf("OS=")
		var br = anno.substring(os).indexOf("("); br = br<0?99999:os+br
		var gn = anno.indexOf("GN=")
		var pe = anno.indexOf("PE=")
		if(os>=0 && gn>=0 && pe>=0) {
			anno = anno.substring(os+3,Math.min(gn,br))+"<br>"+
				anno.substring(gn+3,pe)
		}
		var c=createConcept("custom popup script",anno,desc,"NCBIBlast_EBI","Protein_blast")

		var id = item.getAttribute("id")
		c.createConceptAccession(id, "BLAST", false)

		var seq = new java.lang.String(item.getElementsByTagName("matchSeq").item(0).getTextContent()).replaceAll("-","")
		c.createAttribute("AA",seq,false)

		conceptIDList[i]=c.getId()
		
		// relation infos
		var r=createAndShowRelation(protein,c,"ncbi_blast")
		r.createAttribute("identity", java.lang.Double.valueOf(item.getElementsByTagName("identity").item(0).getTextContent()),false)
		r.createAttribute("positives",java.lang.Double.valueOf(item.getElementsByTagName("positives").item(0).getTextContent()),false)
	}
		
}

/****************************************************************************
	bidirectional blast (local run)
*****************************************************************************/
// c.executeJavaScriptFile("config/popupMenu/toolbox.js");	

function mainBidirectionalBlast(queryProteins) {
	
	tempdir = "data/temp/"
	java.io.File(tempdir).mkdir()
	
	// name to "Protein" mapping & creation of dbFasta
	var proteinMap=Array()
	var dbFasta=java.lang.StringBuffer()
	var j = getActiveGraph().getConceptsOfConceptClass("Protein").iterator()
	while(j.hasNext()) {
		var protein = j.next()
		var name = protein.getConceptName().getName()
		proteinMap[name] = protein
		dbFasta.append( ">"+name+"\n"+protein.getAttribute("AA").getValue()+"\n" )
	}

	// write "protein" fasta db
	fstream = new java.io.FileWriter(tempdir+"db.fasta")
	writer = new java.io.BufferedWriter(fstream)
	writer.write(dbFasta.toString())
	writer.close()
	
	// blast any selected "Protein_blast" against "Protein" data base
	if(!initBlast()) return
	var i = queryProteins.iterator()
	while(i.hasNext()) {
		var queryProtein = i.next()
		
		evaluateBlastTable(queryProtein, proteinMap, doBlast(queryProtein.getAttribute("AA").getValue()))
		
	}
	
	// no merge or layout: everything should be visible
	
}

function initBlast() {
	pathToBlast = "data/blast/" +
		java.lang.System.getProperty("os.name","").split(" ")[0].toLowerCase() +
		"/bin/"
	if(!java.io.File(pathToBlast).exists()) {
		alert("Please download and install blast binaries to "+pathToBlast)
		return false
	}
	// make runCommand available to the script
	cx=org.mozilla.javascript.ContextFactory.getGlobal().enterContext()
	g = new org.mozilla.javascript.tools.shell.Global(cx)

	g.runCommand(pathToBlast+"formatdb", "-p", "T", "-i", tempdir+"db.fasta")
	
	return true
}

function doBlast(querySequence) {
	
	sequenceToFile(querySequence)

	g.runCommand(pathToBlast+"formatdb", "-p", "T", "-i", tempdir+"query.fasta") 

	var o = {};
	o.output = "";
	g.runCommand(pathToBlast+"blastall", "-e", "0.001", "-p", "blastp",
		"-d", tempdir+"db.fasta",
		"-i", tempdir+"query.fasta", "-m", "8", o)
	out(o.output)	
	
	return o.output
	
}

function evaluateBlastTable(queryProtein, proteinMap, table) {
	
	var lines = java.lang.String(table).split("\n")
	
	for(hit in lines) {
		var line = java.lang.String(lines[hit]).split("\t")
		if(line[2]==null) return
		var name = line[1]
		var identity = java.lang.Double.valueOf(line[2])
		if( proteinMap[name]!=null && identity>=60 ) {
			var r = createAndShowRelation(queryProtein, proteinMap[name], "bidirectional_blast")
			if(r!=null)
				r.createAttribute("identity",identity,false)
		}
	}
	
}

function sequenceToFile(querySequence) {
	
	// Create file 
	fstream = new java.io.FileWriter(tempdir+"query.fasta")
	writer = new java.io.BufferedWriter(fstream)
	writer.write(">query\n")
	writer.write(querySequence)
	writer.close()
	
	
}

