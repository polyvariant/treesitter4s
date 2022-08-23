#!/bin/bash

set -e

SUFFIX=""
# if $SYSTEM contains darwin, suffix is dylib
if [[ "$SYSTEM" == *darwin* ]]; then
    SUFFIX="dylib"
else
    SUFFIX="so"
fi

nix build "nixpkgs#legacyPackages.$SYSTEM.tree-sitter"
cp -L "result/lib/libtree-sitter.$SUFFIX" "bindings/src/main/resources/libtree-sitter.$SUFFIX"
chmod +w "bindings/src/main/resources/libtree-sitter.$SUFFIX"

nix build "nixpkgs#legacyPackages.$SYSTEM.tree-sitter-grammars.tree-sitter-scala"
cp -L result/parser "bindings/src/main/resources/tree-sitter-scala.$SUFFIX"
chmod +w "bindings/src/main/resources/tree-sitter-scala.$SUFFIX"

nix build "nixpkgs#legacyPackages.$SYSTEM.tree-sitter-grammars.tree-sitter-python"
cp -L result/parser "bindings/src/main/resources/tree-sitter-python.$SUFFIX"
chmod +w "bindings/src/main/resources/tree-sitter-python.$SUFFIX"
