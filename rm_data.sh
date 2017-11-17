# Removes data directories that are auto-created.
# Use with care!!!
#
find . -type d -name 'data' -not -path '*src*/data' -exec rm -Rf \{\} \;
