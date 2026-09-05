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
`smokes/experiments/aero`; reproduce at each recorded original commit.

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
