<!-- worldline-map-schema=1 -->
<!-- boundary=b173-redstone-signal-consumers-subsystem-conformance-cycle -->
<!-- nonclaims=registry-presence,gameplay-placement,break-transition,drop-matrix,save-reload,native-render,dispenser-payload,note-audio,tnt-fuse,wire-propagation,jukebox-record-lifecycle -->
<!-- frozen-trace=8a2b1947b14e0f5b7fcee755cf14693f3a0bc87b19fe301389543def34a0d2f8 -->

# Beta 1.7.3 redstone signal consumers

This milestone binds dispenser, note block, TNT, torch, redstone wire, and jukebox to one reusable
public TestKit mini-subsystem. It closes only the eighteen Functional Census cells declared in
`census-delta.tsv`; prior lifecycle, persistence, rendering, payload, audio, fuse, and network
evidence keeps its original owner.

The universal layer supplies bounded metadata, collision, and light inspection. The archetype
layer invokes every block's direct tick contract and distinguishes the dispenser's four-tick
policy from inherited ten-tick no-ops. The singular layer proves five native causal transitions.
For powered consumers, a lever's metadata is written raw, read back as powered, and only then is
the consumer callback invoked. This orders cause before dispenser scheduling, note rising-edge
memory, and TNT entity priming. Wire loses support and drops once; jukebox remains unchanged.

Canonical trace:

`v1|seed=17320110855|redstone-consumer-23:time=1,entities=0,column=23.-1.-1.-1.4.1.1|redstone-consumer-25:time=2,entities=0,column=25.-1.-1.-1.10.1.1|redstone-consumer-46:time=3,entities=0,column=46.3.1.25500.10.1.1|redstone-consumer-50:time=4,entities=0,column=50.-1.-1.-1.10.1.-1|redstone-consumer-55:time=5,entities=0,column=55.65535.0.0.10.1.1|redstone-consumer-84:time=6,entities=0,column=84.3.-1.-1.10.1.1`

Frozen signature:
`8a2b1947b14e0f5b7fcee755cf14693f3a0bc87b19fe301389543def34a0d2f8`.

Frozen semantic signal:
`family=redstone-signal-consumers,subjects=23+25+46+50+55+84,claims=18,states=3,shapes=2,light=2,ticks=6,neighbors=5,oracle=MATCH`.

Atlas facets: redstone, signal-consumer, source-before-consumer, stateful-metadata, collision,
lighting, scheduled-tick, neighbor-response, rising-edge, primable, support-dependent, universal,
archetype, singular, server-headless, official-oracle, public-testkit.

Nonclaims: this cycle does not claim dispenser payload selection, note pitch or sound playback,
TNT fuse/explosion completion, wire power propagation topology, record insertion/ejection,
placement, breaking, persistence, native rendering, or any state beyond the declared domains.
