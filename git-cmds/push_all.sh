cd "$(dirname $0)/.."
#
# Push all COMMITED local changes back to GitHub, including submodules/subrepositories.
#
echo -e "\n\n\tWARNING: run this command only after you have committed changes in ondex-full.\n"
git push --all --recurse-submodules=on-demand
git push --tag --recurse-submodules=on-demand
