{
  inputs.nixpkgs.url = "github:NixOS/nixpkgs";
  inputs.flake-utils.url = "github:numtide/flake-utils";

  outputs = { self, nixpkgs, flake-utils, ... }:
    flake-utils.lib.eachDefaultSystem (system:
      let pkgs = import nixpkgs { inherit system; };
      in {
        devShells.default = pkgs.mkShell {
          packages = [ pkgs.sbt pkgs.tree-sitter pkgs.nodejs ];
        };

        packages.tree-sitter = pkgs.stdenv.mkDerivation {
          name = "tree-sitter";
          src = pkgs.fetchFromGitHub {
            owner = "tree-sitter";
            repo = "tree-sitter";
            rev = "v0.24.3";
            sha256 = "sha256-2Pg4D1Pf1Ex6ykXouAJvD1NVfg5CH4rCQcSTAJmYwd4=";
          };
          buildPhase = "make";
          installPhase =
            if system == "x86_64-darwin" || system == "aarch64-darwin" then ''
              cp libtree-sitter.dylib $out
            '' else ''
              cp libtree-sitter.so $out
            '';
        };

        packages.tree-sitter-python = pkgs.stdenv.mkDerivation {
          name = "tree-sitter-python";
          src = pkgs.fetchFromGitHub {
            owner = "tree-sitter";
            repo = "tree-sitter-python";
            rev = "v0.23.2";
            sha256 = "sha256-cOBG2xfFJ0PpR1RIKW1GeeNeOBA9DAP/N4RXRGYp3yw=";
          };
          buildPhase = "make";
          installPhase =
            if system == "x86_64-darwin" || system == "aarch64-darwin" then ''
              cp libtree-sitter-python.dylib $out
            '' else ''
              cp libtree-sitter-python.so $out
            '';
        };

        packages.tree-sitter-funlang = pkgs.stdenv.mkDerivation {
          name = "tree-sitter-funlang";
          src = ./tree-sitter-funlang;
          buildInputs = [ pkgs.tree-sitter pkgs.nodejs ];
          buildPhase = ''
            tree-sitter generate
            cc src/parser.c -shared -o $out
          '';
          dontInstall = true;
        };
        packages.tree-sitter-funlang-all = pkgs.stdenv.mkDerivation {
          name = "tree-sitter-funlang-all";
          src = ./tree-sitter-funlang;
          dontBuild = true;
          installPhase = ''
            mkdir $out
            cd $out
            mkdir darwin-aarch64 && cp ${self.packages.aarch64-darwin.tree-sitter-funlang} darwin-aarch64/libtree-sitter-funlang.dylib
            mkdir darwin-x86-64 && cp ${self.packages.x86_64-darwin.tree-sitter-funlang} darwin-x86-64/libtree-sitter-funlang.dylib
            mkdir linux-aarch64 && cp ${self.packages.aarch64-linux.tree-sitter-funlang} linux-aarch64/libtree-sitter-funlang.so
            mkdir linux-x86-64 && cp ${self.packages.x86_64-linux.tree-sitter-funlang} linux-x86-64/libtree-sitter-funlang.so
          '';
        };
      });
}
