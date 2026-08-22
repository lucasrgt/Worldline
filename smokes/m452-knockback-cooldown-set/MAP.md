# M452 behavior map

Official server symbols:

- `net.minecraft.src.EntityList` maps `Zombie` to Packet24 type `54`.
- Default mob spawner `52` writes NBT `EntityId=Pig`. After a clean save,
  `B173SpawnerSeed.entity` rewrites that tag to `Zombie` without `/summon`.
- Dedicated-server `spawn-monsters=false` stores world difficulty `0`, which
  zeroes player melee damage. M452 boots with `spawn-monsters=true`.
- `EntityMob.getCanSpawnHere` needs night (`time set 14000`) on the raised pad.
- `EntityMob.attackEntity` is the Packet7-equivalent melee: range `< 2`,
  `attackTime = 20`, then `attackEntityFrom` on the actor.
- Easy difficulty deals 2 damage (`20 -> 18`). Packet38 status `2` precedes
  Packet8. Knockback is Packet28 velocity upward and away from the Packet31 mob
  position. Vanilla `hurtResistantTime` (`max = 10`) holds a second contact
  without another Packet8 drop.

This map does not claim env damage (M307), PvP Packet7 (M66), or sword-hurt
on mobs (M463). Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+zombie-spawner52|cause=nbt-entityid-zombie+time-14000+melee-contact|wire=packet24-type54+packet38-status2+packet8-health20->18+packet28-velocity-away|oracle=zombie-melee-knockback+hurt-time-cooldown-not-env-not-pvp-not-sword|column=17,support=4:71:4:1:0,spawner=4:72:4:52:0,mob=type54,health=20->18,damage=2,knockback=away,cooldown=held,hurt=packet38-status2,night=14000,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`096b8ab01152b6efc7574f50c63e4f48562ea96d7a3a095b2daaa2faecde5e48`.
