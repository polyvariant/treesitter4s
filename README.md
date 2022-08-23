# treesitter4s

Tree-sitter wrapper for Scala (JVM). Uses JNA to wrap the native library.

## Usage

# Work in progress note

This project is still in the oven and under active development.
Don't assume anything will work or that anything will stay as it is right now.

Feel free to try it if that's okay with you ;)

```scala
libraryDependencies ++= Seq(
  // Pure Scala interface - cross-compiled for JVM & JS platforms
  "org.polyvariant.treesitter4s" %% "core" % version,
  // Bindings for the JVM artifact. Brings in JNA and the native library.
  // You probably want to use this one.
  "org.polyvariant.treesitter4s" %% "bindings" % version,
  // Language support for a specific language.
  // There's active work to split these out to separate artifacts.
  // "org.polyvariant.treesitter4s" %% "language-scala" % version,
  // "org.polyvariant.treesitter4s" %% "language-python" % version,
)
```

## Goals

- **immutable**, Scala-friendly API
- ~~decent performance~~
- extensible language support
