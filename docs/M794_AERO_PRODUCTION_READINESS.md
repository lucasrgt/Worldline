# M794-AERO-PRODUCTION-READINESS Aero production readiness matrix

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Collect separate CPU and JIT attribution for renderer regressions. This diagnostic acquisition does not establish production readiness.

## Qualification cycle

This temporary diagnostic branch deliberately fails closed before the production matrix. Six independent clients run historical-off/current-off/current-on and the reverse order with the same MixinExtras 0.5.0 and physical CPU profile. Every client retains all twelve scenes, 36 complete exact captures, native dispatch probes and checksum-verified profiler frames. Separate JFR recordings contain allocation stacks, Java and native CPU samples, compilation, deoptimization and CPU-load events. HotSpot LogCompilation XML is retained. Profiler metadata pairs monotonic and epoch clocks for sample-to-frame attribution. No injected stage hooks are enabled in these CPU acquisitions. These instrumented durations cannot establish a gain. All existing production performance guards remain unchanged and are never reached in this acquisition-only branch. Completion writes diagnostic-disposition.txt and throws an explicit non-qualification result, so no production PASS receipt can be created.

Expected signal: `sdk=discovered,sessions=12-counterbalanced,cases=12,pixels=exact,default=guarded,poses=bounded,tail=guarded,allocation=guarded`.

Frozen semantic SHA-256: `9ccb8711e61fcbdbaf01d38955b2b34a66536e64a992905f6f48c364adfc617e`.
