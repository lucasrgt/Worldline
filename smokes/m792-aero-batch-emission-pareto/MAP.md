<!-- worldline-map-schema=1 -->
<!-- boundary=external-aero-bounded-draw-runs -->
<!-- nonclaims=no-occlusion-no-end-to-end-fps-no-all-platform-promotion -->

# M792 bounded adjacent draw runs map

The reused M791 fixture preserves four chunks, two machine layers, 256 static
and 256 animated models, terrain depth, exact RGBA capture and Y/Z/X native BE
dispatch order. M792 changes the candidate and acquisition contract explicitly:
occlusion stays off in both arms. Worldline discovers the public Aero extension.

Twelve checkpoints exercise three distinct scene classes: four stationary
cameras, four continuously translating cameras, and four reveal cameras with
deterministically advancing coordinate-diverse poses. The world tick remains
frozen; the fixture owns exact animation phase and replayed camera coordinates.
No LOD, geometry reduction, pose approximation or changed-pixel allowance is used.

Every camera is rendered for 120 candidate-off frames before acquisition.
Four clients run off-on, on-off, on-off, off-on. Every checkpoint holds each arm
for 180 frames, discarding 60 before measurement. The last frame of each arm
is compared exactly, totaling 48 paired full-frame comparisons. Each client
produces six checksum-verified profiler runs, one per arm and scene class.

Both order strata independently require aggregate render-update ratio <= 0.95,
stationary ratio <= 0.90, moving/advancing ratios <= 1.03, per-scene p99 <= 1.05,
allocation <= 1.02 and non-increasing 50ms render-hitch counts. Every client must
activate coalescing in the stationary candidate and report no rollback draw joins.
The packed Tessellator backing storage ceiling is 16 MiB. Failure remains evidence;
none of these thresholds may be relaxed to promote a result.

The measured boundary excludes display and framebuffer readback. A successful
contract qualifies that measured render boundary on this StationAPI host;
it cannot establish total frame latency or promotion on another platform.

Frozen trace and digest are generated below before execution.

Signal: sdk=discovered,sessions=4-counterbalanced,scenes=stationary-moving-advancing,pixels=exact48of48,draws=coalesced,render=improved,tail=guarded,allocation=guarded,staging=bounded

Trace: v2|candidate=bounded-draw-runs|sdk=public|machines=512-mixed-four-chunks|sessions=4|orders=off-on,on-off,on-off,off-on|scenes=stationary,moving,advancing-diverse|views=12|prewarm=12x120|hold=180|discard=60|pixels=exact|render-ratio=0.95|moving-advancing-ratio=1.03|p99=1.05|allocation=1.02|staging=16MiB|hitches=no-increase.

SHA-256: 2cb320b0eb57994f13225301ea1299bdcda12f7bef71c050bd03ab5efd6f1f8f.
