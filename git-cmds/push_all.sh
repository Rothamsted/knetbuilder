cd "$(dirname $0)/.."
#
# Push all COMMITED local changes back to GitHub, including submodules/subrepositories.
#
echo -e "\n\n\tWARNING: run this command only after you have committed changes in ondex-full.\n"

for repo in ondex-base ondex-desktop ondex-opt ondex-base ondex-doc ondex-knet-builder ondex-old-components
do
  echo -e "\n\n$repo"
  cd "$repo"
  git push --all
  git push --tag
  cd ..
done

# Not clear how the heck it works on submodules, so let's redo it, just in case 
git push --all --recurse-submodules=on-demand
git push --tag --recurse-submodules=on-demand
