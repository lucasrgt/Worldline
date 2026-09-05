# M793-AERO-PRODUCTION-QUALIFICATION Aero production qualification matrix

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Qualify exact multi-pose reuse and the default renderer against the immutable previous implementation across twelve production cases.

## Qualification cycle

Eight fresh StationAPI clients run baseline-current-current-baseline version groups, each with off-on and on-off arms. The immutable baseline is 88adc8e6e219b2381766303dbaa4c26ee5c2199d. Cases cover first render, 1/20/64/128/256 exact phases, a 400-tick transition, custom playback, nested skeleton, model identity replacement, block and lighting changes, and chunk-boundary traversal with teleport reveal. Only terrain warms for 240 frames; model submission is canceled during that interval. Each case runs 180 frames per arm and records 120 after discarding 60, except first-render profiles retain all 180. Cold comparisons use only the first arm of fresh JVMs against the corresponding baseline version and order. Three complete RGBA captures per case and arm must match exactly, including current-off versus historical-off. Each version/order stratum requires mean render ratio at most 1.03, p99 at most 1.05, allocation at most 1.02 and non-increasing 50ms hitches. The 20- and 64-phase candidate cases additionally require at least ten percent mean render reduction in both orders. Saturation, transitions, custom playback and nested skeletons must not produce candidate matches. Changed lighting must rebuild real Cell Pages. Staging remains bounded at 16 MiB. All original Worldline Profiler artifacts and framebuffer bytes are retained. Timing excludes display, readback and fixture preparation; no all-platform or end-to-end latency claim is made.

Expected signal: `sdk=discovered,sessions=8-counterbalanced,cases=12,pixels=exact,default=guarded,poses=bounded,tail=guarded,allocation=guarded`.

Frozen semantic SHA-256: `372c3a31d7788ce48d63f6823d310100c1be99e1a6b0aa552a174209ee07ad5e`.
