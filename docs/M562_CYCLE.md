# M562 qualification cycle

`PortalPairSetCycle` rebuilds two Overworld portal frames that collapse to
one Nether cell in two fresh official server JVMs. Each run lights one
frame, travels `0→-1`, returns, seats a neighbor frame in the same 8:1
cell, and travels `0→-1` again. Both exits reuse one generated Nether
portal. One official EOF is retried after a 5 second sleep. Headless
`B173WireClient` is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/PortalPairSetCycle.java m562-portal-pair-set
```

Canonical evidence uses two official server JVMs and two client sessions.
The frozen semantic SHA-256 is
`d9c652c6452861ad1eda49be87a165111895c142551b462152ebb388ffb81b6c`.
