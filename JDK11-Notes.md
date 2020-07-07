# Notes about migration to Java 11
	
## Still pending

  * `net.sourceforge.ondex.ovtk2.ui.toolbars.MenuGraphSearchBox.fireActionPerformed()`, needs 
  `getModifiersEx()`, keeping the deprecated method until tests.
  	* The same for some Jung-dependant classes, search for `BUTTON1_MASK` or `BUTTON3_MASK`

  * The [workflow-api](ondex-base/core/workflow-api/pom.xml) still depends on an old version of
  clapper ocutils, see the notes in the POM about replacements to do.
