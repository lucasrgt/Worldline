<!-- worldline-map-schema=1 -->
<!-- boundary=creeper-tnt-differential -->
<!-- nonclaims=crater-size,destroyed-count,player-damage,fuse-duration,powered-creeper,beds,chains -->
<!-- frozen-trace=66976217339dbcfb7b060a78387200c36ec9832ef648ad1802ced89cf02b033f -->

# M638 creeper versus TNT differential behavior map

The public boundary is `worldline.testkit.CreeperTntDifferentialFixture#observe`. One independent official probe supplies a proximity-fused Creeper Packet60 strength of `3`; another supplies a flint-primed TNT Packet60 strength of `4`. Equatable evidence retains both strengths, their delta of one, and the ordering that TNT is stronger.

The composite proof reuses the complete M391 and M381 official scenarios in fresh sibling workspaces inside each of two outer replicas. It does not infer strength from crater size, mapped source, or controlled-runtime behavior. Nested scenarios remain responsible for mob identity, priming cause, persistence, and clean disconnects.

Frozen signal: `creeper=packet60:strength3,tnt=packet60:strength4,delta=1,ordering=creeper<tnt,official-probes=2,replicas=2,disconnect=clean`.
