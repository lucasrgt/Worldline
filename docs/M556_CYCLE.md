# M556-RS-NOR-LATCH-SET Rs nor latch set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M556 opens the official two-torch RS-NOR latch boundary. Packet15 of
redstone torch item `76` places north wall torch `76:4` on body A and south
wall torch `76:3` on body B. A west-facing repeater and dust line from B
holds A unlit `75:4`. Enabling the ground-lever SET input into B's repeater
turns Q on (`75:4 -> 76:4`) and Q-bar off (`76:3 -> 75:3`); both stay after
the lever is disabled. Enabling and then disabling RESET returns the pair to
`75:4` plus `76:3`, and the pair stays off.
The final RESET pair remains after a clean save plus fresh login.

This milestone is distinct from M312's single north invert `76:4 -> 75:4`
and from M555 torch burnout. It does not claim wire consumers, repeater
memory as a product, or the lighting plane. Headless `B173WireClient` only.
No GUI. No Aero.

The frozen semantic SHA-256 is
`eb3281691461d7e9823245dcc3d2c552caaa8fd1e2f82bb20ad42a82349218d3`.

## Qualification cycle

`RsNorLatchSetCycle` rebuilds the two-torch RS-NOR latch in two fresh
official server JVMs. Each run places north torch `76:4` and south torch
`76:3`, waits for RESET `75:4` plus `76:3`, arms the hold path, enables SET,
then disables it and requires the pair to stay ON. It enables RESET, disables
it, requires the pair to stay OFF, and reloads that RESET pair after save plus
a fresh login. The signal
names both complementary torch cells and is distinct from M312's single
`76:4 -> 75:4` invert and from M555 burnout. One official EOF is retried
after a 5 second sleep.

The frozen semantic SHA-256 is
`eb3281691461d7e9823245dcc3d2c552caaa8fd1e2f82bb20ad42a82349218d3`.

Run directly with:

```text
java tools/smoke/RsNorLatchSetCycle.java m556-rs-nor-latch-set
```

Canonical evidence uses two official server JVMs and five client sessions per
server JVM (ten sessions total).
Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,blockA=3:72:4:1:0,blockB=7:72:4:1:0,set=10:72:4:69:floor->on->off,reset=5:72:6:69:floor->on->off,q=3:72:3:75:4->76:4->75:4,qbar=7:72:5:76:3->75:3->76:3,stays-on=true,stays-off=true,persisted=q=75:4+qbar=76:3,clients=5,disconnect=clean`.

Frozen semantic SHA-256: `eb3281691461d7e9823245dcc3d2c552caaa8fd1e2f82bb20ad42a82349218d3`.
