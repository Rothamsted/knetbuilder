# Notes about migration to Java 17

## Still pending
  * Neo4 isn't compatible with J17, this prevents a number of tests from
    working, at the moment Neo4j-related components were disabled from
    Ondex. 

  * The desktop app shows [this error][10] sometimes. It doesn't seem to affect
    functionality, likely it will go away with the next JDK 17.

[10]: https://bugs.openjdk.org/browse/JDK-8283347


# Notes about migration to Java 11
	
## Still pending

  * `net.sourceforge.ondex.ovtk2.ui.toolbars.MenuGraphSearchBox.fireActionPerformed()`, needs 
  `getModifiersEx()`, keeping the deprecated method until tests.
  	* The same for some Jung-dependant classes, search for `BUTTON1_MASK` or `BUTTON3_MASK`

  * The [workflow-api](ondex-base/core/workflow-api/pom.xml) still depends on an old version of
  clapper ocutils, see the notes in the POM about replacements to do.
