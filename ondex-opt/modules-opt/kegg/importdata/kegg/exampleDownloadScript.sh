#!/bin/bash 
# 
# This shell script downloads and installs the KEGG files need to run the KEGG parser...run from the dir you want to create files in e.g. datadir/importdata/kegg
#NB this takes a while to run...reccomend you modify this script to run some tasks concurrently

## Set your proxy settings if needed
#export http_proxy=
#export https_proxy=
#export ftp_proxy=

wget --no-check-certificate ftp://ftp.genome.jp/pub/kegg/release/current/brite.tar.gz
wget --no-check-certificate ftp://ftp.genome.jp/pub/kegg/release/current/brite.tar.gz.md5

#check the file was downloaded correctly
md5sum -c brite.tar.gz.md5

mkdir brite
cd brite
gtar xvzf brite.tar.gz
cd ..

wget --no-check-certificate ftp://ftp.genome.jp/pub/kegg/release/current/kgml.tar.gz
wget --no-check-certificate ftp://ftp.genome.jp/pub/kegg/release/current/kgml.tar.gz.md5

md5sum -c kgml.tar.gz.md5

mkdir kgml
cd kgml
gtar xvzf kgml.tar.gz
cd ..

wget --no-check-certificate ftp://ftp.genome.jp/pub/kegg/release/current/ligand.tar.gz
wget --no-check-certificate ftp://ftp.genome.jp/pub/kegg/release/current/ligand.tar.gz.md5

md5sum -c ligand.tar.gz.md5

mkdir ligand
cd ligand
gtar xvzf ligand.tar.gz
cd ..

wget --no-check-certificate ftp://ftp.genome.jp/pub/kegg/release/current/pathway.tar.gz
wget --no-check-certificate ftp://ftp.genome.jp/pub/kegg/release/current/pathway.tar.gz.md5

md5sum -c pathway.tar.gz.md5

mkdir pathway
cd pathway
gtar xvzf pathway.tar.gz
cd ..

wget --no-check-certificate ftp://ftp.genome.jp/pub/kegg/release/current/pathway_dbget.tar.gz
wget --no-check-certificate ftp://ftp.genome.jp/pub/kegg/release/current/pathway_dbget.tar.gz.md5

md5sum -c pathway_dbget.tar.gz.md

mkdir pathway_dbget
cd pathway_dbget
gtar xvzf pathway_dbget.tar.gz
cd ..

wget --no-check-certificate ftp://ftp.genome.jp/pub/kegg/release/current/genes.tar.gz
wget --no-check-certificate ftp://ftp.genome.jp/pub/kegg/release/current/genes.tar.gz.md5

md5sum -c genes.tar.gz.md5

mkdir genes
cd genes
gtar xvzf genes.tar.gz
cd ..

wget --no-check-certificate ftp://ftp.genome.jp/pub/kegg/release/current/medicus.tar.gz
wget --no-check-certificate ftp://ftp.genome.jp/pub/kegg/release/current/medicus.tar.gz.md5

md5sum -c medicus.tar.gz.md5

mkdir medicus
cd medicus
gtar xvzf medicus.tar.gz
cd ..
