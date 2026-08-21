# M359 bed nether explode

M359 qualifies the official Beta 1.7.3 dedicated-server Nether bed family as
one compound SET. A dimension `-1` player seed logs in through the M130
Nether profile, Packet15-places item `355` as block `26` on netherrack, then
empty-hand Packet15-uses that bed.

`WorldProviderHell.canSleepHere` is false, so `BlockBed.blockActivated` does
not emit Packet17. It removes the bed and emits protocol-14 Packet60 at
strength `5`. That is explode rather than sleep, and it is distinct from M137
TNT strength `4`.

The frozen signal names both dimensions: Overworld `0` sleeps (M330), Nether
`-1` explodes (this milestone). The live cache and a fresh login show the bed
halves gone.

Frozen semantic SHA-256:
`be77b379de881712f9089340681a1a0779977df7934e51508858f83c97a9a7a6`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
