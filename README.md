# treesitter4s

Tree-sitter wrapper for Scala (JVM). Uses JNA to wrap the native library.

## Usage

# Work in progress note

This project is still in the oven and under active development.
Don't assume anything will work or that anything will stay as it is right now.

Feel free to try it if that's okay with you ;)

```scala
libraryDependencies ++= Seq(
  // Base interface - cross-compiled for JVM & JS platforms.
  // Use %%% instead of %% for JS/Native.
  "org.polyvariant.treesitter4s" %% "core" % version,
  // Language support for a specific language.
  // "org.polyvariant.treesitter4s" %% "language-scala" % version,
  // "org.polyvariant.treesitter4s" %% "language-python" % version,
)
```

## Goals

- **immutable**, read-only, Scala-friendly API
- complete, 1-1 native/Java interface via [JNA](https://github.com/java-native-access/jna)
- binary convenience: no dealing with native libraries if you're on a supported system
- extensible language support

## Supported systems

### JVM

Support can vary, but the following platforms are considered supported:

- macOS x86_64
- macOS aarch64
- Linux x86_64
- Linux aarch64

CI runs on x86_64 macOS/Linux machines. Development is currently done on an aarch64 Mac.
linux-aarch64 binaries are included thanks to the magic of [Nix](https://nixos.org/) and [Nixbuild](https://nixbuild.net/), but the library isn't being tested on that platform.

### Native

All platforms are supported as long as you provide the binaries. TODO

### JS

All platforms are supported as long as you provide the binaries.
