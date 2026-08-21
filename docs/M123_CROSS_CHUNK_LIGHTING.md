# M123 cross-chunk lighting

Status: GO in Worldline v1.111.0.

M123 establishes the first causal vanilla transition across a chunk boundary.
At global `x=15`, Packet15 replaces water `9:0` with glowstone `89:0` while
the directly adjacent cell at `x=16` remains water in the neighboring chunk.

After forty official heartbeats, a fresh reader receives both complete chunks.
The glowstone sample changes from block light 0 to 15 and the neighboring water
sample changes from 0 to 12. The source chunk has exactly 55 increased samples;
the neighboring chunk has 19. Ordered delta hashes reproduce in two fresh
worlds, while both skylight planes remain unchanged.

M123 proves one eastward boundary crossing through water. It does not claim
arbitrary sources, materials, directions, chunk-loading orders, removal/recovery,
light-engine timing, skylight propagation, unloaded-chunk behavior, dimensions,
or a Worldline lighting implementation.
