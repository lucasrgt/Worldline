# M70 Cycle

Status: **GO** for a real Aero frame/log window after an ordered Packet18 and
Packet38 observer sequence.

## Qualified path

- exact official server bytes and pinned clean Aero revision are verified;
- two fresh official servers host four wire sessions and two real Aero clients;
- Packet1/13/51 readiness and Packet20 attacker/victim identities arm the observer;
- M69 Packet18 is emitted immediately before M66 Packet7;
- observer handler TAIL applies Packet18 attacker/1 before Packet38 victim/2;
- victim wire evidence preserves Packet38-before-Packet8 `20 -> 18`;
- attacker inventory preserves diamond-sword wear `0 -> 1`;
- terminal dropped-item ID reuse is accepted; live duplicate IDs remain invalid;
- each observer completes twenty post-Packet38 frames and a parseable Aero row;
- client, wire sessions and server shut down cleanly.

Frozen semantic SHA-256:

`977bf908fc7edf5e0cf707f81fffaf6208183440a0f07cca81e2b9a22d03e571`

Generated workspaces/logs and the official JAR remain local evidence, not
release artifacts.
