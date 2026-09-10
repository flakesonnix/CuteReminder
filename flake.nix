{
  description = "Paper 1.26.2 template plugin — JDK 21 + Gradle";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
        # JDK 21 — Paper 1.26.2 requires Java 21
        jdk = pkgs.jdk21;
        gradle = pkgs.gradle_8;
      in {
        devShells.default = pkgs.mkShell {
          name = "paper-template";
          buildInputs = [ jdk gradle pkgs.git pkgs.bash ];

          shellHook = ''
            export JAVA_HOME=${jdk}
            echo "Paper template — java $(java -version 2>&1 | head -n1) | gradle $(gradle --version | grep Gradle)"
            echo "  gradle build  → build/libs/template-plugin-1.0.0.jar"
          '';
        };

        # `nix build` → plugin jar (gradle build without wrapper)
        packages.default = pkgs.stdenv.mkDerivation {
          pname = "template-plugin";
          version = "1.0.0";
          src = ./.;
          nativeBuildInputs = [ jdk gradle ];
          buildPhase = ''
            export GRADLE_USER_HOME=$TMPDIR/.gradle
            gradle --no-daemon -x test build
          '';
          installPhase = ''
            mkdir -p $out
            cp build/libs/*.jar $out/ 2>/dev/null || cp -r build $out/
          '';
        };
      });
}
