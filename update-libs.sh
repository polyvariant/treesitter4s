#!/bin/bash

set -e

function updateGrammar() {
    SYSTEM="$1"
    RESOURCE_DIR="$2"
    SUFFIX="$3"
    GRAMMAR="$4"

    nix build "nixpkgs#legacyPackages.$SYSTEM.tree-sitter-grammars.$GRAMMAR"
    cp -L result/parser "$RESOURCE_PATH/lib$GRAMMAR.$SUFFIX"
    chmod +w "$RESOURCE_PATH/lib$GRAMMAR.$SUFFIX"
}

function update() {
    SYSTEM="$1"
    RESOURCE_DIR="$2"
    SUFFIX="$3"

    nix build ".#packages.$SYSTEM.binaries"

    RESOURCE_PATH="bindings/src/main/resources/$RESOURCE_DIR"
    mkdir -p "$RESOURCE_PATH"
    cp result/lib/* "$RESOURCE_PATH"
    chmod +w "$RESOURCE_PATH"/*
}

update aarch64-darwin darwin-aarch64 dylib
update x86_64-darwin darwin-x86-64 dylib
# update "x86_64-linux" "linux-x86-64" "so"
# update "aarch64-linux" "linux-aarch64" "so"
