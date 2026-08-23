<!-- worldline-map-schema=1 -->
<!-- boundary=state-world-differential -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=419100749c15ecc53954dc181ce9bd27403242be9ab0b60dc5cd3778a2a14419 -->

# M511-SW behavior map

- Official server class `az` is mapped `BlockRedstoneOre`; block 73 is
  `Block.oreRedstone` and block 74 is `Block.oreRedstoneGlowing`.
- The mapped public click method calls private trigger `g`, which emits
  particles and replaces 73 with 74. The trigger contains no scheduled-update
  call.
- Only the glowing constructor enables `Block.tickOnLoad`. World random ticks
  invoke its `updateTick`, which replaces 74 with 73.
- The differential fixture installs an anonymous player at the target chunk so
  the official active-chunk random-tick path runs. The same seed, chunks, click,
  and tick loop execute against mapped classes and the unmodified obfuscated
  server JAR.

An adjacent untouched 73 block is the negative control. The smoke records the
oracle-derived seeded reversion tick and exact IDs/metadata. It does not claim
drops, harvest, light output, packets, persistence, or GUI behavior.

Frozen expected signature SHA-256: 419100749c15ecc53954dc181ce9bd27403242be9ab0b60dc5cd3778a2a14419

## Frozen semantic signal

`oracle=MATCH,fixture=m511-sw-redstone-ore-trigger,ticks=2000,controlled=true`
