cd "$(dirname $0)/.."
#
# Push all COMMITED local changes back to GitHub, including submodules/subrepositories.
#
echo -e "\n\n\tWARNING: run this command only after you have committed changes in ondex-full.\n"

cd "`dirname $0`"/..

git submodule foreach --recursive git push --tags origin HEAD
git push --tags origin HEAD
