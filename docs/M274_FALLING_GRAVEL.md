# M274 falling gravel

M274 opens the official one-cell falling-gravel boundary. Supported gravel
`13:0` is the M218 identity. A gravel block is placed above the stabilized
stone column and held motionless for forty ticks. Packet14 then removes
its `1:0` support and Packet53 confirms the transient lower air cell.

After forty gravity ticks, the official server settles gravel into the
lower cell and clears the original upper cell. A clean disconnect/save
followed by a fresh login and Packet51 reproduces both results. Exactly
two complete-chunk states differ: lower `1:0 -> 13:0` and upper
`13:0 -> 0:0`. The ordered delta SHA-256 is
`a919f7bd5ed11b66e9dfd6fb45f0e12ca9da52352113f3021401560c8c57c2e4`.

M274 proves one one-block gravel fall after support removal. It is
distinct from M119 falling sand. It does not claim a generic gravity
engine, falling-entity packets, sand, long falls, flint drops, entity
collisions, piston interaction, unloaded chunks, cross-chunk movement,
tick-exact latency or client rendering.
