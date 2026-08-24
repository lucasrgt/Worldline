# Consuming Worldline TestKit

Worldline TestKit is an isolated Java 8 test surface for Beta 1.7.3 mods. It
does not upgrade or compile a mod's legacy build. The runner and Gradle plugin
use JDK 21, while test sources remain ordinary Java 8.

## Supported versions

| Component | Supported release | Contract |
| --- | --- | --- |
| Worldline TestKit | 0.3.0 | Immutable tag `testkit-v0.3.0` |
| Worldline core | 1.463.x | Release tip M625; stable behavior identities, not milestone numbers |
| Minecraft oracle | Beta 1.7.3 | Hash-verified official client; server only for server scenarios |
| Spec bytecode | Java 8 | `--release 8`, warnings as errors |
| Runner JDK | 21 | Tested with the repository-pinned CI toolchain |
| Gradle | 8.14.4 | Wrapper generated and checksum pinned by `init` |

The release contains `worldline-test-api-0.3.0.jar` for authoring and
`worldline-test-runner-0.3.0.jar` for discovery and execution. Both artifacts
are bound to generated SHA-256 values. Official Minecraft JARs are never
bundled; provide legitimate copies through the ignored local oracle directory
or a private CI artifact.

## Create a project

```text
java -jar worldline-test-runner-0.3.0.jar init
tests/worldline/gradlew.bat worldlineDoctor worldlineTest
```

Use `./gradlew` instead of `gradlew.bat` on Linux or macOS. For host-only tests
that do not touch Minecraft, initialize with `--host-only`. For runtime tests,
place the hash-matching client JAR at
`tests/worldline/.local/oracles/b1.7.3/minecraft.jar`. Runtime acquisition is
always explicit, and runtime sessions remain serialized by the project-local
lease.

## CI

The repository includes a reusable composite action. A consumer workflow can
download a private oracle artifact and then run the isolated suite:

```yaml
name: Worldline TestKit
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    timeout-minutes: 60
    steps:
      - uses: actions/checkout@v5
      - uses: actions/download-artifact@v4
        with:
          name: minecraft-b1.7.3-oracle
          path: tests/worldline/.local/oracles/b1.7.3
      - uses: lucasrgt/Worldline/.github/actions/worldline-test@testkit-v0.3.0
        with:
          project-directory: tests/worldline
      - if: always()
        uses: actions/upload-artifact@v4
        with:
          name: worldline-test-results
          path: |
            tests/worldline/build/worldline/results
            tests/worldline/build/test-results/worldlineTest
```

For a host-only suite, omit the oracle download and set `noRuntime.set(true)`
in `tests/worldline/build.gradle.kts`. The complete configuration, oracle
precedence, templates, and migration path are documented in
[`GRADLE_TESTKIT.md`](GRADLE_TESTKIT.md).
