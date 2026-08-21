# M386 ice snow melt set

M386 opens the official compound ice-and-snow light-melt boundary. One
headless session Packet15-places ice item `79` and snow layer item `78`
beside floor torch `50:5` so official block light plus random ticks melt
ice to still water `9:0` and snow to air `0:0`.

The ice leftover is distinct from M193 ice place. After the bounded
official-tick wait the support-top cell leaves `79` and persists as still
water `9:0`. The snow leftover is distinct from M194 snow-block place and
from M203 snow-layer place. After the same wait the west-wall-top cell
leaves `78` and persists as air `0:0`. This compound is distinct from
M308, which Packet14-breaks ice and glass and torch-melts a second ice.
Exact melt delay is not hashed.

This milestone does not claim silk-touch, snow block melt, snowfall, or
slipperiness. Headless `B173WireClient` only. No GUI. No Aero.

Frozen semantic SHA-256:
`00d10f8cca091d8efcf6f005b84e192d110161deafabfb6a71d69862a5de6b7a`.
