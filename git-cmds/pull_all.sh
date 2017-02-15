cd "$(dirname $0)/.."
#
# Updates (pull) everything (main repo and submodules/subrepos) from GitHub). 
#
git submodule foreach --recursive git pull
git pull
