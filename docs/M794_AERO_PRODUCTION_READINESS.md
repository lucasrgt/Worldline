# M794-AERO-PRODUCTION-READINESS Aero production readiness matrix

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Diagnose differing execution times of identical current-off renderer code using selected C2 instruction bytes without JFR. This acquisition cannot establish production readiness.

## Qualification cycle

Six independent current-off clients use the identical Aero 2b7726e checkout and copied world. All twelve scenes, 36 complete exact captures per client, native dispatch observations and every retained profiler frame are required. Same-source image and dispatch comparisons remain exact. JFR is not started: no execution sampling, allocation recording or JFR compilation recording is enabled. The runner rejects JFR files and recording startup signals. HotSpot LogCompilation XML and selected Aero-only compiled instruction output are required. No force-inline, compile-only, optimization or background-compilation settings change. GPU drain stays disabled and its recorded duration must be zero. UTC calibration records the minimum-span startup midpoint; calibration halfspan is not an OS precision or later drift bound, and calibration does not provide sampled attribution in this branch. The pre-existing production matrix and all 48 performance guards remain unchanged and unreachable in this acquisition-only branch. Completion writes diagnostic-disposition.txt and deliberately throws a non-qualification result. No diagnostic duration can supply a production gain or PASS receipt.

Expected signal: `sdk=discovered,sessions=12-counterbalanced,cases=12,pixels=exact,default=guarded,poses=bounded,tail=guarded,allocation=guarded`.

Frozen semantic SHA-256: `292f4a816b3e4cd3df0f9df064050031118190a0a2099f460e410e1a5133e5b6`.
