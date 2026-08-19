# M144 sticky-piston pullback

M144 adds the exact sticky contrast to M143's normal-piston retention. It uses
only existing Worldline protocol boundaries and leaves the mechanic entirely
inside the official Beta 1.7.3 server.

The raised fixture substitutes sticky piston item/block `29` while preserving
the same west-facing geometry, stone payload and side lever. After activation,
save and fresh login, the treatment client requires base `29:12`, sticky head
`34:12`, and stone in the displaced cell.

One empty-hand Packet15 deactivates the lever. The settled server state returns
the base to `29:4`, replaces the head cell with the pulled stone `1:0`, and
empties the displaced cell. Another clean save and fresh Packet51 prove that
pullback independently.

The raised digest contains exactly four transitions: lever, base, head-to-stone
and stone-to-air. Water below the artificial tower remains outside the causal
scope, matching M142 and M143.

This milestone does not claim two-block sticky chains, short pulses, block-drop
mobility, immovable targets, push limits, quasi-connectivity, collisions,
cross-chunk motion, animation timing, sound, or generic piston semantics.
