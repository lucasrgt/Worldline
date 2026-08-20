# M403 qualification cycle

`RemainingBoatBreakCycle` rebuilds the natural still-water fixture in two
fresh official server JVMs. Each run places two boats `333` on still water
`9:0`, correlates Packet23 type `1` across two peers, empty-hand Packet7
button `1` attacks each boat, and requires Packet21 plank `5` plus stick
`280`. The signal must include two breaks, not a single Packet15 place.
One official EOF is retried after a 5 second sleep. Headless
`B173WireClient` is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/RemainingBoatBreakCycle.java m403-remaining-boat-break
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`34eb6766ee9194e30d2efd5712a5e932110351176e336e526d7c6f23a877dedc`.
