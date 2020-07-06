# Installer for Ondex

;======================================================
; Includes

  	!include MUI.nsh
  	!include Sections.nsh
  	!include target\project.nsh
  	!include TextReplace.nsh
	!include "WordFunc.nsh"
  	!insertmacro VersionCompare

;======================================================
; Installer Information

  	Name "${PROJECT_NAME}"
  	BrandingText " "

  	SetCompressor /SOLID lzma
  	XPStyle on
  	CRCCheck on
  	AutoCloseWindow false
  	ShowInstDetails show
  	Icon "${NSISDIR}\Contrib\Graphics\Icons\orange-install.ico"
  
  	;Default installation folder
  	InstallDir "$PROGRAMFILES\${PROJECT_NAME}"
  
  	;Registry paths override for across versions
  	!undef PROJECT_REG_KEY
  	!define PROJECT_REG_KEY "SOFTWARE\${PROJECT_ORGANIZATION_NAME}\${PROJECT_NAME}"
	!undef PROJECT_REG_UNINSTALL_KEY
	!define PROJECT_REG_UNINSTALL_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROJECT_NAME}"
	!undef PROJECT_STARTMENU_FOLDER
	!define PROJECT_STARTMENU_FOLDER "${SMPROGRAMS}\${PROJECT_ORGANIZATION_NAME}\${PROJECT_NAME}"
  
  	;Get installation folder from registry if available
  	InstallDirRegKey HKLM "${PROJECT_REG_KEY}" ""

  	;Request application privileges for Windows Vista
  	RequestExecutionLevel admin

;======================================================
; Version Tab information for Setup.exe properties

  	VIProductVersion 2014.26.1.0
  	VIAddVersionKey ProductName "${PROJECT_NAME}"
  	VIAddVersionKey ProductVersion "${PROJECT_VERSION}"
  	VIAddVersionKey CompanyName "${PROJECT_ORGANIZATION_NAME}"
  	VIAddVersionKey FileVersion "${PROJECT_VERSION}"
  	VIAddVersionKey FileDescription "${PROJECT_FINAL_NAME}"
  	VIAddVersionKey LegalCopyright "${PROJECT_ORGANIZATION_NAME} 2014"

;======================================================
; Variables

  	Var StartMenuFolder
  
  	!define HELP_URL "http://www.ondex.org/doc.html"
 	!define ABOUT_URL "http://www.ondex.org/"

;======================================================
; Modern Interface Configuration

  	!define MUI_HEADERIMAGE
  	!define MUI_ABORTWARNING
  	!define MUI_COMPONENTSPAGE_SMALLDESC
  	!define MUI_HEADERIMAGE_BITMAP_NOSTRETCH
  	!define MUI_FINISHPAGE
  	!define MUI_FINISHPAGE_TEXT "Thank you for installing ${PROJECT_NAME}. \r\n\n\nYou can now run ${PROJECT_NAME} from your start menu."
  	!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\orange-install.ico"

;======================================================
; Modern Interface Pages

  	!define MUI_DIRECTORYPAGE_VERIFYONLEAVE
  	!insertmacro MUI_PAGE_LICENSE src\main\resources\lgpl.txt
  	!insertmacro MUI_PAGE_DIRECTORY
  	!insertmacro MUI_PAGE_COMPONENTS
  
  	Page custom getMemory
  
  	;Start Menu Folder Page Configuration
  	!define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKLM" 
  	!define MUI_STARTMENUPAGE_REGISTRY_KEY "${PROJECT_REG_KEY}" 
  	!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "Start Menu Folder"
  
  	!insertmacro MUI_PAGE_STARTMENU Application $StartMenuFolder
  
  	!insertmacro MUI_PAGE_INSTFILES
  	
  	# These indented statements modify settings for MUI_PAGE_FINISH
	!define MUI_FINISHPAGE_NOAUTOCLOSE
	!define MUI_FINISHPAGE_RUN
	!define MUI_FINISHPAGE_RUN_NOTCHECKED
	!define MUI_FINISHPAGE_RUN_TEXT "Launch ${PROJECT_NAME}"
	!define MUI_FINISHPAGE_RUN_FUNCTION "LaunchLink"
	!define MUI_FINISHPAGE_SHOWREADME_NOTCHECKED
	!define MUI_FINISHPAGE_SHOWREADME $INSTDIR\README.txt
	!insertmacro MUI_PAGE_FINISH
  
  	!insertmacro MUI_UNPAGE_CONFIRM
  	!insertmacro MUI_UNPAGE_INSTFILES
  	!insertmacro MUI_UNPAGE_FINISH



;======================================================
; Languages

  	!insertmacro MUI_LANGUAGE "English"

  	
;======================================================
; Installer Sections

InstType "Ondex front-end plug-ins"
InstType "Ondex front-end plug-ins (including experimental)"
InstType "All Ondex front-end and Integrator plug-ins"
InstType "All Ondex front-end and Integrator plug-ins (including experimental)" 

Section ""
    
    SetShellVarContext all
    SetOutPath $INSTDIR
    SetOverwrite on

    ;ADD YOUR OWN FILES HERE...
    File /r /x *.svn ${PROJECT_BASEDIR}\src\main\resources\lgpl.txt
    File ${PROJECT_BASEDIR}\uninstall.log
	
    ;Create uninstaller
    WriteUninstaller "$INSTDIR\Uninstall.exe"
  	
    ;Store installation folder
    WriteRegStr HKLM "${PROJECT_REG_KEY}" "" $INSTDIR
  
    !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    
    ;Create shortcuts
    CreateDirectory "$SMPROGRAMS\$StartMenuFolder"
    CreateShortCut "$SMPROGRAMS\$StartMenuFolder\README.lnk" "$INSTDIR\README.txt"
    CreateShortCut "$SMPROGRAMS\$StartMenuFolder\Uninstall.lnk" "$INSTDIR\Uninstall.exe"
  
    !insertmacro MUI_STARTMENU_WRITE_END

    ; Write file type association keys
    WriteRegStr HKCR ".oxl" "" "Ondex.Document"
    WriteRegStr HKCR "Ondex.Document" "" "Ondex Graph File"
    WriteRegStr HKCR "Ondex.Document\DefaultIcon" "" "$INSTDIR\ovtk2.exe,0"
    WriteRegStr HKCR "Ondex.Document\shell\open\command" "" '"$INSTDIR\ovtk2.exe" "%1"'

    ; Write the uninstall keys for Windows
    WriteRegStr HKLM "${PROJECT_REG_UNINSTALL_KEY}" "DisplayName" "${PROJECT_NAME}"
    WriteRegStr HKLM "${PROJECT_REG_UNINSTALL_KEY}" "DisplayVersion" "${PROJECT_VERSION}"
    WriteRegStr HKLM "${PROJECT_REG_UNINSTALL_KEY}" "DisplayIcon" "$INSTDIR\ovtk2.exe"
    WriteRegStr HKLM "${PROJECT_REG_UNINSTALL_KEY}" "HelpLink" "${HELP_URL}"
    WriteRegStr HKLM "${PROJECT_REG_UNINSTALL_KEY}" "Publisher" "${PROJECT_ORGANIZATION_NAME}"
    WriteRegStr HKLM "${PROJECT_REG_UNINSTALL_KEY}" "Readme" "$INSTDIR\README.txt"
    WriteRegStr HKLM "${PROJECT_REG_UNINSTALL_KEY}" "UninstallString" '"$INSTDIR\Uninstall.exe"'
    WriteRegStr HKLM "${PROJECT_REG_UNINSTALL_KEY}" "URLInfoAbout" "${ABOUT_URL}"

    WriteRegDWORD HKLM "${PROJECT_REG_UNINSTALL_KEY}" "NoModify" 1
    WriteRegDWORD HKLM "${PROJECT_REG_UNINSTALL_KEY}" "NoRepair" 1

SectionEnd

;======================================================
; Capture user entered memory settings

Var Memory

Function getMemory
  Push $R0
  InstallOptions::dialog $PLUGINSDIR\memorysettings.ini
  Pop $R0
  ReadINIStr $Memory "$PLUGINSDIR\memorysettings.ini" "Field 3" "state"
FunctionEnd

SectionGroup "Ondex front-end"
	Section "Ondex (required)" SecOvtk2

	        SetShellVarContext all
	        SectionIn RO
	    
	        SetOutPath $INSTDIR
	        SetOverwrite on
	    
	        ;ADD YOUR OWN FILES HERE...
	        File /r ${PROJECT_BUILD_DIR}\ovtk2\*.*
	        	        	    
	        ; replace amount of memory in runme.bat
	      	${textreplace::ReplaceInFile} $INSTDIR\runme.bat $INSTDIR\runme.bat "MEMORY=1200M" "MEMORY=$MemoryM" "/S=1 /C=0 /AO=0 /PO=0" $0
  	        ;MessageBox MB_OK 'textreplace::ReplaceInFile$\n$$0={$0}'
  	        
  	        ; replace amount of memory in ovtk2.l4j.ini
  	        ${textreplace::ReplaceInFile} $INSTDIR\ovtk2.l4j.ini $INSTDIR\ovtk2.l4j.ini "-Xmx1200M" "-Xmx$MemoryM" "/S=1 /C=0 /AO=0 /PO=0" $0
  	        ;MessageBox MB_OK 'textreplace::ReplaceInFile$\n$$0={$0}'
	    
	        CreateDirectory "$INSTDIR\plugins"
	    
	        !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
	    
	    	;Create shortcuts
	    	CreateShortCut "$SMPROGRAMS\$StartMenuFolder\Start Ondex with console.lnk" "$INSTDIR\runme.bat"
	    	CreateShortCut "$SMPROGRAMS\$StartMenuFolder\Start Ondex.lnk" "$INSTDIR\ovtk2.exe"
	  
	        !insertmacro MUI_STARTMENU_WRITE_END
	SectionEnd
	
	Section "Ondex Default plug-in" SecOvtk2Default
	    
	    SetOutPath $INSTDIR
	    SetOverwrite on
	    
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\ovtk2-default-distribution.jar" "${PROJECT_BUILD_DIR}\plugins\ovtk2-default-distribution.jar"
	    
	    SectionIn 1 2 3 4
	SectionEnd
	
	Section "Ondex Experimental plug-in" SecOvtk2Experimental
	    
	    SetOutPath $INSTDIR
	    SetOverwrite on
	    
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\ovtk2-experimental-distribution.jar" "${PROJECT_BUILD_DIR}\plugins\ovtk2-experimental-distribution.jar"
	    
	    SectionIn 2 4
	SectionEnd
	
	Section "Ondex Poplar plug-in" SecOvtk2Poplar
	    
	    SetOutPath $INSTDIR
	    SetOverwrite on
	    
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\ovtk2-poplar-distribution.jar" "${PROJECT_BUILD_DIR}\plugins\ovtk2-poplar-distribution.jar"
	    
	    SectionIn 2 4
	SectionEnd
SectionGroupEnd

SectionGroup "Ondex Integrator"
	Section "Integrator Arabidopsis plug-in" SecOndexArabidopsis
	    
	    SetOutPath $INSTDIR
	    SetOverwrite on
	    
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\arabidopsis-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\arabidopsis-jar-with-dependencies.jar"
	    
	    SectionIn 3 4
	SectionEnd
	
	Section "Integrator Biobase plug-in" SecOndexBiobase
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\biobase-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\biobase-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Blast plug-in" SecOndexBlast
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\blast-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\blast-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Carbs plug-in" SecOndexCarbs
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\carbs-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\carbs-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Chemical plug-in" SecOndexChemical
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\chemical-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\chemical-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Clustering plug-in" SecOndexClustering
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\clustering-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\clustering-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Cyc-pathwaydbs plug-in" SecOndexCyc-pathwaydbs
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\cyc-pathwaydbs-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\cyc-pathwaydbs-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator CyJS_JSON plug-in" SecOndexCyJS_JSON
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\cyjs_json-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\cyjs_json-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Decypher plug-in" SecOndexDecypher
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\decypher-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\decypher-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Enzymatics plug-in" SecOndexEnzymatics
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\enzymatics-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\enzymatics-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Experimental plug-in" SecOndexExperimental
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\experimental-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\experimental-jar-with-dependencies.jar"
	
		SectionIn 4
	SectionEnd
	
	Section "Integrator Generic plug-in" SecOndexGeneric
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\generic-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\generic-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator GO plug-in" SecOndexGO
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\go-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\go-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Grain plug-in" SecOndexGrain
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\grain-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\grain-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator GraphQuery plug-in" SecOndexGraphQuery
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\graph-query-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\graph-query-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Graphalgo plug-in" SecOndexGraphalgo
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\graphalgo-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\graphalgo-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator GSK plug-in" SecOndexGSK
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\gsk-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\gsk-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Habitat plug-in" SecOndexHabitat
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\habitat-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\habitat-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator IAH plug-in" SecOndexIAH
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\iah-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\iah-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Interaction plug-in" SecOndexInteraction
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\interaction-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\interaction-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator JSON plug-in" SecOndexJSON
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\json-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\json-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator KEGG plug-in" SecOndexKEGG
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\kegg-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\kegg-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Legacy plug-in" SecOndexLegacy
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\legacy-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\legacy-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator OXL plug-in" SecOndexOXL
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\oxl-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\oxl-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Phenotypes plug-in" SecOndexPhenotypes
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\phenotypes-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\phenotypes-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Plants plug-in" SecOndexPlants
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\plants-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\plants-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Poplar plug-in" SecOndexPoplar
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\poplar-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\poplar-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Prolog plug-in" SecOndexProlog
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\prolog-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\prolog-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Protein-structure plug-in" SecOndexProtein-structure
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\protein-structure-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\protein-structure-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator PSI-MI plug-in" SecOndexPSIMI
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\psimi2ondex-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\psimi2ondex-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator RDF plug-in" SecOndexRDF
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\rdf-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\rdf-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Relevance plug-in" SecOndexRelevance
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\relevance-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\relevance-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator SBML plug-in" SecOndexSBML
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\sbml-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\sbml-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Sequence plug-in" SecOndexSequence
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\sequence-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\sequence-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Tab plug-in" SecOndexTab
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\tab-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\tab-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Taxonomy plug-in" SecOndexTaxonomy
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\taxonomy-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\taxonomy-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Textmining plug-in" SecOndexTextmining
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\textmining-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\textmining-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
	
	Section "Integrator Validator plug-in" SecOndexValidator
	
	    SetOutPath $INSTDIR
	    SetOverwrite on
	
	    ;ADD YOUR OWN FILES HERE...
	    File "/oname=$INSTDIR\plugins\validator-jar-with-dependencies.jar" "${PROJECT_BUILD_DIR}\plugins\validator-jar-with-dependencies.jar"
	
		SectionIn 3 4
	SectionEnd
SectionGroupEnd

;--------------------------------
;Descriptions

  	;Language strings
  	LangString DESC_SecOvtk2 ${LANG_ENGLISH} "This is the front-end application for ${PROJECT_NAME}."
  	LangString DESC_SecOvtk2Default ${LANG_ENGLISH} "Default functionality for ${PROJECT_NAME}."
  	LangString DESC_SecOvtk2Experimental ${LANG_ENGLISH} "Experimental functionality for ${PROJECT_NAME}."
  	LangString DESC_SecOvtk2Poplar ${LANG_ENGLISH} "Poplar functionality for ${PROJECT_NAME}."
  	LangString DESC_SecOndexArabidopsis ${LANG_ENGLISH} "Arabidopsis functionality plugin-in for Integrator."
  	LangString DESC_SecOndexBiobase ${LANG_ENGLISH} "Biobase functionality plugin-in for Integrator."
  	LangString DESC_SecOndexBlast ${LANG_ENGLISH} "Blast functionality plugin-in for Integrator."
  	LangString DESC_SecOndexCarbs ${LANG_ENGLISH} "Carbs functionality plugin-in for Integrator."
  	LangString DESC_SecOndexChemical ${LANG_ENGLISH} "Chemical functionality plugin-in for Integrator."
  	LangString DESC_SecOndexClustering ${LANG_ENGLISH} "Clustering functionality plugin-in for Integrator."
  	LangString DESC_SecOndexCyc-pathwaydbs ${LANG_ENGLISH} "Cyc-pathwaydbs functionality plugin-in for Integrator."
  	LangString DESC_SecOndexCyJS_JSON ${LANG_ENGLISH} "CytoscapeJS_JSON functionality plugin-in for Integrator."
  	LangString DESC_SecOndexDecypher ${LANG_ENGLISH} "Decypher functionality plugin-in for Integrator."
  	LangString DESC_SecOndexEnzymatics ${LANG_ENGLISH} "Enzymatics functionality plugin-in for Integrator."
  	LangString DESC_SecOndexExperimental ${LANG_ENGLISH} "Experimental functionality plugin-in for Integrator."
  	LangString DESC_SecOndexGeneric ${LANG_ENGLISH} "Generic functionality plugin-in for Integrator."
  	LangString DESC_SecOndexGO ${LANG_ENGLISH} "GO functionality plugin-in for Integrator."
  	LangString DESC_SecOndexGrain ${LANG_ENGLISH} "Grain functionality plugin-in for Integrator."
  	LangString DESC_SecOndexGraphQuery ${LANG_ENGLISH} "Graph-query functionality plugin-in for Integrator."
  	LangString DESC_SecOndexGraphalgo ${LANG_ENGLISH} "Graphalgo functionality plugin-in for Integrator."
  	LangString DESC_SecOndexGSK ${LANG_ENGLISH} "GSK functionality plugin-in for Integrator."
  	LangString DESC_SecOndexHabitat ${LANG_ENGLISH} "Habitat functionality plugin-in for Integrator."
  	LangString DESC_SecOndexIAH ${LANG_ENGLISH} "IAH functionality plugin-in for Integrator."
  	LangString DESC_SecOndexInteraction ${LANG_ENGLISH} "Interaction functionality plugin-in for Integrator."
  	LangString DESC_SecOndexJSON ${LANG_ENGLISH} "JSON functionality plugin-in for Integrator."
  	LangString DESC_SecOndexKEGG ${LANG_ENGLISH} "KEGG functionality plugin-in for Integrator."
  	LangString DESC_SecOndexLegacy ${LANG_ENGLISH} "Legacy functionality plugin-in for Integrator."
  	LangString DESC_SecOndexOXL ${LANG_ENGLISH} "OXL functionality plugin-in for Integrator."
  	LangString DESC_SecOndexPhenotypes ${LANG_ENGLISH} "Phenotypes functionality plugin-in for Integrator."
  	LangString DESC_SecOndexPlants ${LANG_ENGLISH} "Plants functionality plugin-in for Integrator."
  	LangString DESC_SecOndexPoplar ${LANG_ENGLISH} "Poplar functionality plugin-in for Integrator."
  	LangString DESC_SecOndexProlog ${LANG_ENGLISH} "Prolog functionality plugin-in for Integrator."
  	LangString DESC_SecOndexProtein-structure ${LANG_ENGLISH} "Protein-structure functionality plugin-in for Integrator."
  	LangString DESC_SecOndexPSIMI ${LANG_ENGLISH} "PSI-MI functionality plugin-in for Integrator."
  	LangString DESC_SecOndexRDF ${LANG_ENGLISH} "RDF functionality plugin-in for Integrator."
  	LangString DESC_SecOndexRelevance ${LANG_ENGLISH} "Relevance functionality plugin-in for Integrator."
  	LangString DESC_SecOndexSBML ${LANG_ENGLISH} "SBML functionality plugin-in for Integrator."
  	LangString DESC_SecOndexSequence ${LANG_ENGLISH} "Sequence functionality plugin-in for Integrator."
  	LangString DESC_SecOndexTab ${LANG_ENGLISH} "Tab functionality plugin-in for Integrator."
  	LangString DESC_SecOndexTaxonomy ${LANG_ENGLISH} "Taxonomy functionality plugin-in for Integrator."
  	LangString DESC_SecOndexTextmining ${LANG_ENGLISH} "Textmining functionality plugin-in for Integrator."
  	LangString DESC_SecOndexValidator ${LANG_ENGLISH} "Validator functionality plugin-in for Integrator."

  	;Assign language strings to sections
  	!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOvtk2} $(DESC_SecOvtk2)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOvtk2Default} $(DESC_SecOvtk2Default)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOvtk2Experimental} $(DESC_SecOvtk2Experimental)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOvtk2Poplar} $(DESC_SecOvtk2Poplar)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexArabidopsis} $(DESC_SecOndexArabidopsis)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexBiobase} $(DESC_SecOndexBiobase)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexBlast} $(DESC_SecOndexBlast)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexCarbs} $(DESC_SecOndexCarbs)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexChemical} $(DESC_SecOndexChemical)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexClustering} $(DESC_SecOndexClustering)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexCyc-pathwaydbs} $(DESC_SecOndexCyc-pathwaydbs)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexCyJS_JSON} $(DESC_SecOndexCyJS_JSON)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexDecypher} $(DESC_SecOndexDecypher)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexEnzymatics} $(DESC_SecOndexEnzymatics)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexExperimental} $(DESC_SecOndexExperimental)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexGeneric} $(DESC_SecOndexGeneric)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexGO} $(DESC_SecOndexGO)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexGrain} $(DESC_SecOndexGrain)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexGraphQuery} $(DESC_SecOndexGraphQuery)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexGraphalgo} $(DESC_SecOndexGraphalgo)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexGSK} $(DESC_SecOndexGSK)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexHabitat} $(DESC_SecOndexHabitat)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexIAH} $(DESC_SecOndexIAH)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexInteraction} $(DESC_SecOndexInteraction)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexJSON} $(DESC_SecOndexJSON)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexKEGG} $(DESC_SecOndexKEGG)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexLegacy} $(DESC_SecOndexLegacy)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexOXL} $(DESC_SecOndexOXL)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexPhenotypes} $(DESC_SecOndexPhenotypes)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexPlants} $(DESC_SecOndexPlants)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexPoplar} $(DESC_SecOndexPoplar)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexProlog} $(DESC_SecOndexProlog)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexProtein-structure} $(DESC_SecOndexProtein-structure)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexPSIMI} $(DESC_SecOndexPSIMI)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexRDF} $(DESC_SecOndexRDF)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexRelevance} $(DESC_SecOndexRelevance)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexSBML} $(DESC_SecOndexSBML)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexSequence} $(DESC_SecOndexSequence)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexTab} $(DESC_SecOndexTab)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexTaxonomy} $(DESC_SecOndexTaxonomy)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexTextmining} $(DESC_SecOndexTextmining)
  	!insertmacro MUI_DESCRIPTION_TEXT ${SecOndexValidator} $(DESC_SecOndexValidator)
  	!insertmacro MUI_FUNCTION_DESCRIPTION_END

;======================================================
; Select all sections

Function .onInit
  InitPluginsDir
  File /oname=$PLUGINSDIR\memorysettings.ini "memorysettings.ini"
  
  !insertmacro SelectSection ${SecOvtk2Default}
  !insertmacro SelectSection ${SecOvtk2Experimental}
  !insertmacro SelectSection ${SecOvtk2Poplar}
  !insertmacro SelectSection ${SecOndexArabidopsis}
  !insertmacro SelectSection ${SecOndexBiobase}
  !insertmacro SelectSection ${SecOndexBlast}
  !insertmacro SelectSection ${SecOndexCarbs}
  !insertmacro SelectSection ${SecOndexChemical}
  !insertmacro SelectSection ${SecOndexClustering}
  !insertmacro SelectSection ${SecOndexCyc-pathwaydbs}
  !insertmacro SelectSection ${SecOndexCyJS_JSON}
  !insertmacro SelectSection ${SecOndexDecypher}
  !insertmacro SelectSection ${SecOndexEnzymatics}
  !insertmacro SelectSection ${SecOndexExperimental}
  !insertmacro SelectSection ${SecOndexGeneric}
  !insertmacro SelectSection ${SecOndexGO}
  !insertmacro SelectSection ${SecOndexGrain}
  !insertmacro SelectSection ${SecOndexGraphQuery}
  !insertmacro SelectSection ${SecOndexGraphalgo}
  !insertmacro SelectSection ${SecOndexGSK}
  !insertmacro SelectSection ${SecOndexHabitat}
  !insertmacro SelectSection ${SecOndexIAH}
  !insertmacro SelectSection ${SecOndexInteraction}
  !insertmacro SelectSection ${SecOndexJSON}
  !insertmacro SelectSection ${SecOndexKEGG}
  !insertmacro SelectSection ${SecOndexLegacy}
  !insertmacro SelectSection ${SecOndexOXL}
  !insertmacro SelectSection ${SecOndexPhenotypes}
  !insertmacro SelectSection ${SecOndexPlants}
  !insertmacro SelectSection ${SecOndexPoplar}
  !insertmacro SelectSection ${SecOndexProlog}
  !insertmacro SelectSection ${SecOndexProtein-structure}
  !insertmacro SelectSection ${SecOndexPSIMI}
  !insertmacro SelectSection ${SecOndexRDF}
  !insertmacro SelectSection ${SecOndexRelevance}
  !insertmacro SelectSection ${SecOndexSBML}
  !insertmacro SelectSection ${SecOndexSequence}
  !insertmacro SelectSection ${SecOndexTab}
  !insertmacro SelectSection ${SecOndexTaxonomy}
  !insertmacro SelectSection ${SecOndexTextmining}
  !insertmacro SelectSection ${SecOndexValidator}
     
  ;Check earlier installation
  ClearErrors
  ReadRegStr $0 HKLM "${PROJECT_REG_UNINSTALL_KEY}" "DisplayVersion"
  IfErrors init.uninst ; older versions might not have "DisplayVersion" string set
  ${VersionCompare} $0 ${PROJECT_VERSION} $1
  IntCmp $1 2 init.uninst
    MessageBox MB_YESNO|MB_ICONQUESTION "${PROJECT_NAME} version $0 seems to be already installed on your system.$\nWould you like to proceed with the installation of version ${PROJECT_VERSION}?" \
        IDYES init.uninst
    Quit

init.uninst:
  ClearErrors
  ReadRegStr $0 HKLM "${PROJECT_REG_KEY}" ""
  IfErrors init.done
  ExecWait '"$0\Uninstall.exe" _?=$0' ; /S for silent uninstall

init.done:
FunctionEnd
  	
;--------------------------------
;Uninstaller Section

Section "Uninstall"

    SetShellVarContext all
    
    !macro BadPathsCheck
	StrCpy $R0 $INSTDIR "" -2
	StrCmp $R0 ":\" bad
	StrCpy $R0 $INSTDIR "" -14
	StrCmp $R0 "\Program Files" bad
	StrCpy $R0 $INSTDIR "" -8
	StrCmp $R0 "\Windows" bad
	StrCpy $R0 $INSTDIR "" -6
	StrCmp $R0 "\WinNT" bad
	StrCpy $R0 $INSTDIR "" -9
	StrCmp $R0 "\system32" bad
	StrCpy $R0 $INSTDIR "" -8
	StrCmp $R0 "\Desktop" bad
	StrCpy $R0 $INSTDIR "" -22
	StrCmp $R0 "\Documents and Settings" bad
	StrCpy $R0 $INSTDIR "" -13
	StrCmp $R0 "\My Documents" bad done
	bad:
	  MessageBox MB_OK|MB_ICONSTOP "Install path invalid!"
	  Abort
	done:
	!macroend
	 
	ClearErrors
	ReadRegStr $INSTDIR HKLM "${PROJECT_REG_KEY}" ""
	IfErrors +2
	StrCmp $INSTDIR "" 0 +2
	  StrCpy $INSTDIR $EXEDIR
	 
	# Check that the uninstall isn't dangerous.
	!insertmacro BadPathsCheck
	 
	# Does path end with "\Ondex"?
	!define CHECK_PATH "\Ondex"
	StrLen $R1 "${CHECK_PATH}"
	StrCpy $R0 $INSTDIR "" -$R1
	StrCmp $R0 "${CHECK_PATH}" +3
	  MessageBox MB_YESNO|MB_ICONQUESTION "Unrecognised uninstall path. Continue anyway?" IDYES +2
	  Abort
	 
	IfFileExists "$INSTDIR\*.*" 0 +2
	IfFileExists "$INSTDIR\ovtk2.exe" +3
	  MessageBox MB_OK|MB_ICONSTOP "Install path invalid!"
	  Abort
	
	; proper file based uninstall
	!include ${PROJECT_BASEDIR}\uninstall.log

    ; deletion of the installation folder
  	RMDir "$INSTDIR"
  
  	!insertmacro MUI_STARTMENU_GETFOLDER Application $StartMenuFolder
    
  	Delete "$SMPROGRAMS\$StartMenuFolder\Uninstall.lnk"
  	Delete "$SMPROGRAMS\$StartMenuFolder\README.lnk"
  	Delete "$SMPROGRAMS\$StartMenuFolder\Start Ondex with console.lnk"
  	Delete "$SMPROGRAMS\$StartMenuFolder\Start Ondex.lnk"
  	RMDir "$SMPROGRAMS\$StartMenuFolder"
  
  	DeleteRegKey HKLM "${PROJECT_REG_KEY}"
  	DeleteRegKey HKLM "${PROJECT_REG_UNINSTALL_KEY}"
  	
  	DeleteRegKey HKCR ".oxl"
	DeleteRegKey HKCR "Ondex.Document"
  	
SectionEnd

# Installer functions
Function LaunchLink
    SetShellVarContext all
  	ExecShell "" "$SMPROGRAMS\$StartMenuFolder\Start Ondex.lnk"
FunctionEnd