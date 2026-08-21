# Gradle adoption guide

Worldline TestKit 0.2 uses one isolated modern Gradle build at
`tests/worldline`. It does not replace or upgrade a legacy mod build. Tests are
ordinary Java files named `*WorldlineTest.java` under
`tests/worldline/src/test/java`; there is no custom `.wltest.java` language.
This gives editors standard formatting, syntax highlighting, completion,
navigation, refactoring, breakpoints, and Gradle source-set import.

## Initialize

From a Worldline 0.2 runner distribution:

```text
java -jar worldline-test-runner-0.2.0.jar init
cd tests/worldline
./gradlew worldlineDoctor
./gradlew worldlineTest
```

On Windows use `gradlew.bat`. `init` creates and pins a Gradle 8.14.4 wrapper,
the plugin declaration, configuration, source/resource/snapshot directories,
a sample test, and this ignored oracle drop zone:

```text
tests/worldline/.local/oracles/b1.7.3/
|-- minecraft.jar          official client oracle
`-- minecraft_server.jar   optional official server oracle
```

The directory exists immediately but the JARs never do. Copy your legally
obtained official JAR into it, or run the explicit, hash-verified client-only
acquisition task:

```text
./gradlew worldlineAcquireRuntime
./gradlew worldlineAcquireRuntime -Pworldline.acquireServer=true
```

Nothing downloads an oracle as a side effect of compilation or testing.

## Commands

| Task | Purpose |
| --- | --- |
| `worldlineDoctor` | Check JDK, sources, oracle identity, and Git safety without running Minecraft |
| `worldlineTest` | Compile Java 8 specs and run the compact tree reporter plus JUnit XML |
| `worldlineTestList` | Discover and list tests without starting Minecraft |
| `worldlineTestInspect` | Print the immutable collected plan |
| `worldlineTestWatch` | Interactive Vitest-like rerun shell |
| `worldlineTestUpdateSnapshots` | Explicitly accept external snapshot changes |
| `worldlineTestMinimize` | Reproduce and delta-minimize named steps |
| `worldlineMappings` | Show promoted semantic selectors and evidence |
| `worldlineVerifyOracle` | Verify configured official JAR hashes |
| `worldlineConfigure` | Write a named global oracle profile |

`check` depends on `worldlineTest`. Runtime sessions remain serial and use
`tests/worldline/.local/official-runtime.lock`; Gradle parallelism cannot make
official Minecraft sessions concurrent.

## Configuration and precedence

The generated `worldline.toml` is intentionally small:

```toml
runtime = "b1.7.3"
profile = "b173-local"
noRuntime = "false"
loader = "modloader"
```

Oracle locations resolve in this order:

1. Gradle properties `worldline.clientJar`, `worldline.serverJar`, and
   `worldline.retroMcpRoot`;
2. `WORLDLINE_CLIENT_JAR`, `WORLDLINE_SERVER_JAR`, and
   `WORLDLINE_RETROMCP_ROOT`;
3. the selected profile in `~/.worldline/config.toml`;
4. `WORLDLINE_ARTIFACT_ROOT/b1.7.3`;
5. the repository-local ignored drop zone.

Example global profile:

```toml
[profiles.b173-local]
clientJar = "D:/minecraft-oracles/b1.7.3/minecraft.jar"
serverJar = "D:/minecraft-oracles/b1.7.3/minecraft_server.jar"
retroMcp = "D:/minecraft-oracles/b1.7.3/retromcp-java"
```

Configured paths do not define identity. The frozen client/server byte counts
and SHA-256 values do, and both `doctor` and runtime gates fail on mismatch.
An official JAR tracked by the containing Git repository is also a hard error.

The plugin downloads its API and runner from the immutable
`testkit-v0.2.0` GitHub release and verifies frozen SHA-256 values. Offline
builds can set `-Pworldline.distributionDir=/path/to/testkit`; both exact JARs
must be present and valid there.

## Product discovery

The isolated build conservatively recognizes conventional Gradle outputs plus
the established BetaEnergistics and Butter build directories. Exactly one mod
JAR may be selected automatically. Ambiguous output fails with a list rather
than guessing. Override any legacy layout explicitly:

```kotlin
worldline {
    productClasspath.from("../../out/classes")
    modFiles.from("../../dist/example-mod.jar")
}
```

Product code is never compiled as part of the Worldline source set. Specs see
it only through the configured classpath.

## Templates and migration

Initialization templates are `basic`, `storage`, `gui`, `optimization`, and
`multiplayer`:

```text
java -jar worldline-test-runner-0.2.0.jar init --template=storage
java -jar worldline-test-runner-0.2.0.jar init --host-only
```

Migrate the earlier `worldline-tests` convention without deleting it:

```text
java -jar worldline-test-runner-0.2.0.jar migrate
java -jar worldline-test-runner-0.2.0.jar doctor tests/worldline
```

Migration copies sources and snapshots, preserves the legacy configuration,
and leaves the original tree for comparison. Remove it only after the Gradle
suite passes.

## CI

The repository includes `.github/actions/worldline-test`. Supply the official
oracle as a private CI artifact or secret-managed file; never commit it:

```yaml
- uses: actions/checkout@v4
- uses: ./path/to/worldline-action
  with:
    project-directory: tests/worldline
```

The task writes machine-readable JUnit XML to
`tests/worldline/build/test-results/worldlineTest` and failure evidence under
`tests/worldline/build/worldline/results`. Upload both with `if: always()`.

## Release model

The binary plugin is `dev.worldline.test`. Tag `testkit-v0.2.0` runs the
canonical Worldline gate, Gradle TestKit functional tests, plugin validation,
Gradle Plugin Portal publication, and a GitHub release containing hash-pinned
API and runner JARs. Publication fails closed when release credentials are
absent; local builds cannot masquerade as a public release.
