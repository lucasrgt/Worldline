# M555 qualification cycle

`TorchBurnoutSetCycle` rebuilds the cloned M312 raised-stone inverter in two
fresh official server JVMs. Each run places redstone torch item `76` as live
`76:4`, proves the Packet53 `76:4 <-> 75:4` family with one invert and a
neighbor-update return to `76:4`, then rapidly toggles through the official
eight-turn-off / 100-tick burnout window. Live unlit `75:4` holds while the
support is unpowered even under a neighbor update, then recovers to `76:4`
after the window. Fresh login keeps recovered `76:4`. One official EOF is
retried after a 5 second sleep.

The frozen semantic SHA-256 is
`51a58a2129fecaba1f082e28aaa285177901ece62fb08c5e70d85fbcd3535713`.

Run directly with:

```text
java tools/smoke/TorchBurnoutSetCycle.java m555-torch-burnout-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
