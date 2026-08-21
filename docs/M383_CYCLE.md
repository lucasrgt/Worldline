# M383 qualification cycle

`MushroomPlaceSetCycle` rebuilds the raised dark dirt-plus-netherrack
pocket in two fresh official server JVMs. Each run places brown mushroom
item `39` on dirt and red mushroom item `40` on netherrack, then reloads
blocks `39:0` and `40:0`. The signal must include both `39` and `40`. One
official EOF is retried after a 5 second sleep. Headless `B173WireClient`
is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/MushroomPlaceSetCycle.java m383-mushroom-place-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`3a737afbb664a8e1a32858a9e371ced9062f5a30950b631a9372829456fa9a21`.
