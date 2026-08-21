# M378 qualification cycle

`BoatWaterSetCycle` rebuilds the natural still-water fixture in two fresh
official server JVMs. Each run places boat `333` on still water `9:0`,
correlates Packet23 type `1` across two peers, empty-hand Packet7 mounts,
then a second Packet7 detaches. The signal must include spawn plus attach
plus detach on still water `9`. One official EOF is retried after a 5
second sleep. Headless `B173WireClient` is the only client. There is no
GUI and no Aero path.

Run directly with:

```text
java tools/smoke/BoatWaterSetCycle.java m378-boat-water-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`bdd585b5e79c816f4761039c63a02aa8e9f6164e77d7baa4fa4b3980a6a8d905`.
