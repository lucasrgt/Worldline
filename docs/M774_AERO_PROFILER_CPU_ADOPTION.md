# M774-AERO-PROFILER-CPU-ADOPTION AeroModelLib external Worldline CPU-path adoption

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

AeroModelLib consumes the packaged Java 8 Worldline TestKit 0.3.1 API from an isolated external build and checks its maintained morph-array and chunk-scheduler CPU paths against independent reference behavior without a Minecraft runtime.

## Qualification cycle

M774 runs the exact Aero revision twice with Gradle 8.14.4, the included Worldline 0.3.1 plugin, and hash-pinned API and runner artifacts; both runs must discover and pass the same three host-only differentials and emit zero failures, skips, or runtime sessions.

Expected signal: `consumer=aero-model-lib,testkit=0.3.1,java=8,gradle=8.14.4,runs=2,tests=3+3,morph=boxed-reference,scheduler=bounded-visible-first+debt-fair,oracle=none`.

Frozen semantic SHA-256: `46729dc597c78fff77386086d16b7be3477ce22d5f4ec83a7ebcbe611a64b0f2`.
