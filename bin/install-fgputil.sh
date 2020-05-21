#!/usr/bin/env sh

. bin/lib/colors.sh

dir=$1

readonly LIBS="vendor/fgputil-accountdb-1.0.0.jar
  vendor/fgputil-core-1.0.0.jar
  vendor/fgputil-db-1.0.0.jar
  vendor/fgputil-web-1.0.0.jar"

echo "${C_BLUE}Checking for FgpUtil libraries${C_NONE}"

if ! stat ${LIBS} > /dev/null 2>&1; then
  echo "${C_CYAN}  Not found.  Installing${C_NONE}"
  bin/build-fgputil.sh ${dir} 2>&1 | sed 's/^/  /'
fi
