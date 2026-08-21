# M70 Aero Combat Window

M70 composes three previously qualified seams in one official-server session:
M66 armored combat, M68 real Aero multiplayer rendering, and M69 named peer
swing animation. It adds no public API.

## Contract

Two wire sessions establish the exact M66 fixture: full undamaged leather on
the victim, an undamaged diamond sword on the attacker, cleared login
invulnerability, and range below six blocks. A real StationAPI/Aero client joins
as a passive observer and resolves both names through Packet20.

After ten warm-up frames, the attacker calls `swingHeldItem()` immediately
followed by `attackPlayer(victim)`. The real client observes Packet18
attacker/animation1 before Packet38 victim/status2 on the same handler stream.
The victim independently observes Packet38 before Packet8 health 18, and the
attacker observes sword wear from damage 0 to 1. After Packet38, the client
completes at least twenty renderer frames and writes at least one strictly
parseable Aero row with visible chunks before disconnecting.

## Evidence and non-claims

Two fresh scenarios use two official server JVMs, four wire sessions, and two
real graphical Aero client JVMs. The server artifact and pinned Aero checkout
are verified, and the checkout remains clean.

Packet18 is not an attack acknowledgment and has no target. Packet38 has no
attacker or damage value. The isolated ordering composes independent evidence;
it does not prove network-level causality. Handler TAIL plus subsequent renderer
frames does not prove pixels, frustum visibility, or a rendered swing. M70 does
not claim a spike, FPS threshold, performance difference, paired control,
late health persistence, generic combat, or server-synchronized Aero content.
