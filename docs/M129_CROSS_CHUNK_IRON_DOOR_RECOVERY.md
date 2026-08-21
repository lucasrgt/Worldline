# M129 cross-chunk iron-door recovery

Status: GO in Worldline v1.117.0.

M129 completes M128's consumer cycle. A fresh witness observes the open door
and powered lever, deactivates the lever once, and a separate final reader
observes the exact closed metadata for both official door blocks.

The final door and lever chunks are state-for-state equal to the original
closed snapshots.

M129 proves recovery only for this bounded adjacent lever/door topology. It
does not claim arbitrary networks, other consumers, unloaded chunks,
propagation timing, or a Worldline redstone evaluator.
