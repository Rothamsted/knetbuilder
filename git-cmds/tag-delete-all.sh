tag="$1"
if [[ -z "$tag" ]]; then
  echo -e "\n\n\t`basename $0` <tag>\n"
  exit 1
fi

cd `dirname $0`/..

git submodule foreach --recursive "git tag --delete '$tag'; true"
git submodule foreach --recursive "git push --force origin ':refs/tags/$tag'; true"
git push --force origin ":refs/tags/$tag"


