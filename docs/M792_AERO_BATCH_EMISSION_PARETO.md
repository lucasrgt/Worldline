# M792-AERO-BATCH-EMISSION-PARETO Aero exact batch emission Pareto qualification

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Four fresh counterbalanced StationAPI clients preserve exact pixels while qualifying bounded exact animated emission replay across stationary cameras, continuously moving cameras, and deterministically advancing diverse animation poses.

## Qualification cycle

M792 reuses the corrected M791 four-chunk 512-machine fixture and its stable native BE submission order, disables demo population and occlusion, prevents focus-loss menus, and controls poses explicitly. Four clients execute off-on, on-off, on-off, off-on orders across twelve cameras with 180 frames per arm and the first 60 discarded. Each of three scene classes retains 480 samples per arm per client in checksum-verified Worldline Profiler artifacts. Both order strata must reduce aggregate render-update time by at least five percent and stationary time by ten percent, keep moving and advancing ratios at or below 1.03, p99 at or below 1.05, allocation at or below 1.02, and 50ms render-hitch counts non-increasing. Candidate replay must activate in every client, rollback hits must be zero, and retained cache payload and keys must remain within 16 MiB. Render timing excludes display and framebuffer reads; this contract makes no FPS, end-to-end latency, GPU-memory, or all-platform claim.

Expected signal: `sdk=discovered,sessions=4-counterbalanced,scenes=stationary-moving-advancing,pixels=exact48of48,emission=reused,render=improved,tail=guarded,allocation=guarded,cache=bounded`.

Frozen semantic SHA-256: `6a99e03d5760fb84d7ae62e31aa191d5b992e76b8e1ce13739c8bef20d7bf101`.
