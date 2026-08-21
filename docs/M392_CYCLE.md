# M392 qualification cycle

`RemainingFluidFlowCycle` rebuilds the paired raised stone trenches in two
fresh official server JVMs. Each run seeds still water `9` and still lava
`11`, opens the adjacent dirt gates onto air, waits for official horizontal
flow, and reloads both sources plus both flowed cells. The signal must
include still water `9` and still lava `11` flowing into air. One official
EOF is retried after a 5 second sleep. Headless `B173WireClient` is the
only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/RemainingFluidFlowCycle.java m392-remaining-fluid-flow
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`8ec5aefbab73a3cd36a48185fa30c6266c70c3392ce80a5b319f8a6d94f2cfba`.
