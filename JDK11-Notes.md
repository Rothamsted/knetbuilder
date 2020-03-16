  * OK Continue with ondex-base/core/tools
  * OK Then ondex-base/core/lucene

  * OK workflow-api and extensions uses the old com.sun.javadoc doclet API.
		New Doclet written, needs to be verified with the rest (eg, WF distro,
		plugin descriptors in the integrator)
 
  * OK the entire core

  * OK ondex-knet-builder/modules/oxl
    * to check what's next

  * OK Doclet: the fetch of tags like @author gets the whole '@author someone',
    remove the tag.

  * OK modules/json, cyjs-json
    * Have dependencies with specified versions
    * Continue with dependency cleaning, from top-pom/maven-source-plugin
    * Go to the modules done so far and check artifact deps, if they need to be under
      depMgmt etc.
  
  * modules/scripting is probably to be removed
    * OK check knet-builder/scripting
    	* OK Old Jena?
    * ~~then continue from modules/js-plugin~~ Depends on `net.sourceforge.ondex.apps:ovtk2`.
    * later, we need to check ondex-desktop/scripting-commandline, which depends on scripting
	
	* OK owl-parser: see why the tests fail
		
    