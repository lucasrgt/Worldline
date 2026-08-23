<!-- worldline-map-schema=1 -->
<!-- boundary=m313-note-instruments -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=6e171effe14c350c22319797f836fbb498aa88b559a88bef337aa634f95943b6 -->

# M313 behavior map

Official note block `25` is placed on three instrument bases: raised stone,
oak planks `5`, and supported sand `12`. Empty-hand Packet14 begin-dig plays
each note without tuning. The dedicated server emits Packet54 play-note with
instrument `1` (stone), `4` (wood), and `2` (sand). Block metadata stays `0`;
pitch is tile-entity state carried on the packet.

This map is distinct from M166 empty-hand Packet15 tune-click (one stone
instrument, pitch `1`) and from M328's single stone Packet14 play.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+planks5+sand12+noteblock25x3|cause=packet15-item25-place+empty-hand-packet14-play|wire=packet54-instrument1+4+2|oracle=official-note-instruments+fresh-login-block25|column=17,bases=4:71:4:1:0+6:71:4:5:0+5:72:4:12:0,notes=4:72:4:25:0+6:72:4:25:0+5:73:4:25:0,play=packet54:1:0+packet54:4:0+packet54:2:0,instruments=1,4,2,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`6e171effe14c350c22319797f836fbb498aa88b559a88bef337aa634f95943b6`.
