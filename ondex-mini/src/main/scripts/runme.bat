@echo off
cd /d %~dp0
set MEMORY=2G
set DATA=data/

setLocal EnableDelayedExpansion
set CLASSPATH="
 for /R ./lib %%a in (*.jar) do (
   set CLASSPATH=!CLASSPATH!;%%a
 )
 set CLASSPATH=!CLASSPATH!"

java -Xmx%MEMORY% -Dondex.dir=%DATA% -classpath !classpath! net.sourceforge.ondex.OndexMiniMain -ubla -ptest -w%1
