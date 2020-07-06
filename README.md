# ONDEX (ondex-full)

[ONDEX](http://www.ondex.org/) is a framework for text mining, data integration and data analysis. 

## ondex-full
This is an aggregator repository, used to download and build all about ONDEX.

This repo is normally not needed, unless you have to work on the whole ONDEX (e.g., to rename something in the whole code base), if you need to work on a single component, you can clone the needed sub-repository separately (see below).

## Usage

Clone the usual way, **then run `git-cmds/download_all.sh`** to download the component GitHub repositories.
Once you've done this, you'll have this repository in the `ondex-full/` folder and, inside it,
sub-folders (e.g., `ondex-base/`, `knet-builder/`) which are clones of the respective repositories
(this uses the [git submodule](https://github.com/blog/2104-working-with-submodules) mechanism).

**WARNING!**
This is repository is using other GitHub repositories as [submodules](https://github.com/blog/2104-working-with-submodules). This means you have to commit changes made to submodules/subrepos one by one, from the local clones of the various submodules. Moreover, this top repository points to subrepos by referring to a specific commit. This means that, once
you have updated/pushed/etc a subrepo, **you do need to update the reference to it** that is hereby. You find convenience
Bash scripts to do so in `git-cmds/`. [SmartGit](http://www.syntevo.com/smartgit) is another tool that manages this way to organise submodules.

See these notes on [how to manage submodules](https://git-scm.com/book/en/v2/Git-Tools-Submodules) to learn more. Further details are available [here](https://git-scm.com/book/en/v2/Git-Tools-Submodules).

Note that the `download_all.sh` command above switches the submodules clones onto the master branch, it does not keep the detached `HEAD` that git normally downloads for submodules. 


## Included Repositories

The following are all the repositories that ONDEX is composed of and which are linked by this repo. Many of them rely on the POM present hereby
(`artifactId=ondex-full`).

If you need to develop your own ONDEX extension, you can make it dependant on this same POM, by means of the following:

```xml
<project ...>
  ...
  <parent>
    <groupId>net.sourceforge.ondex</groupId>
    <artifactId>ondex-full</artifactId>
    <version>0.6.0-SNAPSHOT</version>
  </parent>
  ...
  <repositories>
     <repository>
        <id>ondex_repo</id>
        <url>http://ondex.rothamsted.ac.uk/nexus/content/groups/public</url>
     </repository>
     <repository>
        <id>ondex_repo_snapshots</id>
        <url>http://ondex.rothamsted.ac.uk/nexus/content/groups/public-snapshots</url>
     </repository>
  </repositories>
  ...
</project>
```

If you only need ONDEX dependencies, you can list our Maven repository listed above, without the `<parent>` section.

[ondex-base](https://github.com/Rothamsted/ondex-base.git)
The base code, used to build everything else.

[ondex-knet-builder](https://github.com/Rothamsted/ondex-knet-builder.git)
What we use to build projects like [KNetMiner](http://knetminer.rothamsted.ac.uk).

[ondex-desktop](https://github.com/Rothamsted/ondex-desktop.git)
The Desktop/GUI applications.

[ondex-doc](https://github.com/Rothamsted/ondex-doc.git)
Documentation and tutorials.

[ondex-integration-tests](https://github.com/Rothamsted/ondex-integration-tests.git)
Integrations tests for ONDEX.

[ondex-opt](https://github.com/Rothamsted/ondex-opt.git)
Optional components. **NOTE**: these are still linked by the components above (so, not so optional), but we usually build them separately. If you build the whole ONDEX from this top repository, these optional modules will be built from (the local clone of) this child repository, if you build an ONDEX component independently (e.g., by cloning `ondex-base` and building from its local copy), some of these 'optional' components are still downloaded from our Maven repositories as dependencies. This is because it is currently hard to modularise things further (we would need to inspect the Java code).

[ondex-old-components](https://github.com/Rothamsted/ondex-old-components)
Old components, which are no longer used by ONDEX, not even as dependencies taken from our Maven repository.
