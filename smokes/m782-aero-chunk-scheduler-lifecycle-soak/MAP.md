<!-- worldline-map-schema=1 -->
<!-- boundary=external-aero-chunk-scheduler-lifecycle -->
<!-- nonclaims=gpu-performance,minecraft-visual-latency -->
<!-- frozen-trace=4a0d5d1e57b9845e4b80fde1e9934b73b61e50d11e31b37629be5f5f06188e54 -->

# M782 Aero chunk scheduler lifecycle soak behavior map

The qualified boundary is Aero's production chunk-work scheduler and the
StationAPI world-transition hook at revision
`31dfe0c03b0ee454bd3996cec0bb76705f52835b`.

The cycle pins and hashes the scheduler, compile-budget facade, lifecycle
Mixin, and external probe. It builds the StationAPI remapped artifact, compiles
the exact core scheduler for Java 8, and runs the same soak in two fresh JVMs.

Each JVM executes 256 epochs. Every epoch begins with 128 hidden chunks, admits
one visible chunk for each of 512 frames, processes exactly one item per call,
and drains all 640 items. Hidden work must wait no more than 160 frames. After
drain, the probe seeds sixteen pending identities, verifies they are retained,
calls production `reset()`, and requires zero state entries, a null active
queue, invocation zero, and reset metrics.

This proves bounded fairness, complete drainage, deterministic repeatability,
and the scheduler-side release contract. It does not launch Minecraft, measure
GPU or frame performance, or independently quantify player-visible chunk
latency; the real-client promotion gate remains separate.

Frozen signal:

`consumer=aero-model-lib,revision=31dfe0c03b0ee454bd3996cec0bb76705f52835b,platform=stationapi-remapJar,compile=javac-release8,jvms=2-fresh,epochs=256,frames=163840,rebuilds=164096,hidden-wait=157<=160,transitions=256,pending-before-reset=16,reset=states0+queue-null+invocation0`

Frozen trace:

`v1|consumer=aero-model-lib|revision=31dfe0c03b0ee454bd3996cec0bb76705f52835b|platform=stationapi-remapJar|compile=javac-release8|jvms=2-fresh|epochs=256|arrival-frames=512|hidden-per-epoch=128|budget=1|max-age=120|debt=30|hidden-wait<=160|transition-pending=16|reset=states0+queue-null+invocation0|oracle=exact`

Frozen SHA-256: `4a0d5d1e57b9845e4b80fde1e9934b73b61e50d11e31b37629be5f5f06188e54`.
