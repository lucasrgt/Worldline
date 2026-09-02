# FAQ

## What is Worldline?

A laboratory for executing the real Minecraft Beta 1.7.3 client and dedicated server under controlled conditions. Official JARs are the behavioral oracles.

## How do I verify a change?

Run `java tools/harness/Gate.java` from the repository root. That is the canonical gate.

## Why are there so many `M*_*.md` files in `docs/`?

Milestone narratives stay at stable root paths so receipts and external links do not churn. Grouped indexes live in `docs/milestones`, `docs/features`, `docs/performance`, and `docs/project`.

## Why does Gate recompute smoke fingerprints?

Each smoke has a behavior-input identity. When a suite will execute 0 smokes and the current-tree receipt already shows `executed=0` for this tree, fingerprint work is skipped. A forced recompute still exists for cache misses.

## Can I write tests in JUnit?

Yes. Annotate public no-arg methods with `@org.junit.jupiter.api.Test` and run `worldline.api.WorldlineJunitEngine`. Gate still requires a `main()` suite listed in `harness.properties` `test.suites`.

## Where did spider-daylight and powered-creeper API types go?

They live in `worldline.api.scenario`. General entity, event, weather, and N-client queries live in `worldline.api.query`. `ApiSurfaceDoctor` fails closed if that split regresses.

## Why is `modules/testapi` not in package `worldline.testkit`?

Authoring types belong in `worldline.testapi`. The TestKit runner stays in package `worldline.testkit` under `modules/testkit`.

## Does the public consumer workflow download Minecraft?

No. Official-client acquire on GitHub-hosted public runners is forbidden. Oracle-backed consumer tests belong on the self-hosted runtime lane.
