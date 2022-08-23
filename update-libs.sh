#!/bin/bash

set -e

function update() {
    SYSTEM="$1"
    RESOURCE_DIR="$2"
    SUFFIX="$3"

    RESOURCE_PATH="bindings/src/main/resources/$RESOURCE_DIR"
    mkdir -p "$RESOURCE_PATH"

    nix build "nixpkgs#legacyPackages.$SYSTEM.tree-sitter"
    cp -L "result/lib/libtree-sitter.$SUFFIX" "$RESOURCE_PATH/libtree-sitter.$SUFFIX"
    chmod +w "$RESOURCE_PATH/libtree-sitter.$SUFFIX"

    nix build "nixpkgs#legacyPackages.$SYSTEM.tree-sitter-grammars.tree-sitter-scala"
    cp -L result/parser "$RESOURCE_PATH/libtree-sitter-scala.$SUFFIX"
    chmod +w "$RESOURCE_PATH/libtree-sitter-scala.$SUFFIX"

    nix build "nixpkgs#legacyPackages.$SYSTEM.tree-sitter-grammars.tree-sitter-python"
    cp -L result/parser "$RESOURCE_PATH/libtree-sitter-python.$SUFFIX"
    chmod +w "$RESOURCE_PATH/libtree-sitter-python.$SUFFIX"

}

update "aarch64-darwin" "darwin-aarch64" "dylib"
update "x86_64-darwin" "darwin-x86-64" "dylib"
update "x86_64-linux" "linux-x86-64" "so"
