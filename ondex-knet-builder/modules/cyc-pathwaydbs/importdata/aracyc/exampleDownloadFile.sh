#!/bin/bash 
# 
# This shell script downloads and installs the  files need to run the AraCyc parser...run from the dir you want to create files in e.g. datadir/importdata/aracyc
# NB you should register and replace the URL with the one AraCyc provides

wget --no-check-certificate ftp://ftp.arabidopsis.org/home/tair/home/tair/tmp/private/plantcyc/aracyc.tar.gz

gtar xvzf aracyc.tar.gz