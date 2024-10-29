#!/usr/bin/env bash

set -e

# core
mkdir -p core/.jvm/src/main/resources/darwin-aarch64
BINARY_PATH=$(nix build .#packages.aarch64-darwin.tree-sitter --no-link --print-out-paths --print-build-logs)
cp $BINARY_PATH core/.jvm/src/main/resources/darwin-aarch64/libtree-sitter.dylib
chmod 755 core/.jvm/src/main/resources/darwin-aarch64/libtree-sitter.dylib

mkdir -p core/.jvm/src/main/resources/darwin-x86-64
BINARY_PATH=$(nix build .#packages.x86_64-darwin.tree-sitter --no-link --print-out-paths --print-build-logs)
cp $BINARY_PATH core/.jvm/src/main/resources/darwin-x86-64/libtree-sitter.dylib
chmod 755 core/.jvm/src/main/resources/darwin-x86-64/libtree-sitter.dylib

mkdir -p core/.jvm/src/main/resources/linux-aarch64
BINARY_PATH=$(nix build .#packages.aarch64-linux.tree-sitter --no-link --print-out-paths --print-build-logs)
cp $BINARY_PATH core/.jvm/src/main/resources/linux-aarch64/libtree-sitter.so
chmod 755 core/.jvm/src/main/resources/linux-aarch64/libtree-sitter.so

mkdir -p core/.jvm/src/main/resources/linux-x86-64
BINARY_PATH=$(nix build .#packages.x86_64-linux.tree-sitter --no-link --print-out-paths --print-build-logs)
cp $BINARY_PATH core/.jvm/src/main/resources/linux-x86-64/libtree-sitter.so
chmod 755 core/.jvm/src/main/resources/linux-x86-64/libtree-sitter.so

# bindingsPython

mkdir -p bindingsPython/.jvm/src/main/resources/darwin-aarch64
BINARY_PATH=$(nix build .#packages.aarch64-darwin.tree-sitter-python --no-link --print-out-paths --print-build-logs)
cp $BINARY_PATH bindingsPython/.jvm/src/main/resources/darwin-aarch64/libtree-sitter-python.dylib
chmod 755 bindingsPython/.jvm/src/main/resources/darwin-aarch64/libtree-sitter-python.dylib

mkdir -p bindingsPython/.jvm/src/main/resources/darwin-x86-64
BINARY_PATH=$(nix build .#packages.x86_64-darwin.tree-sitter-python --no-link --print-out-paths --print-build-logs)
cp $BINARY_PATH bindingsPython/.jvm/src/main/resources/darwin-x86-64/libtree-sitter-python.dylib
chmod 755 bindingsPython/.jvm/src/main/resources/darwin-x86-64/libtree-sitter-python.dylib

mkdir -p bindingsPython/.jvm/src/main/resources/linux-aarch64
BINARY_PATH=$(nix build .#packages.aarch64-linux.tree-sitter-python --no-link --print-out-paths --print-build-logs)
cp $BINARY_PATH bindingsPython/.jvm/src/main/resources/linux-aarch64/libtree-sitter-python.so
chmod 755 bindingsPython/.jvm/src/main/resources/linux-aarch64/libtree-sitter-python.so

mkdir -p bindingsPython/.jvm/src/main/resources/linux-x86-64
BINARY_PATH=$(nix build .#packages.x86_64-linux.tree-sitter-python --no-link --print-out-paths --print-build-logs)
cp $BINARY_PATH bindingsPython/.jvm/src/main/resources/linux-x86-64/libtree-sitter-python.so
chmod 755 bindingsPython/.jvm/src/main/resources/linux-x86-64/libtree-sitter-python.so
