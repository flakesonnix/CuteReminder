{
  description = "Paper 1.26.2 Kotlin template — JDK 21 + Gradle + HikariCP";

  nixConfig = {
    sandbox = "relaxed";
  };

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
    }:
    flake-utils.lib.eachSystem
      [
        "x86_64-linux"
        "aarch64-linux"
        "aarch64-darwin"
      ]
      (
        system:
        let
          pkgs = import nixpkgs {
            inherit system;
            config.allowUnfree = true;
          };

          jdk = pkgs.jdk21;
          gradle = pkgs.gradle_8;

          idea = if pkgs.jetbrains ? idea then pkgs.jetbrains.idea else pkgs.jetbrains.idea-community;

          nixFmt = pkgs.nixfmt;
          ktlint = pkgs.ktlint;
        in
        {
          devShells.default = pkgs.mkShell {
            name = "paper-template";

            packages = [
              jdk
              gradle
              pkgs.git
              pkgs.bash
              idea
              nixFmt
              ktlint
            ];

            shellHook = ''
              unset JAVA_HOME
              unset JDK_HOME

              export JAVA_HOME="${jdk}"
              export PATH="${jdk}/bin:$PATH"

              echo "Paper Kotlin template"
              echo "  Java:   $(java -version 2>&1 | head -n1)"
              echo "  Gradle: $(gradle --version | grep '^Gradle ' | head -n1)"
              echo ""
              echo "Commands:"
              echo "  gradle build"
              echo "  gradle shadowJar"
              echo "  gradle spotlessApply"
              echo "  gradle spotlessCheck"
              echo "  nix fmt"
              echo "  gradle idea"
              echo ""
              echo "IDEA:"
              echo "  nix develop -c idea ."
            '';
          };

          packages.default = pkgs.stdenv.mkDerivation {
            pname = "template-plugin";
            version = "1.0.0";

            src = ./.;

            nativeBuildInputs = [
              jdk
              gradle
              pkgs.cacert
            ];

            __noChroot = true;

            buildPhase = ''
              export JAVA_HOME="${jdk}"
              export PATH="${jdk}/bin:$PATH"
              export GRADLE_USER_HOME="$TMPDIR/.gradle"
              export HOME="$TMPDIR"

              gradle --no-daemon -x test build
            '';

            installPhase = ''
              mkdir -p "$out"

              cp build/libs/*.jar "$out/" 2>/dev/null \
                || cp -r build "$out/"
            '';
          };

          packages.idea = idea;

          formatter = nixFmt;
        }
      );
}
