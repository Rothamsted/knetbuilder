# Ondex-KnetBuilder

[Ondex](http://www.ondex.org/) is a framework for text mining, data integration and data analysis. 


## Requirements

 - At least Java 11. Note that we have migrated to this recently and **Ondex doesn't work with Java 1.8 anymore**. 

## Downloads

 - See the [Downloads](https://github.com/Rothamsted/ondex-knet-builder/wiki/Downloads) Page

## Execution

 0. Create a new folder e.g. `mkdir knetbuilder-folder`

 1. Download the latest [KnetBuilder release](https://github.com/Rothamsted/KnetBuilder/releases) and unzip in knetbuilder-folder

 2. Download the [tutorial files](https://rrescloud.rothamsted.ac.uk/index.php/s/H6sl0RIT9CoMaUI) and unzip in knetbuilder-folder

 3. Check the folder structure looks like this:
 
 ```bash
 -bash-4.1$ ls knetbuilder-folder/
 ondex-mini-1.2
 tutorial_files
 ```

 4. Go to the downloaded ondex-mini folder and run a workflow:
 
 ```bash
 cd ondex-mini-1.2
 ./runme.sh ../tutorial_files/Workflow/HumanDiseaseKNET.xml
 ```

 5. The KnetBuilder workflow integrates several datasets and creates a OXL file `tutorial_files/NeuroDiseaseKnet.oxl`

 6. The OXL file can be opened in the [Ondex Desktop](http://www.ondex.org) application or explored in the 
 [KnetMiner](http://knetminer.rothamsted.ac.uk/HumanDisease/) web application.
 
 
## Documentation
 
 We keep most of our documentation on this repository's 
 [wiki](https://github.com/Rothamsted/ondex-knet-builder/wiki) (note that there are many links on the right column in 
 that page).
 



## Code Repository Organisation

This repository is a rather big code base. The main submodules that it contains are described below.


### ondex-full

This is the top-level POM, defining many common things shared by Ondex code.

### ondex-base
The base code, used to build everything else.

### ondex-knet-builder
What we use to build projects like [KNetMiner](http://knetminer.rothamsted.ac.uk).

### ondex-desktop
Components for the desktop/GUI applications.

### ondex-opt
Optional components. **NOTE**: these are still linked by the components above (so, not so optional), but we usually 
build them separately. If you build the whole ONDEX from this top repository, these optional modules will be built 
from (the local clone of) this child repository, if you build an ONDEX component independently (e.g., by cloning 
`ondex-base` and building from its local copy), some of these 'optional' components are still downloaded from our 
Maven repositories as dependencies. This is because it is currently hard to modularise things further (we would need 
to inspect the Java code).


## Old code repositories and materials

### [ondex-doc](https://github.com/Rothamsted/ondex-doc.git)
Documentation and tutorials. Now replaced by [the Wiki](https://github.com/Rothamsted/knetbuilder/wiki).

### [ondex-old-components](https://github.com/Rothamsted/ondex-old-components)
Old components, which are no longer used by ONDEX, not even as dependencies taken from our Maven repository.
