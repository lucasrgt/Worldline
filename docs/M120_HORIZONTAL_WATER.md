# M120 horizontal water

Status: GO in Worldline v1.108.0.

M120 builds a bounded two-cell trench above the stabilized official world.
An exact server-seeded water block item 9 establishes source `9:0`; dirt `3:0`
closes the only horizontal exit. Stone floor and walls make that exit the sole
legal flow destination.

After 200 fixture ticks, a fresh Packet51 supplies the authoritative baseline.
Packet14 opens the dirt cell and Packet53 confirms transient air. Forty flow
ticks produce `9:1` in the target while retaining source `9:0`; another fresh
Packet51 agrees. Exactly one complete-chunk state changes, with ordered delta
SHA-256 `b04b6593e9708c471e970cac23a8f32913f3de2300e56b463a1a53638c8ffc62`.

M120 proves one bounded horizontal water transition. It does not claim natural
bucket interaction, arbitrary shorelines, branching flow, distance decay,
source formation, infinite-water rules, lava, mixing, cross-chunk behavior,
tick-exact latency or a Worldline fluid simulator.
