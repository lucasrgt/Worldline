# M428 remaining door orient set

M428 opens the official remaining wooden-door hinge/face family. Packet15
of wooden door item `324` places both halves of block `64` from four look
yaws in one session: `-90` writes lower `64:0` plus upper `64:8`, `0`
writes `64:1` / `64:9`, `90` writes `64:2` / `64:10`, and `180` writes
`64:3` / `64:11`. The frozen signal names those four upper+lower `64:8`
bit-family pairs. All eight cells survive a clean save plus fresh login.

This is distinct from shipping M162 (one west-look `64:0/8` place and
toggle), from shipping M277/M306 (open/close of that one facing plus
trapdoor `96`), and from shipping M379 (iron door `71` plus lever power).
It does not claim iron doors, trapdoors, redstone, open-bit `+4`
transitions, or double-door hinge pairing. Headless `B173WireClient`
protocol-14 only. No GUI. No Aero.

The frozen semantic SHA-256 is
`10dad6f6b34f4140a80e7a09abeebaa5ff502bc6eee4607964a64dae72626bd2`.
