<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cpu-path-differential -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=46729dc597c78fff77386086d16b7be3477ce22d5f4ec83a7ebcbe611a64b0f2 -->

# M774 Aero external Worldline CPU-path adoption behavior map

The exact Aero revision is an external TestKit consumer. Its isolated Java 8
suite compiles the platform-neutral product and runs without an official
Minecraft JAR or runtime provider. Two fresh Gradle invocations must discover
the same three tests and produce zero failures or skips.

The morph test applies 256 deterministic updates to Aero's unboxed parallel
arrays and a boxed `Map<String, Float>` reference. The scheduler tests compare
the bounded candidate to explicit visibility, distance, budget, and debt
outcomes. This qualifies adoption and semantic parity, not FPS or a platform
renderer.

Expected signal:

`consumer=aero-model-lib,testkit=0.3.1,java=8,gradle=8.14.4,runs=2,tests=3+3,morph=boxed-reference,scheduler=bounded-visible-first+debt-fair,oracle=none`

Frozen semantic SHA-256:
`46729dc597c78fff77386086d16b7be3477ce22d5f4ec83a7ebcbe611a64b0f2`.
