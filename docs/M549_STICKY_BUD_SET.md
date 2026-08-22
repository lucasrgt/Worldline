# M549 sticky BUD set

M549 opens the official sticky BlockPistonBase BUD boundary. A sticky
piston `29` is quasi-powered by a lever-powered block diagonal-above it
without a piston-cell update, so it does not extend until a neighbor
update. There is no continuous power on the piston cell itself.

One headless session builds a west-facing sticky piston `29:4` with
stone payload, then a north-face lever on a stone at `(5,66,4)`. The
primed arm stays `29:4` while the lever is on (`69:12`). Packet15 of
stone north of the piston BUD-extends `29:4 -> 29:12` with sticky head
`34:12`. Unpowering the lever then sticky-pulls. Those final cells
remain after a clean save plus fresh login.

Frozen semantic SHA-256:
`d9de32a7e37b272dd97be1d211464f0bf67b7b66ba71ddedbf3742d0f345747b`.

This is distinct from M547 sticky QC (lever on the block directly above,
immediate extend), M548 regular piston-`33` BUD, M367 lever-on-support
motion, and shipping M142-M144 1:1 single-arm cycles. It does not claim
two-block chains, push limits, obsidian rejection, or a generic piston
model.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
