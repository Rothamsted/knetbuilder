# Ondex-KnetBuilder

[Ondex](http://www.ondex.org/) is a framework for text mining, data integration and data analysis. 


## Requirements

 - At least Java 11. Note that we have migrated to this recently and **Ondex doesn't work with Java 1.8 anymore**. 

## Downloads

 - See the [Downloads](https://github.com/Rothamsted/ondex-knet-builder/wiki/Downloads) Page

## Documentation

 - A tuturial on building knowledge grahs with KnetBuilder is available [here](https://github.com/Rothamsted/knetbuilder/wiki/Building-Knowledge-Networks)
 
 - We keep most of our documentation on this repository's 
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


## Old code repositories and materials

### [ondex-doc](https://github.com/Rothamsted/ondex-doc.git)
Documentation and tutorials. Now replaced by [the Wiki](https://github.com/Rothamsted/knetbuilder/wiki).

### [ondex-old-components](https://github.com/Rothamsted/ondex-old-components)
Old components, which are no longer used by ONDEX, not even as dependencies taken from our Maven repository.
