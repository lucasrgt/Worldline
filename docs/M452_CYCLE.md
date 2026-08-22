# M452 qualification cycle

`KnockbackCooldownSetCycle` rebuilds the raised grass platform in two
fresh official server JVMs. Each run retargets one spawner `EntityId` to
`Zombie`, sets night, observes Packet24 type `54`, and requires that
zombie melee on the actor emit Packet38 status `2` then Packet8 health
`20 -> 18` together with Packet28 velocity upward and away from the mob. A
second contact inside vanilla hurt-time must not emit another Packet8
drop. One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/KnockbackCooldownSetCycle.java m452-knockback-cooldown-set
```

The frozen semantic SHA-256 is
`096b8ab01152b6efc7574f50c63e4f48562ea96d7a3a095b2daaa2faecde5e48`.

Canonical evidence uses two official server JVMs and four client
sessions. Headless protocol-14 only. No GUI. No Aero.
