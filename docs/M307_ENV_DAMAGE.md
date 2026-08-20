# M307 environmental damage

M307 is a compound official environmental-hurt SET. One headless
`B173WireClient` session records at least three Packet38 status 2 / Packet8
health drops without death:

- two-deep still-water drowning (`20 -> 18`)
- falling-sand head-cell suffocation (`19 -> 18`)
- still lava `11:0` from lava bucket `327` (`19 -> 15`)

Vanilla Beta 1.7.3 owns air-depletion drowning `attackEntityFrom(..., 2)`,
in-wall suffocation, and lava `attackEntityFrom(..., 4)`. Lava is last so
lingering fire cannot contaminate the other two hurts. Health 15 persists
after a clean save plus fresh login.

This is distinct from M275 cactus contact, M276 fire standing-damage, M299
drowning, M300 fall, M301 lava, and M330 suffocation, which each freeze one
cause. M307 freezes drowning, suffocation, and lava together.

Frozen semantic SHA-256:
`8a51289b35f57567a0dfbc0f3cf8f1d6981dac6219b52d494aac34f56713cba7`.

This milestone does not claim fall damage, cactus, armor reduction, death, or
respawn.
