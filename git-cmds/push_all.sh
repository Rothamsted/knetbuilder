cd "$(dirname $0)/.."
#
# Push all COMMITED local changes back to GitHub, including submodules/subrepositories.
#
git push --all --recurse-submodules=on-demand
git push --tag --recurse-submodules=on-demand
