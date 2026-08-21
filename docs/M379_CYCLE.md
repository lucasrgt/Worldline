# M379 qualification cycle

`IronDoorSetCycle` rebuilds the raised stone plus iron-door plus east-face
lever fixture in two fresh official server JVMs. Each run places door item
330 as both `71` halves, attaches lever `69`, opens the door with redstone,
closes it with redstone, and reloads the closed cells. The signal must
include both block-71 cells and the powered/unpowered metadata pair
`71:0->4->0`, `71:8->12->8`, and `69:1->9->1`. A place-only result matching
M241, an open-only result matching M118, or a wooden-closable result
matching M306 fails. One official EOF is retried after a 5 second sleep.
Headless `B173WireClient` is the only client. There is no GUI and no Aero
path.

Run directly with:

```text
java tools/smoke/IronDoorSetCycle.java m379-iron-door-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`9d887adb7cbebcca0c805d02f84507310ea3211b6e1abb774ec7e7ae8d3e4f0c`.
