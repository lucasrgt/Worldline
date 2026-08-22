# M70 Aero Combat Window Evidence Map

| Boundary | Exact evidence |
| --- | --- |
| Runtime | Real b1.7.3 Fabric/StationAPI/Aero 3 observer joins the official server used by two wire actors |
| Fixture | Official-format player NBT places both actors and the observer near the exact level.dat spawn, with four leather pieces in the victim inventory and a diamond sword in attacker slot 36 before first login |
| Identities | Observer Packet20 maps exact attacker and victim names to the wire login entity IDs |
| Stimulus | Published M69 Packet18 animation 1 is sent immediately before published M66 Packet7 action 1 |
| Observer order | Client handler TAIL sees Packet18 attacker/1 before Packet38 victim/2 after vanilla applies each state |
| Victim oracle | Independent wire stream observes Packet38 before Packet8 health `20 -> 18` |
| Weapon oracle | Attacker's authoritative inventory observes diamond sword damage `0 -> 1` |
| Render window | At least 20 complete renderer TAIL frames occur after Packet38 |
| Aero window | At least one strictly parsed Aero row with visible chunks is written after the Packet38 baseline |
| Repetition | Two fresh server, wire-fixture and graphical-client JVM sets produce one semantic signature |

M70 proves ordered composition, not causal attribution. Packet18 is not an ACK
and Packet38 contains no attacker. Handler TAILs and subsequent frames do not
prove pixels or visibility. No FPS, spike, performance difference, paired
control, late health persistence, generic combat, or synchronized Aero content
is claimed.

Frozen expected signature SHA-256: `977bf908fc7edf5e0cf707f81fffaf6208183440a0f07cca81e2b9a22d03e571`
