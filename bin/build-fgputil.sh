#!/usr/bin/env sh

from=$1
dir=$(pwd)

echo "Cloning latest FgpUtil version"

git clone \
  --depth 1 \
  https://github.com/VEuPathDB/FgpUtil || exit 1
cd FgpUtil

echo "Building FgpUtil"

mvn clean install 2>&1 || exit 1

mkdir -p "${dir}/vendor"
cp Util/target/fgputil-util-1.0.0.jar "${1}/vendor/fgputil-util-1.0.0.jar"
cp AccountDB/target/fgputil-accountdb-1.0.0.jar "${1}/vendor/fgputil-accountdb-1.0.0.jar"

cd "${dir}"
rm -rf FgpUtil
