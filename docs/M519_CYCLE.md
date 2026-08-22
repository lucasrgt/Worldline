# M519 qualification cycle

M519 compares seeded dispenser reservoir sampling across mapped and official
Beta 1.7.3 tile entities. Fourteen draws must select only occupied inventory
members and remove exactly one item; single-slot and empty controls close the
boundary.

The expected signature is
`ee4be352a5e761e0091c6c9206d8771151003ad7b2ba731c4b32bf6455dfc8fe`.
Qualify it with `java tools/harness/Gate.java --milestone
m519-sw-dispenser-rng-membership`.
