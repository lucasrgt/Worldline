<!-- worldline-map-schema=1 -->
<!-- boundary=portal-reentry-cooldown -->
<!-- nonclaims=minimum-release-duration,dynamic-coordinates,coordinate-scaling,non-player-entities,construction-limits -->
<!-- frozen-trace=7d049dc9670176f8a7d18b746d9deba02609d0523483c83589572a163bc4511c -->

# M652 portal re-entry cooldown behavior map

An official protocol-14 player constructs the qualified upright portal boundary: fourteen
obsidian `49:0` blocks enclose six portal `90` cells. A 120-tick residence produces Packet9
`0→−1`, and the generated Nether portal is discovered relative to the corrected destination
pose rather than by freezing its dynamic coordinates.

The same player is placed back inside those six destination portal cells and remains there for a
second 120-tick observation. A corrected pose confirms the player is still inside; no return
Packet9 occurs and the dimension remains `−1`. The control records that same player outside the
collision region after exactly 220 observed ticks, then records the player inside it again before
the 120-tick return residence. Packet9 `−1→0` follows, a six-cell/fourteen-frame return portal is
visible, and save persists dimension `0`.

`worldline.testkit.PortalReentryCooldownFixture#verify` equatably binds the same actor key, the
blocked inside pose, explicit collision-region exit and re-entry, bounded timings, portal counts,
and persisted dimension. A wait-only roundtrip cannot satisfy this public evidence contract.
This boundary intentionally does not infer the minimum release duration from decompiled source;
it freezes the official 120-tick blocked and 220-tick released observations only. It also makes
no claim about dynamic coordinates, coordinate scaling, non-player entities, or frame limits.

Frozen signal:

```text
dimensions=0->-1->-1->0,column=10,source=4:65:4,sourcePortal=6:14,destinationPortal=6:14,contactHold=120,outsideRelease=220,path=inside->outside->inside,sameActor=true,returnResidence=120,returnPortal=6:14,persisted=0,clients=1,disconnect=clean
```

Frozen trace SHA-256: `7d049dc9670176f8a7d18b746d9deba02609d0523483c83589572a163bc4511c`.
