# M142 piston extension

M142 qualifies one deterministic Beta 1.7.3 piston transition on the
hash-verified official dedicated server. It adds no public API and no local
redstone or piston simulation.

The smoke raises a stone support above the generated water column, places a
normal piston `33:4` facing west, places one stone block immediately in front
of it, and attaches an unpowered side lever `69:1` to the support. The actor's
fixed yaw and the exact piston metadata prove the orientation before treatment.

One empty-hand Packet15 activates the lever. After the official block event
settles, the base is extended `33:12`, the former stone cell is piston head
`34:4`, and the next west cell contains the displaced stone `1:0`. A clean
disconnect/save and fresh client Packet51 prove the same four states again.

The immutable delta is scoped to the raised fixture from support Y upward.
Generated water below the artificial tower may normalize during neighbor
updates and is deliberately not attributed to the piston contract. Within the
declared raised volume exactly four cells change: lever, base, head and pushed
destination.

This milestone does not claim sticky-piston behavior, retraction, multi-block
pushes, the twelve-block limit, immovable blocks, quasi-connectivity,
cross-chunk motion, collision behavior, transient moving block `36`, animation
timing, sound, or a generic redstone evaluator.
