# M334 qualification cycle

`RecordSetCycle` rebuilds the raised stone plus two-jukebox fixture in
two fresh official server JVMs. Each run places item `84` twice, inserts
gold disc `2256` and green disc `2257`, captures Packet61 for each disc
id, and reloads both `84:1` cells. One official EOF is retried after a
5 second sleep.

The frozen semantic SHA-256 is
`b139e039c60f517453a6e8e0c3fe4f87b11f5c73faa81a77c7fceb7645428d53`.

Run directly with:

```text
java tools/smoke/RecordSetCycle.java m334-record-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
