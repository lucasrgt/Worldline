<!-- worldline-map-schema=1 -->
<!-- boundary=external-aero-production-matrix -->
<!-- nonclaims=no-occlusion-no-end-to-end-fps-no-all-platform-promotion -->
<!-- frozen-trace=e537a91896dbe19bd8b3f17e8fbea58067876ac144213394bc2c84c19dabbf9b -->
# M794 production qualification map

The public Extensions SDK discovers both immutable Aero checkouts. Twelve fresh
clients run four counterbalanced triplets of baseline-off, current-off, and current-on.
The twelve cases and predeclared guardrails are frozen in smoke.properties.
Fixture code controls inputs through public playback and renderer interfaces;
the baseline receives no replacement product classes. The actual production property is fixed at startup. No hook intercepts enabled()
and no process changes modes. Profiles use the shared Worldline Profiler.

Five terrain-only views settle with zero dirty visible chunks and eight identical
complete-frame samples spaced fifteen frames apart; entity/model submission stays canceled. First
render includes cold renderer work; fixture preparation is outside timing.
Every JVM retains all 180 first-case cold frames. Later cases warm 300 model frames
at their fixed initial camera, reset playback/model identity, then discard sixty
of 180 observed frames. Three captures per case verify exact complete images.
Default-off comparisons additionally check the pre-refactor implementation.
No pixel threshold may be relaxed. Failed cases remain failed evidence.

Lighting changes rebuild actual Cell Pages; the fixture also replaces a slab
block. The final case crosses a chunk coordinate boundary, teleports away and
reveals the upper layer. It does not claim forced chunk unload or world replacement.

Signal: sdk=discovered,sessions=12-counterbalanced,cases=12,pixels=exact,default=guarded,poses=bounded,tail=guarded,allocation=guarded

Trace: v12-gpu-quiescence-acquisition|sdk=public|aero=current41dc5a8,baseline88adc8e|triplets=Boff-Coff-Con,Con-Coff-Boff,Con-Coff-Boff,Boff-Coff-Con|sessions=12|cases=cold,1,20,64,128,256,transition,custom,nested,identity,blocks,chunks|hold=180|discard=60-except-cold|terrain-prewarm=5-views-plus-each-case-all-chunks-exact8x15-no-models|model-warm=300-fixed-view-except-cold|captures=3-per-case|pixels=exact|saturation128-256=fallback|gain20-64=0.90|other=1.03|p99=1.05|allocation=1.02|staging=16MiB|hitches=no-increase|diagnostic=separate-inclusive-stage-cpu|surface=owned-rgba8-depth24|vanilla-occlusion=off|spikelog=off|chunk-sort=arm-reset|textures=arsenic-seed173-32ticks|jvm=fixed-production-mode-cpu-gc-jit|terrain=snapshot-after-timing|window=post-capture-preview|host=windows-high-four-physical-vm4|fixture=direct-renderer-primitive-light|allocation-diagnostic=preflight-baseline-off-current-off-jfr-separate|chunk-dispatch=registered-pixels-vanilla-null-success-failure-returns|mixinextras=0.5.0-both-arms|cpu-acquisition=6-counterbalanced-jfr-jit-logs|gpu-entry=finish-separate-drain-and-inclusive|clock=instant-midpoint-minspan16|production-qualification=not-attempted.

SHA-256: e537a91896dbe19bd8b3f17e8fbea58067876ac144213394bc2c84c19dabbf9b.

A thirteenth, separate diagnostic client receives stage hooks omitted from qualification
clients. Its inclusive CPU call durations are attribution, never gain evidence.

Every client draws the complete world into a private RGBA8/depth24 framebuffer,
independent of window visibility. Pixel and depth precision remain exact; empty
alpha or constant captures fail. Display swap remains outside the timed boundary.

Native translucent pixels retain their actual alpha and must match exactly. The
empty-frame check rejects fully zero alpha or constant RGB, not valid transparency.
Advanced OpenGL terrain queries and spike logging are off in both arms; M791
retains its separate advanced-OpenGL occlusion contract.

Each case resets the native previous-camera sort sentinel and initial chunk-array
ordering. Vanilla then applies its own displacement threshold and chunk sorting.

Every case prepares its starting camera until the entire native chunk ring is clean and eight complete RGBA frames match. Model submission is canceled during this input preparation. Arsenic water and lava binders use a seeded, bounded native update clock. Vanilla distance comparison is unchanged.

The corrected source restores the immutable original static emitter and routes
multiple poses through a separate named-group emission loop. Both 128- and
256-phase cases must report zero candidate matches after index saturation.

The native window previews the owned framebuffer only after timing and exact capture.
Case progress appears in the window title. This prevents stale loading-screen
back buffers from looking like an endless load. No capture or metric is substituted.

The controlled Windows host profile applies before world loading and is verified
from each client log. The shared implementation is explicitly fingerprinted.
No frame exclusion or acceptance threshold changes with this host profile.
The fixture registers a direct consumer renderer once through the StationAPI renderer event and adjusts brightness through a primitive return value; no cancellable callback is allocated for each model or brightness sample. 

Two allocation diagnostic clients precede the qualification matrix and record JFR
allocation events with stacks. They compare historical and current default-off
renderers across the same twelve scenes, using copied worlds and exact captures.
The recordings must contain allocation stacks. These instrumented profiles never
enter M794Performance; the twelve qualification clients have no Flight Recorder
flags. The shared template contains no measured model work.

The controlled host selects one logical processor on each of four distinct physical
cores and fixes JVM ActiveProcessorCount at four. The observed log must confirm
logical=4, physical=4, vm=4 and High priority before world loading.

A registered chunk-baked mesh is included in the exact framebuffer oracle. After
allocation preflight captures, native dispatch probes compare vanilla and null
behavior, successful registered meshes, and controlled bake failure returns.
The current client must report activation of the composable dispatch wrapper.

Both arms pin MixinExtras 0.5.0 and verify its runtime service marker. This
qualifies the optional composable wrapper under that explicit dependency
condition. MixinExtras 0.3.2 selects the preserved legacy callback; its completed
v9 preflight is retained as fallback behavior evidence, not wrapper evidence.

This temporary acquisition-only branch runs six counterbalanced CPU/JIT clients,
retains their exact images and native return probes, then deliberately reports
non-qualification before the production matrix. It cannot issue a production
PASS receipt. Java/native samples and compilation logs are attribution only.

The GPU-quiescence diagnostic drains prior commands before each acquisition frame,
including warmup. gpudrain retains the complete wait; renderwithdrain includes it.
The product remains unchanged. UTC calibration uses the minimum-span midpoint
of sixteen startup pairs; recorded halfspan excludes OS precision and clock drift.
