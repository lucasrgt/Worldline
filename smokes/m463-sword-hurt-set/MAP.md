<!-- worldline-map-schema=1 -->
<!-- boundary=m463-sword-hurt-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=34f99909ebaad48c9c513f7aef51ee8586e82fb1b0db74e616104c22b7bb738c -->

# M463 behavior map

The fixture raises an isolated `7×7` grass platform and places two default
mob spawners `52:0`. After a clean save the region NBT `EntityId` values are
rewritten from `Pig` to `Zombie` and `Skeleton`. Console time `14000` makes
the platform dark enough for `EntityMob.getCanSpawnHere`. Packet7 with
diamond sword `276` selected strikes Packet24 type `54` once and type `51`
once. Packet38 status 2 HURT is
required on both identities. `peekDeath` stays null after the first hit.
The session stops after hurt and does not require death or drops.

This map does not re-qualify M353 pig sword-damage hits-to-kill, M388
zombie feather / skeleton arrow death drops, M444 remaining death drops,
or M391 creeper Packet60 strength `3`. Headless `B173WireClient` only.
No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+two-spawner52|cause=nbt-entityid-zombie+skeleton+time-14000+diamond-sword-packet7|wire=packet24-type54+packet24-type51+packet38-status2|oracle=zombie-and-skeleton-hurt-not-death-not-drops-not-explode|column=17,platform=7x7-48grass,spawners=4:72:4:52:0+5:72:4:52:0,entityid=Zombie+Skeleton,mobs=type54+type51,night=14000,sword=276,hurt=packet38-status2,death=none,hits=1+1,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`34f99909ebaad48c9c513f7aef51ee8586e82fb1b0db74e616104c22b7bb738c`.
