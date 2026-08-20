# M358 qualification cycle

`SnowCraftSetCycle` rebuilds the personal 2x2 craft and raised-stone
harvest fixture in two fresh official server JVMs. Each run crafts snow
block `80` from four snowballs `332`, shovels snow layer `78` and snow
block `80` to Packet21 `332`, and reloads the crafted `80` after save plus
fresh login. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`d35de53474c363b1b580a865ea4bcce9403b8f9092e3ca5be19e9f1bf6e6d1be`.

Run directly with:

```text
java tools/smoke/SnowCraftSetCycle.java m358-snow-craft-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
