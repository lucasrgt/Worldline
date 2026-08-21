# M120 qualification cycle

`HorizontalWaterCycle` verifies the official server artifact, compiles the
published API, adapter and smoke, and runs two fresh official worlds. Each
world uses constructor, treatment and verifier sessions so both sides of the
delta are authoritative Packet51 snapshots rather than stale incremental
caches.

The constructor creates the exact trench/source/gate fixture and settles it for
200 ticks. The treatment session requires source `9:0`, opens the sole dirt
exit, and observes target `9:1` after forty ticks. The verifier must reproduce
both cells, and the complete-chunk delta must contain exactly the target. Both
worlds must produce identical evidence, trace and signature. Diagnostic mode
cannot qualify.

Canonical evidence uses two official server JVMs and six client sessions. The
frozen semantic SHA-256 is
`c0bbf83eadc6fd56c3697b50ed2d653aebc2fd9e132467354a9bcae89a6daa29`.
