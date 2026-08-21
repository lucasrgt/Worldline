# M317 slow blocks

M317 opens the official cobweb-and-soul-sand slowdown boundary. It clones
the M195/M192 raised stone fixture: Packet15 of cobweb item `30` places
block `30:0` in the actor's path, and Packet15 of soul sand item `88`
places block `88:0` on the west face. The headless actor then
Packet13-walks the same intended step in air, inside cobweb, and on soul
sand, and freezes standing pose deltas over eight ticks.

The same intended step is slower inside live cobweb `30:0` (`250`
milli-blocks) and on soul sand `88:0` (`400` milli-blocks) than in air
(`1000` milli-blocks). Two official server JVMs must match.

Frozen semantic SHA-256:
`bcae75456216b2655361256edd97079669619d908394782145a21a076e9e676a`.

This is distinct from M195 cobweb placement, M192 soul-sand placement, and
M331 cobweb slow. M195 and M192 only prove the planted cells. M331 does
not claim soul sand. This milestone does not claim sword-break cobweb or
string drops. Headless `B173WireClient` only. No GUI. No Aero.
