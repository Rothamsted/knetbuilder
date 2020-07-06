===== Requirements =====

* Hardware: Ondex requires at least
- 2GHz CPU
- 1GB RAM (2GB recommended)
- 300 MB of free HDD space

* Operating system: Ondex is a Java 6 application and therefore compatible with
- Windows XP / Vista / 7
- Linux / UNIX / Solaris
- Mac OS 64 bit (as this version supports Java 6) however never tested

* Software: Make sure you have the latest Java Runtime Environment (Version 1.6 or higher) installed on your system.
If not, you can download it from http://java.sun.com

Also make sure your PATH variable contains your java executable. 
To test whether this is the case see section "Testing the requirements" below.


===== Installer =====
There is an Ondex installer for Windows users. 
In the Ondex setup, the page "Choose Components" lets the user decide which features of Ondex they wish to install.
Several options are possible:
1) Ondex front-end plug-ins
2) Ondex front-end plug-ins (including experimental)
3) All Ondex front-end and Integrator plug-ins
4) All Ondex front-end and Integrator plug-ins (including experimental)
5) Custom

The first option will allow users to use data visualisation tools which are stable.
The second option will allow users to use data visualisation tools which are stable as well as experimental.
The third option will allow users to use data visualisation and integration tools which are stable.
The fourth option will allow users to use data visualisation and integration tools which are stable as well as experimental.
The fifth option will allow users to customize what their setup and manually select what they wish to install.
(For this fifth option, the plug-ins selected by default depend on what option out of the first four was last selected.)

The installer will create shortcuts (which users can then access from the Start menu) unless stated otherwise during the setup.
Running the exe file for Ondex (using this shortcut or by double-clicking on it) will check that Java Runtime Environment version 1.6 or higher is installed.
If it is not, it will automatically download it (Java Runtime Environment version 1.6 update 17).

Users of other operating systems may download the zip file and execute the runme.sh file to start Ondex.
In LINUX a "chmod 755 runme.sh" might be needed to make the script executable from your user's account.


===== Testing the requirements =====
- WINDOWS: Click Start -> Run enter "cmd" and press enter. 
- LINUX/GNOME: Right-click on your desktop and choose "Open Terminal" from the appearing context menu.
- LINUX/KDE: Choose "Konsole" from your Quick launcher.

Type "java -version" in the appearing console window and press enter.
If your system is configured correctly you will see a message that contains your current Java version. Make sure it is higher than version 1.6.
If you don't see a message, then you have either not installed Java yet, or you have not set your PATH variable.


===== How to set the PATH variable =====
- WINDOWS: 
Right-click on "My computer" and choose "Properties" from the appearing context menu. 
Switch to the tab "Advanced" in the appearing window. Click the button "Environment Variables" on the bottom left of the window. 
You will see a list of variables with their assigned values. 
If the PATH variable already occurs in it, select it and click "Edit". 
Append a semicolon to the end of the value field and then enter the path to your Java binary directory. 
This is usually something like "C:\Program Files\Java\jdk1.6.0_17\bin".
If the Path contains any white spaces, make sure you put it in double quotes. 
If the variable PATH didn't exist yet on your system, create a new one and call it "PATH". 
Assign the binary path as it's value. However you don't need the semicolon in this case.

- LINUX: 
Go to your home directory and open the file ".bashrc" in your favourite editor. 
Append the line PATH=\$PATH:<javadir> where <javadir> is your path to the java binary directory, 
something like $/usr/java/latest/bin/$ to find out what it actually is you can execute the command "type java" in a console window. 


===== To the attention of sysadmins =====
A "system-wide install" of Ondex is discouraged because 
putting the Ondex directory tree in root space makes it hard for users to manage input, output, scripts and databases. 
Installing Ondex in users' spaces or creating an Ondex user is a good alternative. 

Databases will mostly have to be obtained by the sysadmin/users as flatfiles on a need-to-use basis. 
The expected location of those files is under OndexDir/data/importdata/ (where OndexDir is the directory where Ondex was installed).

For a dataset which an average user is expecting to employ, it is essential to use a machine with enough memory.
It is also essential to increase the memory setting in the command line or runme file to run Ondex with more memory. 
Ondex's GUI can currently manage about 250,000 concepts and relations.
Limitations depend on available memory and CPU.
