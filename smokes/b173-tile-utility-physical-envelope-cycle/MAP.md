<!-- worldline-map-schema=1 -->
<!-- boundary=b173-tile-utility-physical-envelope-cycle -->
<!-- nonclaims=occupied-jukebox-state-domain,tile-payloads,container-contents,activation,redstone,tick-policy,neighbor-response,native-render -->
<!-- frozen-trace=73f4aeecdae622592f04014a1c46ba53b2b0102c0906ffa64e9993c4f2ae737e -->

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
`family=tile-utility-physical-envelope,subjects=6,claims=17,layers=3,reload=FRESH_LOGINx17,state=bec6f572d5a4fd675124b1c87f11cc7817d191771259a9b4d091ba86f99658a5,collision=a832a0a31f55d5436ae99a788818f54dacfbca31b7e4f91ecc469eaee609b16c,light=b14477783b41d8f59c51015536779a1bffc64710717771bb8890aecde19de64ca`.

Qualified semantic signature:
`73f4aeecdae622592f04014a1c46ba53b2b0102c0906ffa64e9993c4f2ae737e`.
