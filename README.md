#ONDEX Optional/Additional components.

[ONDEX](https://github.com/Rothamsted/ondex-full) components to provide various additional and optional functions.

**NOTE**: these are still linked by the components above (so, not so optional), but we usually build them separately. 
If you build the whole ONDEX from there, they will be build from (the local clone of) this repository, if you build an 
ONDEX component independently (e.g., by cloning `ondex-base` and building from its local copy), some of these 'optional' components are still downloaded from our Maven repositories as dependencies. This is because it is currently hard to modularise things further (we would need to inspect the Java code).

  