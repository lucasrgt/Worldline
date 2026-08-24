# M627-CHUNK-UNLOAD-RELOAD chunk unload and reload persistence

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M627 freezes one official in-process chunk lifecycle in which a burning furnace, dropped item, and empty minecart survive eviction and reload. Entity identities must rotate while semantic state remains equivalent.

## Qualification cycle

DataDrivenCycle runs two fresh official dedicated-server replicas. An operator teleports beyond view distance, the original client observes Packet50 for the target chunk, the server remains without a player there for 100 ticks, and a fresh client reloads the same chunk. The TestKit boundary validates lit furnace output, item stack, minecart type, target-chunk membership, and rotated entity IDs. Frozen trace SHA-256: b3f9ef8c5fcb0ea1ed9a9b3850946d8ed75466e8ef67c24e926a593bfeb42dff.

Expected signal: `chunk=20:20,unload=packet50,reload=fresh-client,furnace=62+glass20,item=3:1:0,minecart=type10,identity=rotated,replicas=2,disconnect=clean`.

Frozen semantic SHA-256: `b3f9ef8c5fcb0ea1ed9a9b3850946d8ed75466e8ef67c24e926a593bfeb42dff`.
