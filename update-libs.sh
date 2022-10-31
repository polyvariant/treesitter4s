#!/bin/bash

set -e

mkdir -p "modules/language-$1/.jvm/src/main/resources/"

RESULT=$(nix build ".#$1-grammar-all" --no-link --print-out-paths --print-build-logs)
# copy, flattening links
cp --no-preserve=mode,ownership -Lr "$RESULT"/* .
