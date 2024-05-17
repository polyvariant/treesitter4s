{
  inputs.flake-utils.url = "github:numtide/flake-utils";

  outputs = { self, nixpkgs, flake-utils, ... }:

    let
      lib = self.lib;
      mkCommons = system:
        let
          pkgs = import nixpkgs { inherit system; };
          suffix = pkgs.callPackage lib.lib-suffix { };
          make-lib-name = name: version: if pkgs.stdenv.isDarwin then "${name}${version}${suffix}" else "${name}${suffix}${version}";
        in
        pkgs.stdenv.mkDerivation {
          name = "commons";

          buildCommand =
            if pkgs.stdenv.isDarwin then
              ''
                mkdir -p $out/lib

                function copyLib() {
                  # 1 - lib derivation path, 2 - target lib name
                  cp "$1/lib/$2" "$out/lib/$2"
                  chmod +w "$out/lib/$2"
                  install_name_tool -id $2 $out/lib/$2
                }

                function renameLib() {
                  # 1 - library path, 2 - library name, 3 - what depends on this
                  install_name_tool -change $1/lib/$2 @loader_path/$2 $out/lib/$3
                }

                copyLib ${pkgs.tree-sitter} libtree-sitter.0.0.dylib
                renameLib ${pkgs.libiconv} libiconv.dylib libtree-sitter.0.0.dylib

                copyLib ${pkgs.libiconv} libiconv.dylib
                renameLib ${pkgs.libiconv} libiconv-nocharset.dylib libiconv.dylib
                renameLib ${pkgs.libiconv} libcharset.1.dylib libiconv.dylib

                copyLib ${pkgs.libiconv} libiconv-nocharset.dylib
                copyLib ${pkgs.libiconv} libcharset.1.dylib

                copyLib ${pkgs.libcxx} libc++.1.0.dylib
                renameLib ${pkgs.libcxxabi} libc++abi.1.dylib libc++.1.0.dylib
                copyLib ${pkgs.libcxxabi} libc++abi.1.dylib
              '' else
              ''
                mkdir -p $out/lib

                function copyLib() {
                  # 1 - lib derivation path, 2 - target lib name
                  cp "$1/lib/$2" "$out/lib/$2"
                }

                cp "${pkgs.tree-sitter}/lib/libtree-sitter.so.0.0" "$out/lib/libtree-sitter.so.0.0"
                cp "${pkgs.glibc}/lib/libc.so.6" "$out/lib/libc.so.6"
                cp "${pkgs.glibc}/lib/libgcc_s.so.1" "$out/lib/libgcc_s.so.1"
                cp "${pkgs.glibc}/lib/libm.so.6" "$out/lib/libm.so.6"
                cp "${pkgs.gcc-unwrapped.lib}/lib/libstdc++.so.6" "$out/lib/libstdc++.so.6"
              '';
        };
    in
    flake-utils.lib.eachDefaultSystem
      (system:
        let
          pkgs = import nixpkgs { inherit system; };

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
            {
              name = "modules/core/.jvm/src/main/resources";
              path = pkgs.callPackage lib.make-grammar-resources {
                package = mkCommons;
              };
            }
          ];

          packages.ts-scala = ts-scala-generic { rename-dependencies = true; };
          packages.ts-python = ts-python-generic { rename-dependencies = true; };
        }) // {
      lib = import ./lib.nix;
    };
}
