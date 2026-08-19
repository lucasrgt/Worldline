# M135 Player respawn

Status: GO in Worldline v1.123.0.

M135 adds a typed same-dimension death/respawn boundary. An empty player starts
below the official world's void threshold; server-authored damage drives its
Packet8 health value nonpositive. `respawn()` then writes the exact two-byte
Packet9 request, requires a fresh inbound Packet9 epoch in the same dimension,
and waits for authoritative health `20`.

The official server chooses the respawn coordinate. The client accepts its
corrected pose, decodes the containing Overworld chunk, observes skylight and
an empty inventory, then cleanly disconnects and proves persisted health `20`.
The public `RemoteRespawn` normalizes any nonpositive overkill value to the
semantic dead state `0`; the packet tracker retains the signed wire health.

M135 does not claim bed/spawn selection, hardcore behavior, item drops,
experience, score, death messages, PvP attribution, cross-dimension respawn,
client rendering, or arbitrary repeated deaths.
