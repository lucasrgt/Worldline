# M391 qualification cycle

`CreeperExplodeSetCycle` rebuilds a raised dirt-and-wool pad in two
fresh official server JVMs. Each run retargets one spawner `EntityId` to
`Creeper`, sets night, observes Packet24 type `50`, and requires Packet60
strength `3` to list at least one base-layer dirt and wool cell. The signal
must name type `50`, strength `3`, multiple wool-plus-dirt destroyed cells,
and a nonempty persisted crater without freezing exact ray choices. One
official EOF is retried after a 5 second sleep. Headless `B173WireClient`
is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/CreeperExplodeSetCycle.java m391-creeper-explode-set
```

Canonical evidence uses two official server JVMs and six client sessions.
The frozen semantic SHA-256 is
`14ad8cdcf99568672d696cd1c79210ab82f31f2bb6bbda7f005f4c162d76f60c`.
