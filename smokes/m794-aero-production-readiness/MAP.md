<!-- worldline-map-schema=1 -->
<!-- boundary=external-aero-production-matrix -->
<!-- nonclaims=no-production-gain-no-qualification-no-gpu-drain -->
<!-- frozen-trace=292f4a816b3e4cd3df0f9df064050031118190a0a2099f460e410e1a5133e5b6 -->
# M794 same-source assembly diagnosis without JFR

This temporary branch runs six independent current-off JVMs at Aero 2b7726e.
The public Extensions SDK verifies the exact checkout and extension. All clients
use the same copied world, MixinExtras 0.5.0 and the four-physical-core host profile.
Each retains twelve scenes, 36 owned RGBA8/depth24 captures, native dispatch
observations and checksum-verified profiler frames. Every image and return probe
must match the first client exactly. No frame or timing outlier is discarded.

The original 180-frame acquisition retains all cold frames and the final 120
frames of later scenes after their existing warmup. World, model, lighting,
chunk, camera and display-preview actions remain unchanged.

JFR is not started. Execution samples, allocation stacks and JFR compilation
events are absent. Recording files and startup signals are explicitly rejected. LogCompilation XML and instruction dumps of
selected Aero methods are retained. No compiler optimization, forced inlining,
compilation-only filter or background-compilation control is changed.
GPU completion is not forced; the drain metric must remain zero.

The UTC startup calibration uses the minimum-span midpoint of sixteen pairs.
Its halfspan excludes OS clock precision and later drift. There is no JFR
sample attribution in this branch.

The historical twelve-client production matrix and all 48 numeric guards remain
unchanged below the diagnostic stop. This branch deliberately reports
non-qualification after acquisition and cannot issue a production PASS receipt.
The following signal describes that unreachable production contract.

Signal: sdk=discovered,sessions=12-counterbalanced,cases=12,pixels=exact,default=guarded,poses=bounded,tail=guarded,allocation=guarded

Trace: v16-assembly-without-jfr-acquisition|sdk=public|aero=current2b7726e,baseline88adc8e|triplets=Boff-Coff-Con,Con-Coff-Boff,Con-Coff-Boff,Boff-Coff-Con|sessions=12|cases=cold,1,20,64,128,256,transition,custom,nested,identity,blocks,chunks|hold=180|discard=60-except-cold|terrain-prewarm=5-views-plus-each-case-all-chunks-exact8x15-no-models|model-warm=300-fixed-view-except-cold|captures=3-per-case|pixels=exact|saturation128-256=fallback|gain20-64=0.90|other=1.03|p99=1.05|allocation=1.02|staging=16MiB|hitches=no-increase|diagnostic=separate-inclusive-stage-cpu|surface=owned-rgba8-depth24|vanilla-occlusion=off|spikelog=off|chunk-sort=arm-reset|textures=arsenic-seed173-32ticks|jvm=fixed-production-mode-cpu-gc-jit|terrain=snapshot-after-timing|window=post-capture-preview|host=windows-high-four-physical-vm4|fixture=direct-renderer-primitive-light|allocation-diagnostic=preflight-baseline-off-current-off-jfr-separate|chunk-dispatch=registered-pixels-vanilla-null-success-failure-returns|mixinextras=0.5.0-both-arms|cpu-acquisition=6-current-off-aa-jit-assembly-no-jfr|gpu-entry=no-drain|clock=instant-midpoint-minspan16|samples=disabled-jfr-not-started|assembly=selected-aero-only|production-qualification=not-attempted.

SHA-256: 292f4a816b3e4cd3df0f9df064050031118190a0a2099f460e410e1a5133e5b6.
