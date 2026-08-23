<!-- worldline-map-schema=1 -->
<!-- boundary=state-world-differential -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=ee4be352a5e761e0091c6c9206d8771151003ad7b2ba731c4b32bf6455dfc8fe -->

# M519-SW mapping

Official class `ak` maps to `TileEntityDispenser`. Its private `Random b` drives one-pass reservoir sampling over the nine inventory slots. The differential seeds that exact RNG, loads three non-projectile members, and proves that every draw belongs to an occupied slot while exactly one item is removed. Single-slot and empty controls close the boundary. M333 and M397 independently freeze the multiplayer Packet21 and projectile wire paths.

Frozen expected signature SHA-256: ee4be352a5e761e0091c6c9206d8771151003ad7b2ba731c4b32bf6455dfc8fe

## Frozen semantic signal

`oracle=MATCH,fixture=m519-sw-dispenser-rng-membership,ticks=14,controlled=true`
