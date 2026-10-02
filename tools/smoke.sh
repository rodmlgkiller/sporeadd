#!/bin/bash
# smoke.sh client|server [seconds]: run the dev client/server briefly and print the first mixin/mod-loading problems
cd "$(dirname "$0")/.."
kind="${1:-server}"; secs="${2:-150}"
log=/tmp/smoke_$kind.log
JAVA_HOME=$HOME/.jdks/ms-21.0.12.1 timeout "$secs" ./gradlew run${kind^} --console=plain > "$log" 2>&1
grep -n "Mixin apply\|InvalidInjection\|Critical injection\|Caused by: org.spongepowered\|FATAL\|/ERROR\]" "$log" | grep -v "RecipeManager\|Negative index" | head -${3:-14} | cut -c1-600
grep -c "Done (" "$log" | sed 's/^/server-done-count: /'
