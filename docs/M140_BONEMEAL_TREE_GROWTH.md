# M140 Bonemeal tree growth

Status: GO in Worldline v1.128.0.

M140 adds the first official player-triggered vegetation generation boundary.
A raised dirt fixture carries oak sapling `6:0`. A fresh client applies
bonemeal `351:15` through Packet15 and observes the root become oak log `17:0`.
After clean save, another client requires the same root plus at least four logs
and ten leaves in the bounded tree region.

The root transition alone is hashed because the precise oak height and canopy
are chosen by vanilla's world random state. The structural trunk/canopy bounds
prove a real tree rather than a single substituted block without pretending a
particular random geometry is universal. Two fresh official servers reproduce
the normalized evidence.

M140 does not claim natural random-tick growth, exact tree geometry, other
sapling species, giant trees, leaf decay, drops, cross-chunk growth, arbitrary
fertilizers, growth timing below the bounded window or unrelated chunk
stability.
