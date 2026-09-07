<!-- worldline-map-schema=1 -->
<!-- boundary=external-aero-exact-multi-pose -->
<!-- nonclaims=no-occlusion-no-end-to-end-fps-no-all-platform-promotion -->
<!-- frozen-trace=a68eea695489267dd3d50c76ae9ef68a5db5afcad79fab035bc2a0a7c2413d5b -->

# M792 exact multi-pose reuse map

Requalification pins Aero `9abdcc51272f5e844ace6619116aab0b5d5af8b0` after
isolating multi-pose draw dispatch and restoring the original fallback emitter.
The model/camera acquisition protocol and every numerical threshold remain
unchanged; the current trace also binds visible presentation and verified
process scheduling.
The earlier receipt does not qualify this changed external implementation.

The reused M791 fixture preserves four chunks, two machine layers, 256 static
and 256 animated models, terrain depth, exact RGBA capture and Y/Z/X native BE
dispatch order. M792 changes the candidate and acquisition contract explicitly:
occlusion stays off in both arms. Worldline discovers the public Aero extension.

Sixteen checkpoints exercise four distinct scene classes: four stationary
cameras, four continuously translating cameras, and four reveal cameras with
deterministically advancing coordinate-diverse poses. The world tick remains
frozen; the fixture owns exact animation phase and replayed camera coordinates.
No LOD, geometry reduction, pose approximation or changed-pixel allowance is used.

Every camera is rendered for 120 frames per arm before acquisition, including
advancing reveal poses. This contract measures warmed rendering; it does not
qualify cold activation. Animated and page allocation channels attribute costs.
Four clients run off-on, on-off, on-off, off-on. Every checkpoint holds each arm
for 180 frames, discarding 60 before measurement. The last frame of each arm
is compared exactly, totaling 64 paired full-frame comparisons. Each client
produces eight checksum-verified profiler runs, one per arm and scene class.

Both order strata independently require aggregate render-update ratio <= 0.95,
stationary ratio <= 0.90, moving/advancing ratios <= 1.03, per-scene p99 <= 1.05,
allocation <= 1.02 and non-increasing 50ms render-hitch counts. Every client must
activate exact pose sharing in the stationary candidate and report no rollback matches.
The packed Tessellator plus candidate scratch storage ceiling is 16 MiB. Failure remains evidence;
none of these thresholds may be relaxed to promote a result.

The measured boundary excludes display and framebuffer readback. A successful
contract qualifies that measured render boundary on this StationAPI host;
it cannot establish total frame latency or promotion on another platform.

Frozen trace and digest are generated below before execution.

Signal: sdk=discovered,sessions=4-counterbalanced,scenes=stationary-moving-advancing,pixels=exact64of64,poses=shared,render=improved,tail=guarded,allocation=guarded,staging=bounded

Trace: v10|candidate=exact-multi-pose-reuse|sdk=public|machines=512-mixed-four-chunks|sessions=4|orders=off-on,on-off,on-off,off-on|scenes=stationary,moving,advancing-diverse|views=16|prewarm=16x2x120|hold=180|discard=60|pixels=exact|render-ratio=0.95|moving-advancing-ratio=1.03|p99=1.05|allocation=1.02|staging=16MiB|hitches=no-increase|surface=owned-rgba8-depth24|spikelog=off|counter-reset=independent|single-pose=legacy-ratio1.03-p99-1.05|attribution=thread-cpu-gc-jit-no-frame-exclusion|host=windows-high-four-physical-vm4|window=post-capture-preview|aero=2b7726e.

SHA-256: a68eea695489267dd3d50c76ae9ef68a5db5afcad79fab035bc2a0a7c2413d5b.

The reconciled runner owns an RGBA8/depth24 framebuffer with the native depth
precision. Spike logging is off and frame counter reset is independent.
Historical M791 and M793 experiments remain explicitly rejected, outside the
active qualification catalog; this contract retains its original warm scope.

The final four views hold one exact synchronized pose. Both arms use the
original static representative emitter and report zero multi-pose matches.
This scene separately requires mean ratio <= 1.03, p99 <= 1.05, allocation
<= 1.02 and non-increasing hitches; it is excluded from the original three-scene
five-percent aggregate gain requirement. No historical threshold is relaxed.

The controlled Windows host profile applies before world loading and is verified
from each client log. The shared implementation is explicitly fingerprinted.
No frame exclusion or acceptance threshold changes with this host profile.
After timing and exact readback, each client presents its owned framebuffer in the native window and reports view progress in the title. 

The controlled host selects one logical processor on each of four distinct physical
cores and fixes JVM ActiveProcessorCount at four. The observed log must confirm
logical=4, physical=4, vm=4 and High priority before world loading.
