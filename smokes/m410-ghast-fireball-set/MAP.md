<!-- worldline-map-schema=1 -->
<!-- boundary=m410-ghast-fireball-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=4a77f0136d56574b37e6aca69072e884a92ea9240a1904aca9aaaa8170e08b76 -->

# M410 behavior map

Official server symbols:

- `net.minecraft.src.EntityGhast` is Packet24 type `56`. It spawns only in
  dimension `-1` when `spawn-monsters=true` and a 4-high air column exists.
- `net.minecraft.src.EntityFireball` is a Packet23 object whose type is
  discovered live. Frozen object types `1,10,11,12,60,61,62,70,71,90` and TNT
  `50` must not be selected. The fireball thrower is the ghast entity ID.

The fixture uses the M359 8-arg Nether profile (`allowNether=true`) plus the
extracted monster accessor, seeds a netherrack platform, and waits for type
`56` then the unknown Packet23.

This map does not claim pigmen (M411), combat, gunpowder drops, or fireball
blast rays.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|profile=allow-nether-true+spawn-monsters-true|entry=prelogin-player-nbt-dimension-minus-one+item87+item52|fixture=nether-netherrack87-platform+spawner52-ghast|cause=nbt-entityid-ghast+ghast-los|wire=packet24-type56+packet23-type63-thrower-ghast|oracle=nether-ghast-and-fireball-object|dimension=-1,support=36:57:-14:87,pads=1,spawner=36:58:-15:52:0,entityid=Ghast,ghast=type56,fireball=type63,thrower=ghast,packet23-known=absent,clients=3,disconnect=clean
```

Frozen semantic SHA-256:
`4a77f0136d56574b37e6aca69072e884a92ea9240a1904aca9aaaa8170e08b76`.
