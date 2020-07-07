# Ondex-KnetBuilder

[Ondex](https://github.com/Rothamsted/ondex-full) components and applications that are necessary for building genome-scale knowledge networks used in projects like [KnetMiner](http://knetminer.rothamsted.ac.uk/). It includes the Ondex base, CLI, workflow engine and a set of plugins (parsers, mappers, transformers, filters and exporters) that are relevant for building genome-scale knowledge networks as described in [Hassani-Pak et al. 2016](http://www.sciencedirect.com/science/article/pii/S2212066116300308).

## Requirements

 - Java 8

## Downloads

 - See the [Downloads](https://github.com/Rothamsted/ondex-knet-builder/wiki/Downloads) Page

## Execution

 0. Create a new folder e.g. `mkdir knetbuilder-folder`

 1. Download the latest [KnetBuilder release](https://github.com/Rothamsted/KnetBuilder/releases) and unzip in knetbuilder-folder

 2. Download the [tutorial files](https://rrescloud.rothamsted.ac.uk/index.php/s/H6sl0RIT9CoMaUI) and unzip in knetbuilder-folder

 3. Check the folder structure looks like this
 ```
 -bash-4.1$ ls knetbuilder-folder/
ondex-mini-1.2
tutorial_files
```

 4. Go to the downloaded ondex-mini folder and run a workflow:
  ```
 cd ondex-mini-1.2
 ./runme.sh ../tutorial_files/Workflow/HumanDiseaseKNET.xml
 ```

 5. The KnetBuilder workflow integrates several datasets and creates a OXL file `tutorial_files/NeuroDiseaseKnet.oxl`

 6. The OXL file can be opened in the [Ondex Desktop](http://www.ondex.org) application or explored in the [KnetMiner](http://knetminer.rothamsted.ac.uk/HumanDisease/) web application
 
 
 ## Documentation
 
 We keep most of our documentation on this repository's [Wiki](https://github.com/Rothamsted/ondex-knet-builder/wiki) (note that there are many links on the right column in that page).
