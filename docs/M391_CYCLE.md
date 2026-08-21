# M391 qualification cycle

`CreeperExplodeSetCycle` rebuilds a raised dirt-and-wool pad in two
fresh official server JVMs. Each run retargets one spawner `EntityId` to
`Creeper`, sets night, observes Packet24 type `50`, and requires Packet60
strength `3` to air both dirt and wool cells. The signal must name type
`50`, strength `3`, and multiple wool-plus-dirt destroyed cells. One
official EOF is retried after a 5 second sleep. Headless `B173WireClient`
is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/CreeperExplodeSetCycle.java m391-creeper-explode-set
```

Canonical evidence uses two official server JVMs and six client sessions.
The frozen semantic SHA-256 is
`2a74b9f63925b31966343a26c78c5b6d87dcdb84096822099fe3988f5d59b771`.
