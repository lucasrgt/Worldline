# M127 cross-chunk redstone recovery

Status: GO in Worldline v1.115.0.

M127 completes M126's causal cycle. A fresh witness first observes lever
`69:9` in chunk `(1,0)` and wire `55:15` in chunk `(0,0)`. It deactivates the
lever once, after which a separate reader observes `69:1` and `55:0`.

Each complete chunk changes at exactly one coordinate. Both final snapshots
are state-for-state equal to the original unpowered snapshots.

M127 proves exact recovery for this bounded adjacent lever/wire topology. It
does not claim arbitrary redstone networks, propagation timing, unloaded
chunks, repeaters, torches, consumers, or a Worldline redstone evaluator.
