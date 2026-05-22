{
  description = "Dev shell for contributing to the Renaissance JVM benchmark suite";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          config.allowUnfree = false;
        };

        jdk = pkgs.jdk25;
        sbt = pkgs.sbt;
      in
      {
        devShells.default = pkgs.mkShell {
          packages = with pkgs; [
            jdk
            sbt
            git
            curl
            gnumake
            which
            bashInteractive

            hyperfine
            perf-tools
            perf

            jq
            yq-go
          ];

          env = {
            JAVA_HOME = "${jdk}";
            SBT_OPTS = "-Dsbt.boot.directory=.sbt/boot -Dsbt.ivy.home=.ivy2";
          };

          shellHook = ''
            export PATH="$JAVA_HOME/bin:$PATH"

            alias build="tools/sbt/bin/sbt renaissancePackage"
            run() {
              local jar=$(find target -name 'renaissance-gpl*.jar' | head -n 1)
              if [[ -f "$jar" ]]; then
                echo "Found built JAR: $jar"
                echo "Running with options: $@"
                java -jar "$jar" "$@"
              else
                echo "JAR not found. Please run 'build' first."
              fi
            }

            cat <<'EOF'
            Renaissance dev shell
            =====================
            Available tools:
              - java / javac  : ${jdk.version}
              - sbt           : ${sbt.version or "installed"}
              - git, curl
              - perf, hyperfine
              - jq, yq

            Typical workflow:
              build       - Build the Renaissance benchmark suite JAR.
              run [args]  - Run the benchmark suite with optional arguments.`
            EOF
          '';
        };
      });
}
