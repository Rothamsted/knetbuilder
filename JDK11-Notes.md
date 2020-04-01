  * OK Continue with ondex-base/core/tools
  * OK Then ondex-base/core/lucene

  * OK workflow-api and extensions uses the old com.sun.javadoc doclet API.
		New Doclet written, needs to be verified with the rest (eg, WF distro,
		plugin descriptors in the integrator)
 
  * OK the entire core

  * OK ondex-knet-builder/modules/oxl
    * to check what's next

  * OK Doclet: the fetch of tags like @author gets the whole '@author someone',
    * remove the tag.

  * OK modules/json, cyjs-json
    * Have dependencies with specified versions
    * Continue with dependency cleaning, from top-pom/maven-source-plugin
    * Go to the modules done so far and check artifact deps, if they need to be under
      depMgmt etc.
  
  * scripting is probably to be removed
    * OK check knet-builder/scripting
    	* OK Old Jena?
    * ~~then continue from modules/js-plugin~~ 
    	* Depends on `net.sourceforge.ondex.apps:ovtk2`. Do it later
    * later, we need to check ondex-desktop/scripting-commandline, which depends on scripting
	
	* OK owl-parser: see why the tests fail
		
	* OK launcher
	* OK opt modules
	* OK integrator
	* OK ondex-mini
	* OK installer
	* OK mini-integration-tests
	
	
## Still pending
  * `net.sourceforge.ondex.ovtk2.ui.toolbars.MenuGraphSearchBox.fireActionPerformed()`, needs 
  `getModifiersEx()`, keeping the deprecated method until tests.
  	* The same for some Jung-dependant classes, search for `BUTTON1_MASK` or `BUTTON3_MASK`
   
   
## Package Order
```
[INFO] Reactor Summary for ONDEX POM for Full Build 4.0-SNAPSHOT:
[INFO]
[INFO] ONDEX POM for Full Build ........................... SUCCESS [  0.122 s]
[INFO] ONDEX Base components .............................. SUCCESS [  0.004 s]
[INFO] datadir ............................................ SUCCESS [  0.004 s]
[INFO] core ............................................... SUCCESS [  0.004 s]
[INFO] ONDEX/Core/api ..................................... SUCCESS [  0.022 s]
[INFO] marshal ............................................ SUCCESS [  0.009 s]
[INFO] ONDEX/core/base .................................... SUCCESS [  0.016 s]
[INFO] memory ............................................. SUCCESS [  0.012 s]
[INFO] algorithms ......................................... SUCCESS [  0.026 s]
[INFO] tools .............................................. SUCCESS [  0.029 s]
[INFO] lucene ............................................. SUCCESS [  0.011 s]
[INFO] workflow-api ....................................... SUCCESS [  0.021 s]
[INFO] workflow-base ...................................... SUCCESS [  0.018 s]
[INFO] workflow-component-descriptor ...................... SUCCESS [  0.006 s]
[INFO] parser-api ......................................... SUCCESS [  0.016 s]
[INFO] ONDEX Optional Components .......................... SUCCESS [  0.004 s]
[INFO] ONDEX core/optional Components ..................... SUCCESS [  0.003 s]
[INFO] ONDEX/core/memory-dist ............................. SUCCESS [  0.006 s]
[INFO] sql ................................................ SUCCESS [  0.011 s]
[INFO] ONDEX Optional Modules ............................. SUCCESS [  0.080 s]
[INFO] arabidopsis ........................................ SUCCESS [  0.021 s]
[INFO] Aries .............................................. SUCCESS [  0.024 s]
[INFO] biobase ............................................ SUCCESS [  0.023 s]
[INFO] blast .............................................. SUCCESS [  0.024 s]
[INFO] carbs .............................................. SUCCESS [  0.018 s]
[INFO] K-Net Builder ...................................... SUCCESS [  0.003 s]
[INFO] modules ............................................ SUCCESS [  0.017 s]
[INFO] oxl ................................................ SUCCESS [  0.026 s]
[INFO] clustering ......................................... SUCCESS [  0.029 s]
[INFO] decypher ........................................... SUCCESS [  0.023 s]
[INFO] enzymatics ......................................... SUCCESS [  0.017 s]
[INFO] experimental ....................................... SUCCESS [  0.008 s]
[INFO] grain databases .................................... SUCCESS [  0.004 s]
[INFO] GraphAlgo .......................................... SUCCESS [  0.005 s]
[INFO] validator .......................................... SUCCESS [  0.004 s]
[INFO] tab-tools .......................................... SUCCESS [  0.005 s]
[INFO] graph-query ........................................ SUCCESS [  0.012 s]
[INFO] gsk ................................................ SUCCESS [  0.005 s]
[INFO] habitat ............................................ SUCCESS [  0.004 s]
[INFO] iah ................................................ SUCCESS [  0.004 s]
[INFO] interaction ........................................ SUCCESS [  0.004 s]
[INFO] kegg ............................................... SUCCESS [  0.004 s]
[INFO] legacy ............................................. SUCCESS [  0.004 s]
[INFO] taxonomy ........................................... SUCCESS [  0.004 s]
[INFO] plants ............................................. SUCCESS [  0.005 s]
[INFO] poplar ............................................. SUCCESS [  0.003 s]
[INFO] prolog ............................................. SUCCESS [  0.005 s]
[INFO] structure .......................................... SUCCESS [  0.004 s]
[INFO] rdf IO ............................................. SUCCESS [  0.005 s]
[INFO] Relevance .......................................... SUCCESS [  0.004 s]
[INFO] tab ................................................ SUCCESS [  0.011 s]
[INFO] ONDEX Scripting .................................... SUCCESS [  0.004 s]
[INFO] ONDEX apps ......................................... SUCCESS [  0.003 s]
[INFO] scripting-commandline .............................. SUCCESS [  0.003 s]
[INFO] generic, plugins/components ........................ SUCCESS [  0.004 s]
[INFO] sequence ........................................... SUCCESS [  0.004 s]
[INFO] launcher ........................................... SUCCESS [  0.014 s]
[INFO] ONDEX OVTK2 ........................................ SUCCESS [  0.004 s]
[INFO] ovtk2-modules ...................................... SUCCESS [  0.003 s]
[INFO] ovtk2-default ...................................... SUCCESS [  0.004 s]
[INFO] ovtk2-poplar ....................................... SUCCESS [  0.004 s]
[INFO] ovtk2-experimental ................................. SUCCESS [  0.004 s]
[INFO] doc ................................................ SUCCESS [  0.003 s]
[INFO] cyc pathway databases .............................. SUCCESS [  0.012 s]
[INFO] cyjs_json .......................................... SUCCESS [  0.004 s]
[INFO] GeneOntology ....................................... SUCCESS [  0.004 s]
[INFO] json exporter ...................................... SUCCESS [  0.003 s]
[INFO] phenotypes ......................................... SUCCESS [  0.004 s]
[INFO] Tabular Parser 2 ................................... SUCCESS [  0.003 s]
[INFO] ONDEX OWL Parser ................................... SUCCESS [  0.004 s]
[INFO] textmining ......................................... SUCCESS [  0.003 s]
[INFO] ONDEX RDF Common ................................... SUCCESS [  0.004 s]
[INFO] Ondex RDF Exporter ................................. SUCCESS [  0.013 s]
[INFO] Ondex RDF Exporter CLI ............................. SUCCESS [  0.003 s]
[INFO] ONDEX RDF-to-OXL Converter ......................... SUCCESS [  0.004 s]
[INFO] ONDEX Neo4J Exporter ............................... SUCCESS [  0.018 s]
[INFO] ONDEX + Integrator/Worflow Tool .................... SUCCESS [  0.004 s]
[INFO] ondex-mini ......................................... SUCCESS [  0.003 s]
[INFO] ONDEX Installer .................................... SUCCESS [  0.002 s]
[INFO] mini-integration-tests ............................. SUCCESS [  0.004 s]
```	
    