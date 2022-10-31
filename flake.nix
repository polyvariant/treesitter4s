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
          '';
        };
    in
    flake-utils.lib.eachDefaultSystem
      (system:
        let
          pkgs = import nixpkgs { inherit system; };
          lib = self.lib;

          ts-scala-generic = { rename-dependencies }: pkgs.callPackage lib.rename-grammar {
            pname = "tree-sitter-scala";
            grammar = pkgs.tree-sitter-grammars.tree-sitter-scala;
            inherit rename-dependencies;
          };
          ts-scala-shell = ts-scala-generic { rename-dependencies = false; };

          ts-python-generic = { rename-dependencies }: pkgs.callPackage lib.rename-grammar {
            pname = "tree-sitter-python";
            grammar = pkgs.tree-sitter-grammars.tree-sitter-python;
            inherit rename-dependencies;
          };

          ts-python-shell = ts-python-generic { rename-dependencies = false; };
        in
        {
          devShells.default = pkgs.mkShell {
            buildInputs = [ pkgs.jre pkgs.nodejs pkgs.yarn pkgs.sbt pkgs.binutils ];
            nativeBuildInputs = [
              pkgs.tree-sitter
              ts-scala-shell
              ts-python-shell
              pkgs.clang
            ];
            shellHook = ''
              export DYLD_LIBRARY_PATH="${pkgs.lib.makeLibraryPath [ts-scala-shell ts-python-shell]}"
            '';
          };
          packages.grammars = pkgs.linkFarm "grammars" [
            {
              name = "modules/language-scala/.jvm/src/main/resources";
              path = pkgs.callPackage lib.make-grammar-resources {
                package = system: self.packages.${system}.ts-scala;
              };
            }
            {
              name = "modules/language-python/.jvm/src/main/resources";
              path = pkgs.callPackage lib.make-grammar-resources {
                package = system: self.packages.${system}.ts-python;
              };
            }
          ];

          packages.ts-scala = ts-scala-generic { rename-dependencies = true; };
          packages.ts-python = ts-python-generic { rename-dependencies = true; };
        }) // {
      lib = import ./lib.nix;
    };
}
