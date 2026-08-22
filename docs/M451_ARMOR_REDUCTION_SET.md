# M451 armor reduction set

M451 opens the official armor-reduction SET. The same official melee
source — a Packet24 type-`54` zombie at explicit Normal difficulty `2`,
provoked once per trial and never player-vs-self Packet7 —
produces different Packet8 health deltas for unarmored versus leather
`298-301`, iron `306-309`, and diamond `310-313`. Observed unarmored
Packet8 is `20->18` (damage 2). Each seated full family reduces that to
`20->19` (damage 1). Armor is equipped through personal window-0
Packet102 slots `5-8`. Health is restored with golden apple `322` or
cooked pork `320` between trials.

This family is distinct from armor crafts (M314/M320-M322), equip-only
window proofs (M270-M273), and PvP Packet7 (M66). It does not claim
durability, gold/chain families, or player-vs-player.

The frozen semantic SHA-256 is
`b04b51a3cb23c8254f44a5a8fddd04c0066bb3be81e60bd7b8ffde3ae89b0897`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
