# M509-SW behavior map

The differential fixture places active redstone torch 76:5 at `(8,65,8)`, a
dust source at `(9,65,8)`, a junction at `(10,65,8)`, and north/south branches
at `(10,65,7)` and `(10,65,9)`. Vanilla neighbor notifications compute exact
metadata for the complete T topology.

The negative phase removes only the south branch and proves the north branch
retains its oracle metadata. The mutation removes the torch source and proves
the remaining component settles to metadata zero. Mapped and official-JAR
processes run the same setBlock-with-notify and World.tick path.

This milestone does not claim straight-line attenuation generally, cross-chunk
propagation, repeaters, torch inversion, consumers, persistence, or arbitrary
circuits.

Frozen expected signature SHA-256: 6aca9f501946f63db33fca4ed618e472fead629bc7a86847355bee55802f33f0
