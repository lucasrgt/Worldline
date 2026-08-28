<!-- worldline-map-schema=1 -->
<!-- boundary=b173-tile-utility-physical-envelope-cycle -->
<!-- nonclaims=occupied-jukebox-state-domain,tile-payloads,container-contents,activation,redstone,tick-policy,neighbor-response,native-render -->
<!-- frozen-trace=584e8de3918573b16e41b4a55c2ecd0a3933cb1ca0d242cf248cb77dff6c3ad8 -->

# Beta 1.7.3 tile-utility physical envelopes

One catalog covers dispenser, note block, mob spawner, chest, furnace, and jukebox. Five state
rows exercise the complete placement-reachable metadata contract available to this package;
six collision rows prove full-cube trajectories and six light rows compare air with exact
per-subject source-cell skylight. The catalog distinguishes five opaque utilities from the
skylight-transparent mob spawner. Every row uses gameplay placement, canonical public evidence,
and a fresh login boundary against the unmodified official server.

The occupied jukebox metadata state is deliberately not claimed: reaching it requires a record
loadout/action not represented by this state-domain package. Tile payloads, container contents,
activation, redstone, ticking, neighbor response, and native rendering also remain outside scope.

Frozen aggregate signal:
`family=tile-utility-physical-envelope,subjects=6,claims=17,layers=3,reload=FRESH_LOGINx17,state=0000000000000000000000000000000000000000000000000000000000000000,collision=0000000000000000000000000000000000000000000000000000000000000000,light=0000000000000000000000000000000000000000000000000000000000000000`.

Bootstrap semantic signature:
`584e8de3918573b16e41b4a55c2ecd0a3933cb1ca0d242cf248cb77dff6c3ad8`.
