#!/usr/bin/env bash
set -e

# Invoked internally as part of the CI, deploys the binary packages onto the RRes file system.
#
# This used to be invoked by our Jenkins, but we're dismissing that, in order to avoid some sysadmin 
# overhead. Now the script is invoked in polling mode by cron, see rres-deploy-crontab.sh
#

knet_software_dir=/home/data/knetminer/software
last_update_path_base="$knet_software_dir/ondex-deploy/last-update-times/"


# Deals with the deployment of a single package listed by the Downloads page (see the implementation).
#
# This finds the latest .zip download link in the auto-published Downloads page 
# 
function deploy_package ()
{
	# A variable name to identify the package, in last_update_path and elsewhere,
	# eg, "ondex_desktop"	
	package_id="$1" 
	
  target_dir="$2" # Local installation path, eg, $knet_software_dir/ondex-desktop_SNAPSHOT
  package_dir="$3" # The root dir inside the .zip, eg, ondex
  url_probe="$4" # A URL fragment to find the .zip download link on the Download page, eg, installer
 
  echo -e "\n\n\tDeploying $package_id\n"

  if [ "$target_dir" == '' ]; then
    echo -e "\n\n\tTarget Dir parameter is empty\n"
    exit 1
  fi

	# Gets the download URL and the last version timestamp

  link_label='Latest Dev Release'
  wget_cmd="wget --no-verbose"

  dl_url=$(get_download_url "$url_probe")
	last_ver_tstamp=$(get_package_timestamp "$dl_url")
	last_update_timestamp=$(get_last_update_timestamp "$package_id")

	# Is it recent?
		
	if [[ ! -z "$last_update_timestamp" ]] && [[ ! "$last_update_timestamp" > "$last_ver_tstamp" ]]; then
  	printf "\n\tPackage '%s' time %s is not newer than last update time %s, skipping deployment\n" \
  		"$package_id" "$last_ver_tstamp" "$last_update_timestamp"
  	return 
  fi

	# OK, let's go

  echo -e "\nDeploying on '$target_dir'\n"
  
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
  
  update_last_update_date "$package_id" "$last_ver_tstamp"
  
  printf "\n\n\t Package '%s' deployed (version time: %s)\n\n" "$package_id" "$last_ver_tstamp"
}


function get_download_url
{
  url_probe="$1" # URL fragment to search, see deploy_package()
  
  link_label='Latest Dev Release'
  wget_cmd="wget --no-verbose"

  dl_url=$($wget_cmd -O - "https://github.com/Rothamsted/ondex-knet-builder/wiki/Downloads" \
      | egrep "a href=\"(.+${url_probe}.+)\".+$link_label" \
      | sed -E s/".+\"(.+${url_probe}.+)\" .+"/'\1'/)

	printf "$dl_url"
}


# The package timestamp, extracted from the download URL.
function get_package_timestamp
{
	download_url="$1"
	
	# Format is like: 
	# https://knetminer.rothamsted.ac.uk/.../installer-7.0.2-20250828.101107-18-packaged-distro.zip
	# https://knetminer.rothamsted.ac.uk/.../rdf-export-2-cli-7.0.2-20250828.101107-18.zip
	
	timestamp=$(echo "$download_url" | sed -E s/".+-([0-9]{8}\.[0-9]{6})-[0-9]+.+"/'\1'/)
	printf "$timestamp"
}


function get_last_update_timestamp
{
	# The last update timestamp of a package, as it comes from a tracker file, if it exists
	package_id="$1" # identifies the package, see deploy_package()
	
	last_update_path="$(get_last_update_path "$package_id")"
	[[ ! -r "$last_update_path" ]] || tstamp=$(cat "$last_update_path")
	printf "$tstamp"
}


function update_last_update_date
{
	# Updates the last update timestamp of a package, by writing it to its tracker file
	
	package_id="$1" # identifies the package, see deploy_package()
	ver_date="$2" # The new timestamp to be written, in the same format
		
	last_update_path="$(get_last_update_path "$package_id")"
	echo "$ver_date" >"$last_update_path"
}


function get_last_update_path
{
	package_id="$1" # identifies the package, see deploy_package()
	
	last_update_path="$last_update_path_base$package_id.time"
	printf "$last_update_path"
}


# Here we go, the stuff above is run for every package we need to deploy
#

deploy_package ondex-mini "$knet_software_dir/ondex-mini_SNAPSHOT" ondex-mini ondex-mini
deploy_package ondex-desktop "$knet_software_dir/ondex-desktop_SNAPSHOT" ondex installer
deploy_package rdf-exporter "$knet_software_dir/rdf-export-2-cli_SNAPSHOT" rdf-export-2-cli_* rdf-export-2-cli
