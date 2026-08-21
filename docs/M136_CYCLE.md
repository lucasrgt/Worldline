# M136 qualification cycle

`NetherDeathRespawnCycle` repeats the `-1→0` death/respawn lifecycle in two
fresh official server JVMs with Nether enabled. A production-path fixture
freezes the request bytes as `09-ff`.

Both runs reproduce skyless netherrack before death, nonpositive health,
Packet9 dimension `0`, health `20`, an Overworld-only decoded cache, empty
inventory, clean disconnect and persisted `dimension:health = 0:20`. Their
frozen semantic SHA-256 is
`48c243301cfa00388490bde784ac80eb7597256aa539b83f1777b841d77148a1`.

Canonical evidence uses two official server JVMs and two client sessions.
