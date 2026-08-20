# M402 qualification cycle

`RemainingDetectorRailCycle` rebuilds the raised stone fixture in two fresh
official server JVMs. Each run places detector rail item `28` as unpowered
`28:0`, then minecart item `328` on that detector. Packet23 type `10` occupancy
writes `28:8`. The result is distinct from unpowered place-only `28:0` (M185),
powered-rail `27:8` (M309), powered-rail motion (M377), and regular rail `66`
(M155). One official EOF is retried after a 5 second sleep. Headless
`B173WireClient` is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/RemainingDetectorRailCycle.java m402-remaining-detector-rail
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`00ed23852b2822be0b8b8766debc5cf5049c7e54b7c106f0e7c8d6a5028b8ab3`.
