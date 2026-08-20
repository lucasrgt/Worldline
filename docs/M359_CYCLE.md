# M359 qualification cycle

`BedNetherExplodeCycle` rebuilds a Nether netherrack-and-bed fixture in two
fresh official server JVMs. Each run logs a dimension `-1` actor, places item
`355`, empty-hand Packet15-uses the bed, and observes Packet60 at strength
`5` with Packet17 absent. The frozen signal must name Overworld sleep and
Nether explode. One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/BedNetherExplodeCycle.java m359-bed-nether-explode
```

The frozen semantic SHA-256 is
`be77b379de881712f9089340681a1a0779977df7934e51508858f83c97a9a7a6`.

Canonical evidence uses two official server JVMs and six client sessions.
Headless protocol-14 only. No GUI. No Aero.
