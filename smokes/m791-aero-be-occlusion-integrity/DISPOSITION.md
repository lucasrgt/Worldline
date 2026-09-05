# Vanilla-query candidate rejected

This milestone is not qualified. Its expected signal is an acceptance target,
not a report of observed success. No promotion, integration or push is authorized
by the failed execution.

At Worldline `7adcf82f136d24a179d96d6834df6d58e4cd4027`, Aero
`f56c6b22ba05720d43aef10e9677343e16a15070` produced:

| Observation | Result |
| --- | --- |
| Full RGBA changed pixels | 108 (54 in each cross-session phase pair) |
| Maximum channel delta | 50 |
| Submitted model work | 178930 off, 178930 on |
| Work ratio | 1.000 |
| Candidate activation | 0 of 2 sessions |

The old pending-state interpretation is invalid. Completed vanilla queries
cannot reject this nearby fixture because Beta forces the nearest sixteen
render chunks visible. Repeated reads of a completed result do not establish
independent GPU confirmations. The implementation is formally rejected in
Aero's own catalog; no project-specific catalog is copied into Worldline.

The original log is preserved locally under
`.worldline/evidence/m791-vanilla-query-rejected/runtime.log`, SHA-256
`fa8db2a734a737cc7c169ff27c5a0faf286721ed7c7d6b0b0a73c0d30a414bf4`.
Original full-frame captures are retained beside it. Pixel differences occur
at checkpoints 0, 8, 9 and 10 (6, 16, 28 and 4 pixels per phase pair).
No culls occurred, so the raster discrepancy is an independent oracle issue.
The exact zero-pixel requirement remains unchanged.

Before a successor or general render optimization can be qualified, resolve
the oracle's cross-client ordering and warmup determinism. Reuse of this
fixture must preserve terrain depth, mixed static/animated machines, reveal
cameras, opposite order strata and full-frame comparisons. New measurements
must retain failed evidence as well as successful evidence.

## Exact-oracle follow-up

Executed Worldline: `b429a6f105c9ac49bfaa12a9ad938e26626e4ddc`.
Executed Aero: `17a1af3a7946b5b9887035eb704e4d58bf9384ff`.
Both phase comparisons are now exact: zero changed RGBA pixels and zero delta.
The gate remains red because activation and work-reduction acceptance still fail.

| Session | Off submissions | On submissions | Useful culls |
| --- | ---: | ---: | ---: |
| off-on | 121792 | 122880 | 0 |
| on-off | 122880 | 121792 | 0 |
| Total | 244672 | 244672 | 0 |

Unlike the original accounting, these totals include Cell Page model calls.
The opposite per-session changes follow acquisition phase and are not a gain.
Neither activation nor reduced work was established in both order strata.
`maximum.changed.pixels=0` and `maximum.work.ratio=0.80` remain unchanged.

The observed cause of the remaining cross-client raster noise was Beta's
HashSet-derived global BE list order. Sorting that list by Y/Z/X only in the
fixture makes both comparisons exact while preserving membership, native
dispatch and terrain depth. The earlier 86-pixel failure and full captures are
archived in `.worldline/evidence/m791-unsorted/` as the negative reproduction.
Demo population suppression and 256/256 population assertions prevent unrelated
test-mod content from contaminating either arm.

Current full evidence: `.worldline/build/smoke/m791-aero-be-occlusion-integrity/evidence.txt`,
SHA-256 `9995c3b56eac2bc26360a8d25676abd069b3d0ef9afd30259740169a3f180990`.
Runtime log SHA-256: `9142a36a75ef600e28ee6fed06436c2a62ee79847155e45f2ccac8119d65d320`.

## General optimization measurements

Each client writes one checksum-verified Worldline Profiler `.wlpr` per arm,
with 240 mixed render-update samples. Stage means in milliseconds:

| Order / arm | Render update | Combined Aero flush | BE enqueue |
| --- | ---: | ---: | ---: |
| off-on / off | 10.601 | 7.406 | 0.309 |
| off-on / on | 9.629 | 7.168 | 0.280 |
| on-off / off | 10.081 | 7.360 | 0.329 |
| on-off / on | 11.550 | 7.774 | 0.400 |

The concrete hot boundary is `WorldRendererBatchFlushMixin`'s combined
`Aero_AnimatedBatcher.flush()` / `Aero_BECellRenderer.flush()` drain. The current
observer cannot attribute the two separately. Do not choose or promote a product
optimization from the aggregate alone. Render-update maxima are 18.86-108.21 ms;
these short mixed windows include startup/camera work, exclude display and
framebuffer reads, and prove neither hitch freedom nor a throughput improvement.

Remaining integration blockers: the full Worldline gate detects
`composite refactor pin drift: m638-creeper-tnt-differential` after the shared
Extensions SDK change; the isolated branch also requires reconciliation with
main `0ddee0e5a8c9dfe214243cdc556cb18122c1e59a`. No qualification pins were
rewritten, no thresholds were relaxed, and no main merge or push was performed.

## Pareto attribution follow-up

The SDK README exception was removed from this isolated optimization branch.
Aero's authoring documentation now lives in `docs/WORLDLINE_EXTENSIONS.md`,
outside discovery. The previously qualified SDK implementation and tests are
restored byte-for-byte; no legacy qualification fingerprints were rewritten.
The general runtime gate now clears M638 and stops at the unqualified M791
schema entry, which remains a separate integration blocker.

Diagnostic overlays now measure animated and Cell Page flush durations
separately and retain exact pose/vertex reuse counters. They instrument method
entry/return without changing draw order or product behavior. These channels
remain diagnostic, with the same 240-frame mixed-window limitations above.

The first attribution acquisition at `61a59f051949` was invalid: focus loss
opened the pause menu inside the render boundary. Its 9,829,856 changed pixels
and decoded captures are retained in
`.worldline/evidence/m791-pareto-focus-contamination-61a59f051`. It is not
performance or visual qualification evidence. A fixture-only `setScreen`
guard and explicit menu/HUD capture rejection address the observed cause.

The corrected run at Worldline `0518f917d9b5` and Aero
`88adc8e6e219b2381766303dbaa4c26ee5c2199d` restored exact pixels in both
comparisons. Submissions remain 244672 -> 244672 with zero useful culls,
reproducing the existing rejection evidence digest
`9995c3b56eac2bc26360a8d25676abd069b3d0ef9afd30259740169a3f180990`.

| Order / arm | Animated flush ms | Cell Page flush ms |
| --- | ---: | ---: |
| off-on / off | 6.822 | 0.484 |
| off-on / on | 6.787 | 0.451 |
| on-off / off | 7.043 | 0.456 |
| on-off / on | 7.291 | 0.496 |

The animated boundary accounts for approximately 94 percent of these two
flush stages. The next isolated candidate therefore targets exact animated
vertex emission rather than further static-page or query-source work. This
attribution does not imply that animation pose evaluation dominates emission.
