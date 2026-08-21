# M376 qualification cycle

`RemainingShovelBreaksCycle` rebuilds the raised stone fixture in two
fresh official server JVMs. Each run places clay `82`, snow `78`, snow
block `80`, and soul sand `88` on the top face, holds gold shovel item
`284`, fully breaks each cell, and observes Packet21 drops. The signal
must name block ids `82`, `78`, `80`, and `88`. One official EOF is
retried after a 5 second sleep.

The frozen semantic SHA-256 is
`40c64c0c07f6bc2b0dd8ed47b2526c1b5ef81a70c4b44720126cc44bc5d15c52`.

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.

Run directly with:

```text
java tools/smoke/RemainingShovelBreaksCycle.java m376-remaining-shovel-breaks
```
