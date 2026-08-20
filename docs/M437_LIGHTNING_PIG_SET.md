# M437 lightning pig set

M437 qualifies the official Beta 1.7.3 pig / zombie-pigman identity pair as
one compound SET. Overworld Packet24 type `90` (`EntityPig`) and Nether
Packet24 type `57` (`EntityPigZombie`) are the two identities that vanilla
lightning conversion would join. Official weather lightning is not a
deterministic headless path: Packet71 thunderbolts occur at
`nextInt(100000) == 0` per loaded chunk, and no protocol-14 client packet
can summon them. Lightning is Packet71, not Packet23; this SET does not
invent a Packet23 tracker.

The frozen signal names type `90` and type `57` together with Overworld
dimension `0` plus Nether `-1`. This is distinct from M411's Nether-only
pigman plus cooked-pork `320` drop.

Frozen semantic SHA-256:
`536016d5292cf2d747ea4a029011726719795579c19e8507ec912154e9bd77db`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
