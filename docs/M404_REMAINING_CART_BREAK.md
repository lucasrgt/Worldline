# M404 remaining cart break

M404 opens the official remaining-cart-break set. Packet15 of minecart
`328` on rail `66` creates Packet23 type `10`. Packet15 of chest-minecart
`342` on a second isolated rail creates Packet23 type `11`. Packet7 attack
(`leftClick=1`) with diamond sword `276` then breaks both objects until
Packet21 drops: type `10` yields `328`, and type `11` yields `328` plus
chest `54`. The frozen signal includes both object types and both drop
families.

This is distinct from shipping M155 (type-`10` spawn only), M311 (type-`11`
window plus type-`12` interact), and M326 (workbench crafts of `328`/`342`/
`343`). It does not re-qualify riding, furnace-cart type `12`, or vehicle
crafts. Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`8a80558c9383a317d0d6a8f145c940ff21cb07ffb3649aa4c564214adde79bcf`.
