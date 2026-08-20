# M333 dispenser set

M333 opens the official dispenser-set boundary. Packet15 places dispenser
`23:4` on a raised stone column. The Trap window (Packet100 type 3) accepts
cobblestone `4:0` then oak planks `5:0` via Packet102. A side lever `69:1`
attached to the support stone powers to `69:9` and the dispenser ejects both
stacks as Packet21. Reopening the Trap window shows owned slots 0 and 1 empty.

This compounds M231 place-only and M153 single-item eject: the frozen signal
includes dispenser `23` plus ejected item ids `4` and `5`. It does not claim
arrows, bonemeal, buckets, TNT, multi-item RNG identity, quasi-connectivity,
or hopper insertion.

The frozen semantic SHA-256 is
`46b62a083dad7f0e54a72e16e9b51144add22acb4cb53b75b51439b04385894e`.
