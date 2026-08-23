# M590 behavior map

Official server symbols:

- `net.minecraft.src.EntityGhast` is Packet24 type `56`. It spawns only in
  dimension `-1` when `spawn-monsters=true` and a 4-high air column exists.
- `net.minecraft.src.EntityFireball` is Packet23 type `63`. The thrower is
  the ghast entity ID. `attackEntityFrom` copies the attacker's look vector
  into motion and acceleration.
- Packet7 left-click on that type-`63` entity, while looking straight up,
  must publish Packet28 whose Y component is upward and whose X/Z components
  stay near zero. That Packet28 is the punch. Packet60 strength `1` is M459
  impact, not this SET.

The cavern scan is restricted to the frozen support chunk `2,-1`. The actor
holds wooden sword `268` only because the protocol-14 attack helper requires
a vanilla sword; the official server redirects on any Packet7 attack.

This map does not re-qualify M410 spawn-only type `63` or M459 Packet60
strength-`1` hit.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|profile=allow-nether-true+spawn-monsters-true|entry=prelogin-player-nbt-dimension-minus-one+item87+item4+item52+item268|fixture=nether-netherrack87-platform+cobble4-pad+spawner52-ghast|cause=nbt-entityid-ghast+ghast-los+packet7-fireball-punch|wire=packet24-type56+packet23-type63-thrower-ghast+packet28-look-up|oracle=nether-ghast-fireball-punch-not-m410-spawn-only-not-m459-hit|dimension=-1,support=36:57:-14:87,pads=0,cobble-pads=1,spawner=36:58:-15:52:0,entityid=Ghast,ghast=type56,fireball=type63,thrower=ghast,punch=packet28-look-up,redirect=up,not-m410-spawn-only,not-m459-hit,packet23-known=absent,clients=3,disconnect=clean
```

Frozen semantic SHA-256:
`ccf294e6ee17b1c7670374e4d95dc9de2b663e720f0b137c14a1c6436e89bdbb`.
