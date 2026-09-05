# M792-AERO-BATCH-EMISSION-PARETO Aero exact multi-pose Pareto qualification

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Four fresh counterbalanced StationAPI clients preserve exact pixels while qualifying bounded exact multi-pose reuse across stationary cameras, continuously moving cameras, and deterministically advancing diverse animation poses.

## Qualification cycle

This reconciled qualification uses an owned RGBA8/depth24 framebuffer so window visibility cannot invalidate readback. Aero spike logging is off; renderer frame counters reset independently. M792 reuses the corrected M791 four-chunk 512-machine fixture and its stable native BE submission order, disables demo population and occlusion, prevents focus-loss menus, and controls poses explicitly. Before acquisition, every camera is rendered for 120 frames with each arm, including advancing poses in reveal views, to finish terrain and exercise both code paths before steady-state acquisition. Four clients execute off-on, on-off, on-off, off-on orders across sixteen cameras with 180 frames per arm and the first 60 discarded. Each of four scene classes retains 480 samples per arm per client in checksum-verified Worldline Profiler artifacts. Both order strata must reduce aggregate render-update time by at least five percent and stationary time by ten percent, keep moving and advancing ratios at or below 1.03, p99 at or below 1.05, allocation at or below 1.02, and 50ms render-hitch counts non-increasing. Exact multi-pose reuse must activate in every client, rollback matches must be zero, and packed Tessellator plus candidate scratch storage must remain within 16 MiB. The candidate admits at most 64 exact bundle, clip and raw-time identities per dense batch and caps additional transformed-vertex scratch at 1 MiB. Saturation, custom playbacks and transitions use the ordinary emitter. Separate animated and page allocation channels attribute regressions. Four additional reveal views exercise one synchronized pose. They require render ratio at most 1.03, p99 at most 1.05, allocation at most 1.02, no increased hitch count and zero multi-pose matches in both arms. The original three-scene aggregate five-percent improvement requirement is unchanged and excludes this new guardrail scene. The original static representative emitter is retained for default-off and one-pose batches. The measured contract concerns warmed rendering; cold activation and the earlier failed acquisitions remain outside any promotion claim. Render timing excludes display and framebuffer reads; this contract makes no FPS, end-to-end latency, total-memory, or all-platform claim.

Expected signal: `sdk=discovered,sessions=4-counterbalanced,scenes=stationary-moving-advancing,pixels=exact64of64,poses=shared,render=improved,tail=guarded,allocation=guarded,staging=bounded`.

Frozen semantic SHA-256: `6864ab4e6013de864936ed26a7817f45ce1592f567b12b5f2ba76124c39c3417`.
