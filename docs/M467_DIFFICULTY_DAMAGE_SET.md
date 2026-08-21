# M467 difficulty damage set

M467 opens the official difficulty-damage SET. `B173ServerProperties`
writes an extracted `difficulty=` accessor. The same zombie type `54`
melee is observed on Easy (`difficulty=1`) then Hard (`difficulty=3`)
with Health `20` restored between those boots. Packet24 type `54` plus
Packet38 status `2` precede Packet8.

Official dedicated-server `spawn-monsters=true` stores world difficulty
`1`, so EntityLiving's Easy branch `i/3+1` scales zombie `attackStrength`
`5` to Packet8 damage `2` (`20 -> 18`) on both property boots. The Hard
`i*3/2` branch is not reached on this dedicated server. The frozen
signal names both difficulties and `delta=2+2`.

This family is distinct from M451 armor reduction, M454 peaceful
despawn, and M446 door break. It does not claim client `gameSettings`
difficulty, Normal `2`, or armored absorption.

The frozen semantic SHA-256 is
`61e1ac15b1e84c70af6ec58f615e81db3d5a6ae0c3deaac931da803a16f459d7`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
