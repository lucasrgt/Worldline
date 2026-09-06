# M794-AERO-PRODUCTION-READINESS Aero production readiness matrix

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Diagnose GPU queue carryover with separately reported completion wait and CPU/JIT attribution. This diagnostic acquisition does not establish production readiness.

## Qualification cycle

This temporary diagnostic branch deliberately fails closed before the production matrix. Six independent clients run historical-off/current-off/current-on and the reverse order with the same MixinExtras 0.5.0 and physical CPU profile. Every client retains all twelve scenes, 36 complete exact captures, native dispatch probes and checksum-verified profiler frames. Separate JFR recordings contain allocation stacks, Java and native CPU samples, compilation, deoptimization and CPU-load events. HotSpot LogCompilation XML is retained. Every acquisition and warming frame drains prior GPU commands before render timing. The gpudrain channel records all glFinish wait and renderwithdrain records its sum with render time. The same fixture boundary applies to all six arms. No product code gains a GPU barrier. Profiler metadata calibrates Instant UTC against the midpoint of the shortest of sixteen monotonic samples and records calibration halfspan, which is not a bound on OS clock precision or later drift. No injected stage hooks are enabled in these CPU acquisitions. These instrumented durations cannot establish a gain. All existing production performance guards remain unchanged and are never reached in this acquisition-only branch. Completion writes diagnostic-disposition.txt and throws an explicit non-qualification result, so no production PASS receipt can be created.

Expected signal: `sdk=discovered,sessions=12-counterbalanced,cases=12,pixels=exact,default=guarded,poses=bounded,tail=guarded,allocation=guarded`.

Frozen semantic SHA-256: `e537a91896dbe19bd8b3f17e8fbea58067876ac144213394bc2c84c19dabbf9b`.
