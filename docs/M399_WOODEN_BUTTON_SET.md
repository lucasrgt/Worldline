# M399 wooden button set

Beta 1.7.3 has no wooden button. Item and block `143` arrive later.
M399 therefore freezes the remaining official stone-button wall-attachment
family under this milestone id.

Packet15 of stone button item `77` places `77:1` on the east face of a
raised stone column, `77:2` on the west face, `77:3` on the south face,
and `77:4` on the north face in one session. Vanilla wall metadata is
the clicked Packet15 direction: east `5` writes `1`, west `4` writes `2`,
south `3` writes `3`, and north `2` writes `4`. Floor and ceiling buttons
are not legal in b1.7.3. All four unpowered cells survive a clean save
plus fresh login. The frozen signal names multiple `77` damages.

This is distinct from shipping M165/M279 (east-face `77:1` pulse only)
and from shipping M340 (lever `69:1->9->1` plus one south-pad `77:1->9->1`
pulse). It does not invent wooden buttons, claim the powered bit `77:9`
on every face, or attach redstone consumers. Headless `B173WireClient`
only. No GUI. No Aero.

The frozen semantic SHA-256 is
`898b58fa0f849df159f7bfcfde243b0957fddcd580770518251b1721cbf21c90`.
