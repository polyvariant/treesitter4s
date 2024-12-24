{
  inputs.nixpkgs.url = "github:NixOS/nixpkgs";
  inputs.flake-utils.url = "github:numtide/flake-utils";

  outputs = { nixpkgs, flake-utils, ... }:
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
      });
}
