# M147 qualification cycle

`PistonPushLimitCycle` runs the twelve-block capacity fixture and thirteen-block
rejection fixture twice, using four fresh official server JVMs and eight client
sessions. Both matched pairs must reproduce the exact chain and raised-volume
digests plus one combined semantic trace.

The frozen semantic SHA-256 is
`6fd354f14bc191c11fd670b0d58e6aa0b86072feec3bb2322261cef951ca1a54`.
