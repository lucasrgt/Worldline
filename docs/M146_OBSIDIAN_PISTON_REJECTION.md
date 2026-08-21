# M146 obsidian piston rejection

M146 qualifies one official Beta 1.7.3 immovable-payload boundary. It adds no
API, adapter behavior or local piston model.

A west-facing normal piston `33:4` is built against obsidian `49:0`, with air
behind the payload and a side lever. Activating the lever changes only its
metadata from `1` to `9`: the piston remains retracted, obsidian remains in
place and the destination remains air.

The oracle compares the raised fixture before treatment with a fresh client
after clean save. Its exact one-cell digest contains only the powered lever.
Generated water below the raised support is excluded, matching M142-M145's
causal boundary.

This milestone does not generalize to other immovable or breakable blocks,
tile entities, sticky pistons, the twelve-block push limit, quasi-connectivity,
cross-chunk motion, transient moving block `36`, collisions, timing, sound or
a generic piston evaluator.
