cd "$(dirname $0)/.."
#
# Check the status on main and subrepos.
#
git submodule foreach --recursive git status
git status

