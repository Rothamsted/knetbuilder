# 
# Run this the first time you clone ondex-full, if your git client doesn't download submodules automatically (i.e., 
# you see empty ondex-xxx folders)
# 
cd "$(dirname $0)/.."
echo -e "\n\n\tSetting up submodules\n"
git submodule update --init --recursive

./git-cmds/checkout_all.sh master
