# M113 causal lighting

Status: GO in Worldline v1.101.0.

M113 turns M112's deterministic light snapshot into one bounded vanilla cause
and effect. In each of two fresh worlds, a protocol-14 actor begins with one
glowstone block in hotbar slot zero, placed through minimal official player
NBT. The actor sends the ordinary selected-slot and block-placement packets;
the unmodified official Beta 1.7.3 server confirms block 89 through Packet53.

After forty heartbeat ticks, the actor disconnects, the server saves, and a
fresh protocol session receives a full Packet51 for absolute chunk `(0,0)`.
Compared with the pre-placement snapshot, exactly 68 block-light samples
change, all increase, the maximum increase is 15, and the source coordinate
reaches level 15. The ordered changed-sample digest is
`9f3d2b0e7d511d0440d2990b3d80649d66a0b38a9d4c14fb889acd99751021fe`.
Sky light is unchanged.

The qualified fixture is intentionally narrow: fixed seed `17320110707`, one
glowstone at `(4,55,4)` above dirt, one chunk, and an add-source transition.
M113 does not claim the generic propagation algorithm, removal, opacity rules,
cross-chunk updates, client-side relighting, renderer brightness, day/night,
alternate terrain, Nether lighting, or arbitrary emitting blocks.
