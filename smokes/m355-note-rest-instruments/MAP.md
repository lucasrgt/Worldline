# M355 behavior map

Official note block `25` is placed on the remaining instrument bases M313 did
not hash: glass `20` and gold block `41`. Empty-hand Packet14 begin-dig plays
each note without tuning. The dedicated server emits Packet54 play-note with
instrument `3` (glass) and `0` (gold / default material). Those ids are
distinct from M313's `1`, `4`, and `2`. Block metadata stays `0`; pitch is
tile-entity state carried on the packet.

This map is distinct from M313's hashed stone/planks/sand table and from M166
empty-hand Packet15 tune-click (one stone instrument, pitch `1`). Clay would
occupy the same default-material piano row as gold.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+glass20+goldblock41+noteblock25x2|cause=packet15-item25-place+empty-hand-packet14-play|wire=packet54-instrument3+0|oracle=official-note-rest-instruments+fresh-login-block25|column=17,bases=5:71:4:20:0+6:71:4:41:0,notes=5:72:4:25:0+6:72:4:25:0,play=packet54:3:0+packet54:0:0,instruments=3,0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`0b8bfa875138db6748a105c9ca98ad10bd8f4ff277dbe49e5d1d96e5790cf868`.
