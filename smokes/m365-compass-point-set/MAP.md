<!-- worldline-map-schema=1 -->
<!-- boundary=client-runtime-equivalence -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=45694ff4da19a7b3c554d405380e9535c075d7fee1f3880674813ed5cc9a8b68 -->

# M365 behavior map

Frozen expected signature SHA-256: 45694ff4da19a7b3c554d405380e9535c075d7fee1f3880674813ed5cc9a8b68

The server half seeds compass item `345`, Packet16-selects it, reads
`level.dat` `SpawnX/Y/Z`, observes two distinct player cells, saves, and proves
the item plus fixture through a fresh login. Its trace claims only server spawn
data, held-item state, positions, and persistence.

The needle half uses a separate client differential through actual mapped and
official `TextureCompassFX` execution. Two fresh mapped and two fresh official
processes cross east/west spawn positions with yaw `0` and `180`; smoothing
state and the complete pixel digest agree for all four arms.

The client half is frozen by shared physics signature
`c2508b3dfff5f7852ce6b3155c5257ba781482031001cfdd38326b3363a5c014`.
Compass crafting, clock behavior, map use, GUI behavior, and Nether spin are
excluded.
