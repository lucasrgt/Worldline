# M386 qualification cycle

`IceSnowMeltSetCycle` rebuilds the raised stone fixture in two fresh
official server JVMs. Each run Packet15-places ice `79` and snow layer
`78` beside floor torch `50:5`, then waits a bounded official-tick window
until ice is still water `9:0` and snow is air `0:0`. One official EOF is
retried after a 5 second sleep. Exact melt delay is not hashed.

The frozen semantic SHA-256 is
`00d10f8cca091d8efcf6f005b84e192d110161deafabfb6a71d69862a5de6b7a`.

Run directly with:

```text
java tools/smoke/IceSnowMeltSetCycle.java m386-ice-snow-melt-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
