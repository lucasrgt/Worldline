# M59 Chest Transfer and Restart Map

| Boundary | Exact evidence |
| --- | --- |
| Mapping | Personal slots 9-44 map to combined chest slots 27-62 with `combined=personal+18` |
| Transfer | Packet102/106 action 1 takes combined54; action 2 stores in owned chest slot0 |
| Atomic state | Active 63-slot view, canonical window0 view, and cursor commit together after each ACK |
| Close | M58 accepted window0 proof closes the exact post-transfer window |
| Restart | A new official server process and wire client reopen the same chest workspace |
| Persistence | Fresh Packet104 exposes stone in chest slot0 and an empty player tail |

M59 does not claim chest-to-player retrieval, merges, splits, right/shift clicks,
rejected container recovery, concurrent chest mutation, or generic containers.
