set -e

function deploy_package ()
{
  target_dir="$1"
  package_dir="$2" # Created by the zip
  url_probe="$3"

  if [ "$target_dir" == '' ]; then
    echo -e "\n\n\tTarget Dir parameter is empty\n"
    exit 1
  fi

  echo -e "\n\n\t Deploying '$target_dir'\n"

  link_label='Latest Dev Release'
  wget_cmd="wget --no-verbose"

  dl_url=$($wget_cmd -O - "https://github.com/Rothamsted/ondex-knet-builder/wiki/Downloads" \
      | egrep "a href=\"(.+${url_probe}.+)\".+$link_label" \
      | sed -E s/".+\"(.+${url_probe}.+)\" .+"/'\1'/)

  cd /tmp
  rm -Rf _ondex_tmp.zip
  $wget_cmd -O _ondex_tmp.zip "$dl_url"
  rm -Rf $package_dir
  unzip _ondex_tmp.zip
  
  # This is to delete the target dir contents, without removing the dir itself
  # That's necessary in order to keep the target Unix membership and its 
  # GID settings, which, in turn is necessary to keep write permissions for 
  # our users
  find "$target_dir" -mindepth 1 -delete
  
  # Similarly, copies the package contents into the existing container
  cp --recursive --no-dereference --preserve=timestamps $package_dir/. "$target_dir"
  chmod -R ug=rwX,o=rX "$target_dir"
  rm -Rf $package_dir
}

# Here we go, the stuff above is run for every package we need to deploy
base=/home/data/knetminer/software
deploy_package "$base/ondex-mini_SNAPSHOT" ondex-mini ondex-mini
deploy_package "$base/ondex-desktop_SNAPSHOT" ondex installer
deploy_package "$base/rdf-export-2-cli_SNAPSHOT" rdf-export-2-cli_* rdf-export-2-cli

