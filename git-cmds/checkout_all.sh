branch="$1"

if [ "$branch" == "" ]; then
  printf "\n\n\tBranch missing\n\n"
  exit 1
fi

printf "\n\n\tSwitching to '$branch' in each module\n"
for repo in ondex-base ondex-desktop ondex-opt ondex-doc ondex-knet-builder ondex-old-components
do
  echo -e "\n\n$repo"
  cd "$repo"
  git checkout "$branch"
  cd ..
done

git checkout "$branch"
