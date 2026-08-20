# M398 qualification cycle

`JukeboxEjectSetCycle` rebuilds the raised stone plus two-jukebox fixture
in two fresh official server JVMs. Each run places item `84` twice,
inserts gold disc `2256` and green disc `2257`, Packet14-breaks both
playing cells, captures Packet21 for each disc id, and reloads air. One
official EOF is retried after a 5 second sleep.

The frozen signal must name both disc ids and Packet21 eject, not only
Packet61 play.

The frozen semantic SHA-256 is
`21d9a2123e3a3041573a22722d268dec75ee1d0d27d84fe0ae6f22e187f2bd8f`.

Run directly with:

```text
java tools/smoke/JukeboxEjectSetCycle.java m398-jukebox-eject-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
