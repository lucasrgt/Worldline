# M66 player combat map

## Stable boundary

- One authenticated client sends Packet7 action 1 against a target entity ID
  resolved from that target's fresh Packet20 username binding.
- The attacker returns only after a new Packet38 status 2 for that target.
- The victim independently observes its own Packet38 status 2 before Packet8
  changes health from 20 to 18.
- Full undamaged leather and an undamaged diamond sword make the applied damage
  exactly two; the actor's Packet103 view observes sword wear from zero to one.
- Clean save persists victim health 18.

## Oracle

The official Beta 1.7.3 server owns PvP range/visibility acceptance, damage,
armor reduction, hurt status, health emission, weapon wear, and NBT save. The
server authenticates the attacker from the connection and does not trust the
source entity ID carried by Packet7.

## Non-claims

M66 does not claim generic attacks, Packet7 interact mode, raw entity IDs,
unarmored or arbitrary weapon damage, armor wear, repeated hits, hurt
resistance, knockback, death, respawn, projectiles, mobs, health attribution,
latency, concurrent damage attribution, reconnect/ID reuse, or reproduction of
the Aero lag spike.

Frozen expected signature SHA-256: `8d05a812d9bfa62ac53321d1cca3f96c2cf9ff76668e36cdf0605945b883022c`
