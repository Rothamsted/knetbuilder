#!/usr/bin/env bash
set -e

# Download and calls the Ondex check-n-deploy script for the RRes installation
# 
# This was placed in my (brandizim2) crontab with this:
#
# KNET_ONDEX_UPDATER=/home/data/knetminer/software/ondex-deploy/ondex-deploy.sh
# */15  * * * * nice "$KNET_ONDEX_UPDATER" &>/tmp/knet-ondex-deploy.out
# # (always leave a foo trailer, some crons don't work without it) 
#
# And then a copy of this script was poured into $KNET_UPDATER.
# WARNING: before placing this in any other crontab, check nothing else similar 
# is still enabled.
# 

script_url="https://raw.githubusercontent.com/Rothamsted/knetbuilder/master/ondex-knet-builder/src/main/deploy/rres/rres-deploy.sh"

# --fail-with-body not available on RRes
# Accept: is as per https://stackoverflow.com/questions/18126559
#
bash <(curl -H "Accept: application/vnd.github.v3.raw" --fail -o -o "$script_url")
