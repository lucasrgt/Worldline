# M391 qualification cycle

`CreeperExplodeSetCycle` rebuilds a raised checkerboard dirt-and-wool pad in two
fresh official server JVMs. Each run retargets one spawner `EntityId` to
`Creeper`, sets night, observes Packet24 type `50`, and requires Packet60
strength `3` to select and air at least one known cell of each material. A
fresh login must retain those exact cells as air. The signal must name type
`50`, strength `3`, and multiple wool-plus-dirt destroyed cells. One
official EOF is retried after a 5 second sleep. Headless `B173WireClient`
is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/CreeperExplodeSetCycle.java m391-creeper-explode-set
```

Canonical evidence uses two official server JVMs and six client sessions.
The frozen semantic SHA-256 is
`389f99f5639c66342a8560c23fe7e85cbe1aafc6e71530ed05c0cc7bbdbb19c0`.
