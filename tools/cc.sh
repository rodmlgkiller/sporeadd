#!/bin/bash
# compile and summarise errors: tools/cc.sh <logfile>
cd "$(dirname "$0")/.."
JAVA_HOME=$HOME/.jdks/ms-21.0.12.1 ./gradlew compileJava --console=plain -q > "$1" 2>&1
echo "errors: $(grep -c 'error:' "$1")"
