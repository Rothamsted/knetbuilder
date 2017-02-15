cd "$(dirname $0)/.."
#
# Push all COMMITED local changes back to GitHub, including submodules/subrepositories.
#
git push --recurse-submodules=on-demand
