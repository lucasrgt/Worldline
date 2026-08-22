# M547 sticky piston QC set

M547 opens the official sticky-piston quasi-connectivity boundary. It
clones the M367 west-facing sticky-`29` arm, then powers the stone ABOVE
the piston instead of the piston cell or the M144/M367 support lever.

One headless session builds the west-facing sticky arm on the raised
stone column, places QC stone on the piston, and Packet15-toggles the
above-block lever. Sticky piston `29` extends through QC, then pulls the
stone payload on retract. Those final cells remain after a clean save
plus fresh login.

Frozen semantic SHA-256:
`21af5dafa50bb529a1c0264a2be27d9b92aa0728c007fae07ecbef1547d92b1d`.

This is distinct from M546 regular piston-`33` QC, from M367 dual-arm
motion, and from shipping M144 1:1 support-lever pull. It does not claim
BUD updates, two-block chains, push limits, obsidian rejection, or a
generic piston model.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
