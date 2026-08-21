# M437 qualification cycle

`LightningPigSetCycle` rebuilds an Overworld pig spawner and a Nether
pigman spawner in two fresh official server JVMs. Each run observes
Packet24 type `90` in dimension `0`, then retargets a DIM-1 MobSpawner
`EntityId` to `PigZombie` and observes Packet24 type `57`. The frozen
signal must name both identities and must not name Packet23 or pork `320`.
One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/LightningPigSetCycle.java m437-lightning-pig-set
```

The frozen semantic SHA-256 is
`536016d5292cf2d747ea4a029011726719795579c19e8507ec912154e9bd77db`.

Canonical evidence uses two official server JVMs and eight client sessions.
Headless protocol-14 only. No GUI. No Aero.
