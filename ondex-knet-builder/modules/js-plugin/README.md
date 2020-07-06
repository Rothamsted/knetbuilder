#JavaScript Plug-in

An Integrator plug-in that allows for invoking a Javascript file from an Integrator workflow.
Comes with ONDEX entities available inside the scripting engine, so that you can run the same scripts
that are available from the ONDEX Console component.

See examples in src/test/resources. You can specify a script to run in a workflow step, as plug-in parameter.
Similarly, you can pass parameters to the script, by defining them in the workflow configuration, the script
will see them as a global variable. 
