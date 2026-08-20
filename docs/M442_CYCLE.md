# M442 qualification cycle

`RemainingRecordPlaceSetCycle` rebuilds the raised stone plus two-jukebox
fixture in two fresh official server JVMs. Each run Packet15-places item
`84` twice, inserts gold disc `2256` and green disc `2257`, captures
Packet61 for each disc id, and reloads both `84:1` cells. Packet21 eject
is required absent. One official EOF is retried after a 5 second sleep.

The frozen signal must name jukebox `84` place plus both disc ids, and
must not name Packet21 eject.

Run directly with:

```text
java tools/smoke/RemainingRecordPlaceSetCycle.java m442-remaining-record-place-set
```

The frozen semantic SHA-256 is
`b70badf841ffc29e7c9adb0c7d29b5c2b687a43a5bcdb0e85e065170d1f7551a`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
