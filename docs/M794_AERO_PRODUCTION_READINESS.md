# M794-AERO-PRODUCTION-READINESS Aero production readiness matrix

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Diagnose differing execution times of identical current-off renderer code using selected C2 instruction bytes and CPU samples. This acquisition cannot establish production readiness.

## Qualification cycle

Six independent current-off clients use the identical Aero 2b7726e checkout and copied world. All twelve scenes, 36 complete exact captures per client, native dispatch observations and every retained profiler frame are required. Same-source image and dispatch comparisons remain exact. JFR requests Java and native samples every millisecond and retains allocation stacks and compilation events. HotSpot LogCompilation XML and selected Aero-only compiled instruction output are required. No force-inline, compile-only, optimization or background-compilation settings change. GPU drain stays disabled and its recorded duration must be zero. UTC calibration records the minimum-span startup midpoint; calibration halfspan is not an OS precision or later drift bound, and sample attribution remains approximate. The pre-existing production matrix and all 48 performance guards remain unchanged and unreachable in this acquisition-only branch. Completion writes diagnostic-disposition.txt and deliberately throws a non-qualification result. No diagnostic duration can supply a production gain or PASS receipt.

Expected signal: `sdk=discovered,sessions=12-counterbalanced,cases=12,pixels=exact,default=guarded,poses=bounded,tail=guarded,allocation=guarded`.

Frozen semantic SHA-256: `a118baa508b52849fa832e019b09ceb5d405bc61520e34c85f2476948b6816ba`.
