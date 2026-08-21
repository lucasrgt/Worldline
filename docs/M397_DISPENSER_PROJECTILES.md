# M397 dispenser projectiles

M397 opens the official dispenser-projectile SET. Packet15 places dispenser
`23:4` on a raised stone column. The Trap window (Packet100 type 3) accepts
snowball `332` then egg `344` via Packet102. A side lever `69:1` attached
to the support stone powers to `69:9` and the dispenser ejects both stacks
as Packet23 types `61` and `62` on the existing object tracker. Reopening
the Trap window shows owned slots 0 and 1 empty.

This compounds M231 place-only, M153/M333 Packet21 item ejects, and M331
player air-use throwables: the frozen signal names multiple projectile
types `61` and `62` from dispenser loads `332` and `344`. It does not claim
arrows, fishing hooks, cobble/plank drops, hit damage, egg hatch, inventory
decrement hashing, or a second Packet23 tracker. Headless `B173WireClient`
protocol-14 only. No GUI. No Aero.

The frozen semantic SHA-256 is
`66d497bee36abdc673c44336dad9a75afcc08fcf7ade36676c652023100b1731`.
