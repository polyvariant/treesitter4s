let
  rename-grammar =
    { lib
    , stdenv
    , libcxxabi
    , libcxx
    , grammar
    , pname
    , rename-dependencies ? true
    }:
    stdenv.mkDerivation {
      inherit pname;
      inherit (grammar) version;
      src = grammar;
      buildPhase =
        if stdenv.isDarwin then ''
          install_name_tool -id lib${pname}.dylib parser
        '' + (
          if rename-dependencies then ''
            install_name_tool -change ${libcxxabi}/lib/libc++abi.1.dylib @loader_path/libc++abi.1.dylib parser
            install_name_tool -change ${libcxx}/lib/libc++.1.0.dylib @loader_path/libc++.1.0.dylib parser
          '' else ""
        )
        else "true";
      installPhase =
        let suffix = (lib.systems.elaborate stdenv.system).extensions.sharedLibrary; in
        ''
          mkdir -p $out/lib
          cp parser $out/lib/lib${pname}${suffix}
        '';
    };

  make-grammar-resources =
    { lib
    , linkFarm
    , stdenv
    , package
    , system-mappings ? {
        "darwin-x86-64" = "x86_64-darwin";
        "darwin-aarch64" = "aarch64-darwin";
        "linux-x86-64" = "x86_64-linux";
        "linux-aarch64" = "aarch64-linux";
      }
    }: linkFarm "${(package stdenv.system).name}-all" (
      lib.mapAttrsToList
        (jna-system: nix-system: {
          name = "${jna-system}";
          path = "${package nix-system}/lib";
        })
        system-mappings
    );
in

{ inherit rename-grammar make-grammar-resources; }

