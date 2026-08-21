# M417 remaining TNT place

M417 opens the official remaining TNT place family as a two-cell chain.
Packet15 places two TNT blocks `46:0` on a raised stone support, two cells
apart. Packet15 of flint-and-steel item `259` primes only the first cell.
After the official fuse, protocol-14 Packet60 fires at TNT strength `4` and
the second TNT cell is also primed then exploded.

The frozen signal names both `46` cells, flint `259`, and Packet60. This is
distinct from shipping M219 (one unprimed place), M137 (one isolated Packet60
detonate), and sibling M381 (one TNT prime plus Packet23 type `50`). It is
not a Nether-bed explosion; strength is `4`, not bed strength `5`.

This milestone does not claim exact blast rays, a deterministic destroyed-cell
count, entity damage, knockback, or fire spread. Headless `B173WireClient`
protocol-14 only. No GUI. No Aero. No version bump.

The frozen semantic SHA-256 is
`153e7f2258e4d355e0e2c070a630aebe6dfa4262d98a3e4aa3e99b8f99e0205d`.
