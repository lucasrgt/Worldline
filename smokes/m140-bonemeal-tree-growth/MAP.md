<!-- worldline-map-schema=1 -->
<!-- boundary=m140-bonemeal-tree-growth -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=d5bca5667d5f93503d8c2226bf52d4e49d9395d51c2e2da675497b7d6a57d896 -->

# M140 behavior map

The fixture raises one dirt block and oak sapling `6:0` into a dry, bounded
region. A fresh client applies bonemeal `351:15` through Packet15. Incremental
world updates prove the root becomes oak log `17:0`; a third client then reads
the saved chunk and requires a bounded positive trunk and canopy.

Only the deterministic root transition enters the causal state hash. Vanilla
owns the exact randomized tree geometry, so the public evidence requires at
least four logs and ten leaves without freezing a particular canopy shape.

Frozen semantic SHA-256:
`d5bca5667d5f93503d8c2226bf52d4e49d9395d51c2e2da675497b7d6a57d896`.
