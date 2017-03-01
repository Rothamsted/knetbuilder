#KnetBuilder

[Ondex](https://github.com/Rothamsted/ondex-full) components and applications that are necessary for building genome-scale knowledge networks used in projects like [KnetMiner](http://knetminer.rothamsted.ac.uk/). It includes the Ondex base, CLI, workflow engine and a set of plugins (parsers, mappers, transformers, filters and exporters) as described in [Hassani-Pak et al. 2016](http://www.sciencedirect.com/science/article/pii/S2212066116300308).

##Requirements
 - Java 8
 
##Execution

 1. Download the latest [KnetBuilder release](https://github.com/Rothamsted/KnetBuilder/releases) and unzip
 
 2. Download the [tutorial files](https://rrescloud.rothamsted.ac.uk/index.php/s/H6sl0RIT9CoMaUI) and unzip
 
 3. Place the tutorial_files folder on the same level with the KnetBuilder folder
 ```
 -bash-4.1$ ls
knetbuilder_tutorial_files.zip
ondex-mini-1.0-20170228.230404-8-packaged-distro.zip
ondex-mini-1.0-SNAPSHOT
tutorial_files
```

 4. Go to the KnetMiner folder and run a workflow:
  ```
 cd ondex-mini-1.0-SNAPSHOT
 ./runme.sh ../tutorial_files/Workflow/HumanDiseaseKNET.xml
 ```
 
 5. The KnetBuilder workflow integrates several datasets and creates a OXL file `tutorial_files/NeuroDiseaseKnet.oxl`
 
 6. The OXL file can be opened in the [Ondex Desktop](http://www.ondex.org) application or explored in the [KnetMiner](http://knetminer.rothamsted.ac.uk/HumanDisease/) web application
 
