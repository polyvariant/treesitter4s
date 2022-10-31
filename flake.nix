{
  inputs.flake-utils.url = "github:numtide/flake-utils";

  outputs = { self, nixpkgs, flake-utils, ... }:

    let
      mkDarwinBinaries = system:
        let
          pkgs = import nixpkgs { inherit system; }; in
        pkgs.stdenv.mkDerivation {
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
              install_name_tool -change $1/lib/$2.dylib @loader_path/$2.dylib $out/lib/$3.dylib
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
    in
    flake-utils.lib.eachDefaultSystem
      (system:
        let
          pkgs = import nixpkgs { inherit system; };
          lib = self.lib;

          ts-scala = pkgs.callPackage lib.rename-grammar {
            pname = "tree-sitter-scala";
            grammar = pkgs.tree-sitter-grammars.tree-sitter-scala;
            rename-dependencies = false;
          };
          ts-python = pkgs.callPackage lib.rename-grammar {
            pname = "tree-sitter-python";
            grammar = pkgs.tree-sitter-grammars.tree-sitter-python;
            rename-dependencies = false;
          };
        in
        {
          devShells.default = pkgs.mkShell {
            buildInputs = [ pkgs.jre pkgs.nodejs pkgs.yarn pkgs.sbt pkgs.binutils ];
            nativeBuildInputs = [
              pkgs.tree-sitter
              ts-scala
              ts-python
              pkgs.clang
            ];
            shellHook = ''
              export DYLD_LIBRARY_PATH="${pkgs.lib.makeLibraryPath [ts-scala ts-python]}"
            '';
          };
        }) // {
      packages.aarch64-darwin.binaries = mkDarwinBinaries "aarch64-darwin";
      packages.x86_64-darwin.binaries = mkDarwinBinaries "x86_64-darwin";
      packages.x86_64-linux.ts =
        let pkgs = import nixpkgs { system = "x86_64-linux"; }; in
        pkgs.tree-sitter;
      packages.x86_64-linux.ts-scala =
        let pkgs = import nixpkgs { system = "x86_64-linux"; }; in
        pkgs.tree-sitter-grammars.tree-sitter-scala;
      packages.x86_64-linux.ts-python =
        let pkgs = import nixpkgs { system = "x86_64-linux"; }; in
        pkgs.tree-sitter-grammars.tree-sitter-python;

      packages.aarch64-linux.ts =
        let pkgs = import nixpkgs { system = "aarch64-linux"; }; in
        pkgs.tree-sitter;
      packages.aarch64-linux.ts-scala =
        let pkgs = import nixpkgs { system = "aarch64-linux"; }; in
        pkgs.tree-sitter-grammars.tree-sitter-scala;
      packages.aarch64-linux.ts-python =
        let pkgs = import nixpkgs { system = "aarch64-linux"; }; in
        pkgs.tree-sitter-grammars.tree-sitter-python;
    } // {
      lib = import ./lib.nix;
    };
}
