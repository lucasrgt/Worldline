<!-- worldline-map-schema=1 -->
<!-- boundary=b173-tile-utility-physical-envelope-cycle -->
<!-- nonclaims=occupied-jukebox-state-domain,tile-payloads,container-contents,activation,redstone,tick-policy,neighbor-response,native-render -->
<!-- frozen-trace=c95f251773ad35abcdb5d991515f7e57cfd74dda445f328a7767ef6b701cd36c -->

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
`family=tile-utility-physical-envelope,subjects=6,claims=17,layers=3,reload=FRESH_LOGINx17,state=bec6f572d5a4fd675124b1c87f11cc7817d191771259a9b4d091ba86f99658a5,collision=a832a0a31f55d5436ae99a788818f54dacfbca31b7e4f91ecc469eaee609b16c,light=db7fba86f5f68d58271e380c4fd4a7d78026aadf9fcb3cba1b7e23d3302965b7`.

Qualified semantic signature:
`c95f251773ad35abcdb5d991515f7e57cfd74dda445f328a7767ef6b701cd36c`.
