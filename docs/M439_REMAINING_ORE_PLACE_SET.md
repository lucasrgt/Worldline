# M439 remaining ore place set

M439 qualifies the official remaining-ore Packet15 place family as one
compound SET. It reuses the raised stone fixture so Packet15 places coal
ore `16:0`, lapis ore `21:0`, and unlit redstone ore `73:0` in one
session. The frozen signal names blocks `16`, `21`, and `73`. All three
cells survive a clean save plus fresh login.

This family is distinct from shipping M225 (one coal-ore cell), M230 (one
lapis-ore cell), and M229 (one unlit redstone-ore cell). It is also
distinct from M300 iron-pick cobble/coal/diamond-ore harvests and M375
gold/diamond-pick mossy-cobble/gold-ore/obsidian harvests. It does not
claim iron, gold, or diamond ore place, glowing ore `74`, mining drops,
or pick durability. Headless `B173WireClient` protocol-14 only. No GUI.
No Aero.

Frozen semantic SHA-256:
`0c58ca403f7064fde875a5257d07193fe9916277c21455b47ac366ab28b828ab`.
