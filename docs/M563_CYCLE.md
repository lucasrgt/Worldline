# M563 qualification cycle

`NetherExitCreateSetCycle` rebuilds the M382 overworld frame, traverses
to the Nether, relogs east of the generated portal, and returns through
a second Nether frame in two fresh official server JVMs. Each run requires
Packet9 `0→-1→0` and a created Overworld portal of fourteen obsidian `49`
cells plus six portal `90` cells that is not the source frame. One official
EOF is retried after a 5 second sleep. Headless `B173WireClient` is the
only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/NetherExitCreateSetCycle.java m563-nether-exit-create-set
```

Canonical evidence uses two official server JVMs and six client sessions.
The frozen semantic SHA-256 is
`6a31a5c30bf7a861c626da550e1989e4d2c38f0a32cd4607e27a9093fa6a268d`.
