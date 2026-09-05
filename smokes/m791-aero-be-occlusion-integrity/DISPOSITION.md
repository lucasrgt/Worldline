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
