# M66 Player Combat

M66 adds the first bounded PvP event to the multiplayer server framework. It
composes M65's exact leather equipment with one diamond-sword strike and keeps
the evidence from the two network streams separate.

## Contract

`CombatHealthSession.attackPlayer(name)` resolves the target from a fresh
Packet20 identity, writes Packet7 with action 1, and returns only after the
requesting client receives a matching Packet38 hurt status for that target in
the isolated fresh-session boundary. Packet38 is not an ACK and carries no
attacker identity. Callers cannot
provide raw entity IDs or packet codes.

`awaitIncomingHit(18)` is victim-local. It requires an observed health baseline
of 20, then the victim's Packet38 status 2, then Packet8 health 18. Its value
does not name an attacker because neither Packet38 nor Packet8 carries source
identity. The isolated smoke composes this local evidence with the outgoing
strike observed on the other connection.

The fixture equips leather IDs 298..301 and selects diamond sword 276. The
official armor formula applies two points of damage, and Packet103 observes the
sword's damage field change from zero to one. The dedicated server writes
`pvp=true` explicitly while retaining `spawn-monsters=false`. The maintained
smoke seeds those exact items through official player NBT and alternates small
position heartbeats for both clients during the 80-tick invulnerability wait,
removing item-pickup and uncontrolled-fall nondeterminism without changing the
wire contract or frozen evidence.

## Evidence and non-claims

Two fresh official-server workspaces reproduce health 20 to 18 and persisted
NBT health 18. M66 deliberately excludes interaction, arbitrary combat,
repeated hits, death/respawn, knockback, armor durability, projectiles, mobs,
timing guarantees, and Aero spike attribution.
