#!/bin/bash
# jp.sh <fully.qualified.Class> [javap flags]: javap against Minecraft+NeoForge (patched) and the libs/ jars
cd "$(dirname "$0")/.."
MC="$HOME/.gradle/caches/neoformruntime/intermediate_results/compiledWithNeoForge_3c469a8246adf2cdd928f836473c55643fb0f8e2_output.jar"
NEO=$(ls "$HOME"/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/21.1.252/*/neoforge-21.1.252-universal.jar | head -1)
MCW=$(cygpath -m "$MC"); NEOW=$(cygpath -m "$NEO")
CP="$MCW;$NEOW;$(ls libs/*.jar | tr "
" ";")"
cls="$1"; shift
"$HOME/.jdks/ms-21.0.12.1/bin/javap" -cp "$CP" "$@" "$cls"
