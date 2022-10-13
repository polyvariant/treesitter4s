#!/bin/bash

set -e

function update() {
    SYSTEM="$1"
    RESOURCE_DIR="$2"

    nix build ".#packages.$SYSTEM.binaries"

    RESOURCE_SUFFIX="src/main/resources/$RESOURCE_DIR"
    RESOURCE_PATH="bindings/$RESOURCE_SUFFIX"
    mkdir -p "$RESOURCE_PATH"
    for file in c++.1.0 c++abi.1 charset.1 iconv-nocharset iconv tree-sitter.0.0; do
        cp result/lib/lib$file."$3" "$RESOURCE_PATH"
    done
    chmod +w "$RESOURCE_PATH"/*

    mkdir -p "bindingsScala/$RESOURCE_SUFFIX"
    cp result/lib/libtree-sitter-scala."$3" "bindingsScala/$RESOURCE_SUFFIX"
    chmod +w "bindingsScala/$RESOURCE_SUFFIX"/*

    mkdir -p "bindingsPython/$RESOURCE_SUFFIX"
    cp result/lib/libtree-sitter-python."$3" "bindingsPython/$RESOURCE_SUFFIX"
    chmod +w "bindingsPython/$RESOURCE_SUFFIX"/*
}

update aarch64-darwin darwin-aarch64 dylib
update x86_64-darwin darwin-x86-64 dylib
# update "x86_64-linux" "linux-x86-64" "so"
# update "aarch64-linux" "linux-aarch64" "so"
