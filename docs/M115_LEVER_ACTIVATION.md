# M115 lever activation

Status: GO in Worldline v1.103.0.

M115 adds `BlockActivationMultiplayerSession.activateBlock(position, face)` as
the first bounded redstone-component action. The b1.7.3 adapter requires a
synchronized personal window, no active container, an observed empty cursor
and an empty selected hand before emitting Packet15 with the server-observed
block coordinate and face.

The official fixture builds a ten-block stone column from the fixed-seed
ocean floor and places lever 69 on its east face above the waterline. The actor fixes yaw at
`-90` degrees and waits 200 heartbeat ticks before the treatment, so water
updates and lever orientation cannot contaminate it. Empty-hand activation
changes the lever from `69:1` to `69:9`. Packet53 updates the live cache and a
fresh session's Packet51 preserves the same state.

Across the complete chunk, exactly the lever metadata changes. The ordered
state-delta SHA-256 is
`3506bb3866a86782ddacfae92e7468ec72d1874777e768650eb4be95b8810c85`.

M115 qualifies component activation, not generic right-click behavior or
redstone propagation. It does not claim wire power, neighbor notification,
doors, torches, repeaters, pressure plates, TNT, pistons, circuits, timing,
rendering, cross-chunk behavior or arbitrary interactable blocks.
