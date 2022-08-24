{
  inputs.flake-utils.url = "github:numtide/flake-utils";
  inputs.flake-utils.inputs.nixpkgs.follows = "nixpkgs";

  outputs = { nixpkgs, flake-utils, ... }: flake-utils.lib.eachDefaultSystem (system:
    let pkgs = import nixpkgs { inherit system; }; in
    {
      devShells.default = pkgs.mkShell { packages = [ pkgs.nodejs ]; };

      packages.binaries = pkgs.stdenv.mkDerivation {
        name = "binaries";
        buildCommand = ''
          mkdir -p $out/lib

          function copyLib() {
            # 1 - lib derivation path, 2 - target lib name
            cp "$1/lib/$2.dylib" "$out/lib/$2.dylib"
            chmod +w "$out/lib/$2.dylib"
            install_name_tool -id $2.dylib $out/lib/$2.dylib
          }

          function copyGrammar() {
            # 1 - grammar derivation path, 2 - target lib name
            cp "$1/parser" "$out/lib/$2.dylib"
            chmod +w "$out/lib/$2.dylib"
            install_name_tool -id $2.dylib $out/lib/$2.dylib
          }

          function renameLib() {
            # 1 - library path, 2 - library name, 3 - what depends on this
            install_name_tool -change $1/lib/$2.dylib @rpath/$2.dylib $out/lib/$3.dylib
          }

          copyLib ${pkgs.tree-sitter} libtree-sitter.0.0
          renameLib ${pkgs.libiconv} libiconv libtree-sitter.0.0

          # dependency of libtree-sitter
          copyLib ${pkgs.libiconv} libiconv
          renameLib ${pkgs.libiconv} libiconv-nocharset libiconv
          renameLib ${pkgs.libiconv} libcharset.1 libiconv

          # dependenies of libiconv
          copyLib ${pkgs.libiconv} "libiconv-nocharset"
          copyLib ${pkgs.libiconv} "libcharset.1"

          # dependencies of grammars
          copyLib ${pkgs.libcxx} "libc++.1.0"
          renameLib ${pkgs.libcxxabi} libc++abi.1 libc++.1.0
          copyLib ${pkgs.libcxxabi} "libc++abi.1"

          # scala grammar
          copyGrammar ${pkgs.tree-sitter-grammars.tree-sitter-scala} libtree-sitter-scala
          renameLib ${pkgs.libcxxabi} libc++abi.1 libtree-sitter-scala
          renameLib ${pkgs.libcxx} libc++.1.0 libtree-sitter-scala

          # python grammar
          copyGrammar ${pkgs.tree-sitter-grammars.tree-sitter-python} libtree-sitter-python
          renameLib ${pkgs.libcxxabi} libc++abi.1 libtree-sitter-python
          renameLib ${pkgs.libcxx} libc++.1.0 libtree-sitter-python
        '';
      };
    });
}
