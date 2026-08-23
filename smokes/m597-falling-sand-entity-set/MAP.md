# M597 behavior map

The official fixture stabilizes stone `1:0` at `(4,64,4)` with sand `12:0`
directly above it. Packet14 removes the stone and Packet53 must expose air
at the lower coordinate before settlement.

The live Packet23 object type is recorded from the official oracle rather
than assumed from decompiled source. That observed type is `70`, the
falling-sand entity. The server then places `12:0` in the lower cell and
leaves `0:0` in the former upper cell. After clean disconnect/save, a
fresh Packet51 must expose both states.

This map is distinct from M119's block-only settle, M274 gravel, and
M342's sand-plus-gravel pair. It does not claim gravel type `71`, long
falls, flint, entity collisions, or piston interaction.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=stone-column+supported-sand12|settle=40+40ticks|cause=packet14-remove-support|confirmation=packet53-air|effect=official-falling-sand-entity-land|observation=packet23-type-observed+live-packet53+fresh-login-packet51|column=10,lower=4:64:4:1:0->12:0,upper=4:65:4:12:0->0:0,entity-type=70,packet23=70,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`cc5127dcadf21010c8d1a840f832d3e1b95b803bd2b8d74dd7a5c77984a7328b`.
