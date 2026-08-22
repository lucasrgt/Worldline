# M559 double extender set

M559 opens the official two-piston double-extender boundary. It clones the
M142/M367 west-facing piston family and sequences sticky piston `29` then
normal piston `33` so one cobble payload travels two cells.

One headless session builds both arms on the raised stone column. Lever
Packet15 first extends sticky `29` and shifts the regular piston plus cobble
one cell, then extends piston `33` so cobble travels a second cell. Those
final cells remain after a clean save plus fresh login.

Frozen semantic SHA-256:
`49d44fb82433fdcf9dcf8ca5201aa946b783e9d3539c9689d6a2284af36fac0f`.

This is distinct from M145 two-block payload on one piston (one-cell
two-material shift) and from M147 twelve-block push capacity. It does not
claim retraction of the extender, quasi-connectivity, 0-tick pulses, slime,
or a generic piston model.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
