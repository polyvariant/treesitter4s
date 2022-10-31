#!/bin/bash

set -e

RESULT=$(nix build ".#grammars" --no-link --print-out-paths --print-build-logs)
# copy, flattening links
cp --no-preserve=mode,ownership -Lr "$RESULT"/* .
