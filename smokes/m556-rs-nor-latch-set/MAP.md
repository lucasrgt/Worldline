# M556 RS-NOR latch-set behavior map

Two wall redstone torches form one official RS-NOR latch. Packet15 places
north torch `76:4` on body A and south torch `76:3` on body B. A west
repeater plus dust line from B holds A unlit `75:4` as RESET. A ground-lever
SET input on B's repeater inverts B to `75:3`; after save plus login,
A is lit `76:4` and stays on after SET is disabled. Enabling and disabling RESET returns
`75:4` plus `76:3` and stays off. The final RESET pair survives a clean
save plus fresh login.

This map does not re-qualify M312's single north invert `76:4 -> 75:4` or
M555 torch burnout. Headless `B173WireClient` only.

Frozen trace:

```text
pending final serialized qualification
```

The semantic SHA-256 is reconfrozen only by the final serialized qualification.
