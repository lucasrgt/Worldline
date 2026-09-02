# Contributing to Worldline

All repository artifacts must be written in English.

## Closed JDK set

| Role | Release |
| --- | --- |
| Product / mod-facing API | Java 8 (`java.release=8`) |
| Host tests and harness | JDK 21 (`test.release=21`) |
| GitHub Actions Temurin pin | `21.0.12+8` |
| Gradle TestKit plugin toolchain | JDK 17 |
| External consumer matrix | 21 and 25 |

Do not add a fourth ad-hoc CI pin. Update `quality/jdk-pins.properties` in the same change as any workflow edit.

## Canonical gate

From the repository root:

```text
java tools/harness/Gate.java
```

That is the only local and CI launch path. It owns source ceilings, module isolation, compilation with warnings as errors, and the host test suite. Do not substitute partial commands.

Runtime work that reads Minecraft must pass `java tools/harness/Gate.java --runtime`. Smoke proofs use `--smoke`.

## Tests

Host tests still register `main()` entry points in `harness.properties` `test.suites`. An additional JUnit engine (`worldline.api.WorldlineJunitEngine`) discovers `@org.junit.jupiter.api.Test` methods for IDE runs. Keep both paths green.

Shared helpers:

- SHA-256: `tools/harness/HexDigest.java`
- Eight-step column lift: `tools/harness/ColumnLift.java`
- Official dedicated-server constructor: `worldline.b173server.OfficialServerBootstrap.start` (typed factory) and `tools/harness/OfficialServerBootstrap.java` (canonical defaults)
- TestKit waits: `worldline.test.WorldlineAwait` / `Worldline.awaitPolls`

New `MessageDigest.getInstance("SHA-256")`, `for (int lift = 0; lift < 8; lift++)`, `new B173DedicatedServer(`, or `Thread.sleep` copies in `tools/` and `smokes/` fail the duplication ratchet.

## Source ceilings

Per-file limits live in `AGENTS.md` and `harness.properties`. Do not grow a file past its ceiling without a reviewed limit change in the same patch as the abstraction that removes shards.

## Official oracles

Never commit or download official Minecraft JARs onto a public GitHub-hosted runner. Acquire oracles only on trusted machines or the self-hosted `worldline-runtime` lane.

## Pull requests

Use one branch per change. Do not rewrite `main` from a worker branch. Qualification receipts are bound to an exact clean commit.
