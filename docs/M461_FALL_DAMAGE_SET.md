# M461 fall damage set

M461 opens the official compound fall-damage SET. A headless
`B173WireClient` walks off two distinct drop heights onto the same east
stone pad. Vanilla `EntityLiving.fall` applies `ceil(fallDistance - 3)`:
a 6-block fall drops Packet8 `20 -> 17`, golden apple `322` restores
full health, and a 10-block fall drops Packet8 `20 -> 13`. The taller
fall deals strictly more damage. Reduced health 13 persists after a
clean save plus fresh login.

This family is distinct from M307 drowning/suffocation/lava hurt and from
M469 void death. It does not claim armor reduction, water/ladder cancel,
cactus, fire, or PvP.

The frozen semantic SHA-256 is
`15947f02196bef6a87d9ec502c93150c37c209cdfeae766857f8f345c1b76cf4`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
