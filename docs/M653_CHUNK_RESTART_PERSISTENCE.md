# M653-CHUNK-RESTART-PERSISTENCE chunk restart persistence

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M653 freezes one official dedicated-server lifecycle in which a stocked chest, a dropped dirt item, and an empty minecart survive an explicit chunk unload, a graceful stop, and a fresh-process restart. Semantic state must compare equal while unstable entity IDs stay normalized out of the equatable evidence.

## Qualification cycle

DataDrivenCycle runs two fresh official dedicated-server replicas. An operator stocks a chest with glass, drops dirt, and spawns a minecart in chunk 20:20, teleports beyond view distance, observes Packet50 for the target chunk, and stays absent for 100 ticks before the server saves and stops. A brand-new server process boots on the persisted workspace and a fresh client reloads chunk 20:20. The TestKit boundary validates the reopened chest stack, item stack, minecart type, target-chunk membership, and quantized positions while excluding unstable entity IDs. Frozen trace SHA-256: 898bb819c0254d8ea34355180387bd7602747da87866ff5e2e91275571ca876c.

Expected signal: `chunk=20:20,unload=packet50,stop=graceful,restart=new-process,reload=fresh-client,chest=glass20,item=3:1:0,minecart=type10,identity=normalized,replicas=2,disconnect=clean`.

Frozen semantic SHA-256: `898bb819c0254d8ea34355180387bd7602747da87866ff5e2e91275571ca876c`.
