@echo off
cd /d %~dp0
set MEMORY=1200M
set DATA=data/
echo Running OVTK with %MEMORY% and data dir %DATA% edit runme.bat to change this amount

setLocal EnableDelayedExpansion
set CLASSPATH="
 for /R ./lib %%a in (*.jar) do (
   set CLASSPATH=!CLASSPATH!;%%a
 )
 set CLASSPATH=!CLASSPATH!"


java -Xmx%MEMORY% -Dondex.dir=%DATA% -Dplugin.scan.lib=false -classpath !classpath! net.sourceforge.ondex.workflow2.gui.Main %1