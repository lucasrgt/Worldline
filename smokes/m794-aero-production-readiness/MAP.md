<!-- worldline-map-schema=1 -->
<!-- boundary=external-aero-production-matrix -->
<!-- nonclaims=no-occlusion-no-end-to-end-fps-no-all-platform-promotion -->
<!-- frozen-trace=282052dccbc706efede87f608e9ffbe0bf76224517256c4ee7adeda2ad8c4718 -->
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

Trace: v4|sdk=public|aero=currenta3079d7,baseline88adc8e|triplets=Boff-Coff-Con,Con-Coff-Boff,Con-Coff-Boff,Boff-Coff-Con|sessions=12|cases=cold,1,20,64,128,256,transition,custom,nested,identity,blocks,chunks|hold=180|discard=60-except-cold|terrain-prewarm=5-views-plus-each-case-all-chunks-exact8x15-no-models|model-warm=300-fixed-view-except-cold|captures=3-per-case|pixels=exact|gain20-64=0.90|other=1.03|p99=1.05|allocation=1.02|staging=16MiB|hitches=no-increase|diagnostic=separate-inclusive-stage-cpu|surface=owned-rgba8-depth24|vanilla-occlusion=off|spikelog=off|chunk-sort=arm-reset|textures=arsenic-seed173-32ticks|jvm=fixed-production-mode-cpu-gc-jit|terrain=snapshot-after-timing

SHA-256: 282052dccbc706efede87f608e9ffbe0bf76224517256c4ee7adeda2ad8c4718.

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
