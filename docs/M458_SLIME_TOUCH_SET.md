# M458 slime touch set

M458 opens the official slime contact-damage boundary. A headless
protocol-14 client places one default spawner in seed `17320110707` slime
chunk `-2,-2` below `y=16`, rewrites MobSpawner `EntityId` to `Slime`, and
observes Packet24 type `55`. Walking into a larger slime AABB emits
Packet38 status 2 then Packet8. Metadata index 16 makes size-1 versus
larger one family. That contact family is the SET.

Spawner placement is bounded to four attempts and succeeds only after the
authoritative remote block view reports `52:0`. This stabilizes placement
without changing the cave, spawn, size, or contact oracle.

This milestone does not claim M412 parent-split or child Packet24
identities, M423 slimeball `341` plus sticky piston `29`, or the slime-chunk
cave `y<=16` geometry as its own freeze. Child splits stay outside the
hash. Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`84cc0baf6465c46adf5437018728a84b237f8d611fa461bcc6335932432f2d26`.
