# M792-AERO-BATCH-EMISSION-PARETO Aero bounded batch draw Pareto qualification

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Four fresh counterbalanced StationAPI clients preserve exact pixels while qualifying bounded adjacent named-group draw coalescing across stationary cameras, continuously moving cameras, and deterministically advancing diverse animation poses.

## Qualification cycle

M792 reuses the corrected M791 four-chunk 512-machine fixture and its stable native BE submission order, disables demo population and occlusion, prevents focus-loss menus, and controls poses explicitly. Before acquisition, every camera is rendered for 120 frames with the candidate off to finish terrain preparation. Four clients execute off-on, on-off, on-off, off-on orders across twelve cameras with 180 frames per arm and the first 60 discarded. Each of three scene classes retains 480 samples per arm per client in checksum-verified Worldline Profiler artifacts. Both order strata must reduce aggregate render-update time by at least five percent and stationary time by ten percent, keep moving and advancing ratios at or below 1.03, p99 at or below 1.05, allocation at or below 1.02, and 50ms render-hitch counts non-increasing. Coalescing must activate in every client, rollback joins must be zero, and packed Tessellator backing storage must remain within 16 MiB. Runs contain at most 65536 vertices, except an individually larger group keeps its original standalone draw. Render timing excludes display and framebuffer reads; this contract makes no FPS, end-to-end latency, total-memory, or all-platform claim.

Expected signal: `sdk=discovered,sessions=4-counterbalanced,scenes=stationary-moving-advancing,pixels=exact48of48,draws=coalesced,render=improved,tail=guarded,allocation=guarded,staging=bounded`.

Frozen semantic SHA-256: `2cb320b0eb57994f13225301ea1299bdcda12f7bef71c050bd03ab5efd6f1f8f`.
