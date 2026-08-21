# M145 two-block piston push

M145 extends the qualified normal piston from one payload block to a chain of
two distinct materials. It adds no API or local mechanics.

The west-facing piston receives stone `1:0` followed by cobblestone `4:0` and
an empty destination. Distinct materials make every position transition
observable; two identical blocks would hide the middle displacement.

Lever activation settles to extended base `33:12`, head `34:4`, stone in the
former cobblestone cell, and cobblestone in the former air cell. A fresh client
after clean save proves all five raised state changes: lever, base and three
payload cells.

Generated water below the raised support is excluded exactly as in M142-M144.
This milestone does not claim longer chains, the twelve-block limit, sticky
retraction, immovable or breakable payloads, tile entities, cross-chunk motion,
transient moving blocks, collisions, timing, sound, or generic piston logic.
