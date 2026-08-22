# M552 qualification cycle

`TntQcSetCycle` rebuilds the raised TNT fixture in two fresh
official server JVMs. Each run places TNT item `46`, places stone on
top of that cell, powers the block above with lever `69`, requires
Packet23 type `50` on the existing object tracker, waits through the
official fuse, and reloads the air crater. One official EOF is retried
after a 5 second sleep. Headless `B173WireClient` is the only client.
There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/TntQcSetCycle.java m552-tnt-qc-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`a0ad8d6262175c29d1c7d1dadfcaf90f6a45d1db92c4c7dbbb63983a969b0732`.
