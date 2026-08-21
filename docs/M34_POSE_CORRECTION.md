# M34 Server-authoritative Pose Correction

M34 consumes official Beta 1.7.3 server Packet13 corrections during a sustained
protocol-14 session. The adapter decodes the server order (`x`, stance, feet,
`z`, yaw, pitch), validates that stance is above feet by less than two blocks,
and immediately acknowledges the packet in the client's feet-before-stance
order. The newest correction replaces the neutral channel pose and stance.

A byte-level fixture freezes both field orders and stance normalization. The
live smoke starts two fresh official servers, selects a nearby non-liquid solid
block from each decoded cache, and deliberately moves the client into its
center. Success requires an inbound correction, exact restoration of the
initial authoritative pose, and retention of the original cached chunk.
Sending an invalid movement is never evidence by itself.

## Non-claims

M34 does not model collision, gravity, fall damage, accepted paths, arbitrary
server teleports, entities, or server ticks. Corrections are applied at the
bounded `sustainTicks` pump boundary; this is not an asynchronous event API.
