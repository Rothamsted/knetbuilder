
echo -e "\n\n\tGenerating New Download Page\n"

cd /tmp
rm -Rf knetbuilder.wiki
git clone https://github.com/Rothamsted/knetbuilder.wiki.git
cd knetbuilder.wiki

down_script="$MYDIR/ondex-knet-builder/src/main/deploy/mk_download_page.sh"
$down_script >Downloads.md

git diff --exit-code || (  
  echo -e "\n\n\tCommitting Wiki Changes\n"
  git commit -a -m "[Jenkins] Updating Downloads Page."
  git push --set-upstream origin master # credentials are already set here
)

cd "$MYDIR"


# Jenkins will do internal stuff, such as updating download links and deploying
# on our servers.
# The API URL below is provided by this plug-in: https://plugins.jenkins.io/build-token-root/
# The token in configured in the job, in the Build Triggers section.
#
echo -e "\n\n\tTriggering RRes deployment\n"
  
job='ondex_rres_deployment'
curl --user "$KNET_JENKINS_USER:$KNET_JENKINS_TOKEN" -X POST -o - --fail \
     "https://knetminer.org/build/job/$job/build"
