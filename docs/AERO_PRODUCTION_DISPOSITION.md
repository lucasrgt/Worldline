# Aero integration and production disposition

General production readiness is withheld. The candidate remains opt-in; no
release tag, push, all-platform certification or claim of an optimization ceiling
is authorized by these measurements.

## Rejected experiments

M791 at c796c870dc32636774b155526e5ddbf93bba954e rejected vanilla-query
occlusion: completed queries produced zero useful culls and no work reduction.
The retired DDA path stays retired. The independent GPU-query proposal was not
implemented: diagnostic CPU measurements prioritize packing and driver calls;
GPU elapsed time was not measured, so this is not a GPU ceiling claim.

M793 at 3a9fe325bab35c1a2b67bd4534e09f579236561c failed all eight clients'
exact oracle at the first chunk-crossing checkpoint (595 pixels each, max delta
62), including the immutable old renderer. Baseline-versus-baseline session
controls also differed (161590 and 174502 pixels). This invalidates cross-session
visual attribution; no mask or nonzero tolerance is introduced. Several mean
and tail guardrails also failed. No positive qualification pin may be imported.

Raw M793 evidence: `.worldline/evidence/m793-production-3a9fe325b.zip` in the
original qualification worktree, 83193780 bytes, SHA-256
`bb42494a5c9803b5fd58ddfdabf1b1e88d1b6e03e58a9f8de7fc167714fc535f`.
The archive contains complete RGBA frames, profiler artifacts, metrics, logs,
summary and baseline-session controls. Experimental source is retained under
`tools/experiments/aero`; reproduce at each recorded original commit.

M793 exposed accumulated frame counters with spike logging disabled and a
one-pose overhead. Aero source correction 8690e4229d8c1a871517c22fbd1e8b2f82097c90
resets counters independently and retains the original static emitter for
single-pose and default-off batches. Aero c83afe60ca0b4fbc63c8976cb53023f6dae45db2
includes the qualification limits. Verify passed 299 tests in 39 suites;
StationAPI library and integration-mod builds passed. Native ModLoader/Forge
consumer runtime is not certified by that platform gate.

## Warm successor qualification

Historical M792 at fcaa51853700e6699660cc7e795bef59a0cb653a passed exact pixels
and showed approximately 22-24% lower warmed render-update time across both
order strata. Its artifact archive SHA-256 is
`8958ce4bd30259120a85bbca50c5055a22106dccadcae7baa06c39c2e0b39390`.
That result does not qualify corrected source automatically.

The active M792 successor pins Aero c83afe60ca0b4fbc63c8976cb53023f6dae45db2.
It retains the original gain/tail/allocation/hitch limits, adds four single-pose
views, requires 64 exact paired captures, disables spike logging and owns an
RGBA8/depth24 framebuffer. Its executed outcome must be appended before integration.


M792 v6 at 5003c553a943c7cdcb56330a08c58f6bed6613ef was rejected despite
64/64 exact visual pairs and neutral single-pose means (0.9966/0.9997).
Advancing-pose on-off p99 ratio was 1.28384, above 1.05. The third client's
last moving rollback view and first advancing candidate view contained clustered
slow frames; both modes were affected. Existing wall-time data cannot attribute
this to scheduling, GC, JIT or library work, and the failure is retained.
Archive `.worldline/evidence/m792-v6-5003c553a.zip`, 15011420 bytes, SHA-256
`0958cb8577d8930b3b49d38ba75dc0cfe47e06efcb0aa81abe1ce7430ed68bcc`.

The v7 successor adds CPU/GC/JIT attribution outside the timed render boundary.
All original frames, thresholds, orders and gain requirements remain unchanged;
no noisy-frame removal or best-run selection is permitted.


## Executed v7 result and integration decision

M792 v7 qualified at Worldline `63a250170ee8343137b182116c9ae4a369b9f30d`,
tree `d5964b665af884c735e1cd896a803fd245fdf288`, against Aero
`c83afe60ca0b4fbc63c8976cb53023f6dae45db2`. Official receipt evidence SHA-256:
`4ca7ddc96917b8f9250743341fc9e2726f04b1f678499ca2b31c64b211a52ddb`.
The frozen trace digest is
`e5a46a39076ea2b32a4d71259ce41ae52ea3f37640f5b3abb341b2ddde015a57`.

| Observation | Off-on | On-off |
| --- | ---: | ---: |
| Original three-scene render-time ratio | 0.72004 | 0.78330 |
| Measured render-time reduction | 28.00% | 21.67% |
| Single-pose render-time ratio | 1.00442 | 1.00737 |
| Single-pose p99 ratio | 1.02214 | 0.94725 |
| Worst allocation ratio | 1.00025 | 1.00025 |
| 50ms render hitches, all scenes | 0 -> 0 | 0 -> 0 |

All four clients activated exact multi-pose reuse with 236 matches per measured
frame in the diverse-pose scenes. Rollback and single-pose matches were zero.
All eight order/scene guardrails passed, with 64/64 full RGBA pairs exact and
staging within 16 MiB. No frames were discarded beyond the predeclared first
60 warm frames of each 180-frame arm. CPU/GC/JIT telemetry is diagnostic only.

The complete v7 archive is `.worldline/evidence/m792-v7-63a250170.zip`,
15072278 bytes, SHA-256
`9258b06d94442f71afab02d5c09af477c1a80394a4f757d35a2721a25a03291d`.
It contains 128 raw RGBA captures, 32 profiler artifacts with summaries, metrics,
client logs, complete observations, exact milestone receipt and smoke attestation.
The original passing commit is preserved by `codex/milestone-m792-qualified-v7`.

The integration decision is to retain this bounded optimization as opt-in and
carry only this scoped passing proof through TrainPinMigration. M791, M793 and
M792 v6 remain rejected evidence. The new telemetry cannot retroactively explain
v6's intermittent tail, and its failure is not erased by the v7 pass. General
production certification and default enablement remain withheld. No push or
production tag is part of this integration.

Further optimization work needs a reproducible consumer bottleneck and an exact
oracle. Priority unresolved work is the broad chunk-crossing oracle, attribution
of intermittent tail latency, and a native ModLoader/Forge consumer qualification.
This is an explicit stopping boundary for this integration, not a claim that all
possible optimizations have been exhausted.

The later M795 package closes the declared native ModLoader/Forge source-consumer
and renderer lifecycle boundary at Aero `2b7726ee861c2f9d86a3952512857e611aa95e28`.
Its four official-client executions compare 28 complete images and exercise
display-list disposal, animation and native NBT restoration. This is a distinct
revision and contract from M792 v7; it makes no legacy performance or general
production claim. See `M795_AERO_LEGACY_CONSUMER.md` and `MAIN_CONSOLIDATION.md`.


Integration normalizes the M792 map's required frozen-trace header to its
already executed v7 signature. This documentation-only input change is carried
by the imported qualified receipt; runner/runtime source and limits are identical.
Archived fixtures live under tools/experiments/aero, outside the active smoke
and map censuses, while remaining original reviewable harness source. No failed
experiment is counted as an active or qualified behavior contract.


The active M792 Java sources were formatted for main's 120-column and unpacked
statement rules without changing those ratchets. An executable-token comparison
against qualified 63a250170 passed for all 24 Java sources, excluding comments
and whitespace. The single long TRACE literal is split into compile-time string
constants whose concatenation retains the same frozen SHA-256. This formatting
transport changes no runtime operations or performance acceptance criteria.
