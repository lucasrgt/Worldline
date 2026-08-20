# M308 fragile set

M308 opens the official compound fragile-block boundary. One headless
session Packet14-breaks ice item `79` and glass item `20`, then places a
second ice beside torch item `50` so official random ticks melt that ice
to still water `9:0`.

The west ice leftover is distinct from M193 ice place. After Packet14
the cell leaves `79`; beside the later melt source it persists as still
water `9:0`. Glass-break leftover is air `0:0` with no Packet21 glass
stack, distinct from M196 glass place. Torch melt is still water `9:0`
at the support-top ice, distinct from ice placement without melt.
Exact melt delay is not hashed.

This milestone does not claim silk-touch, glass pane, stained glass, or
slipperiness. Headless `B173WireClient` only. No GUI. No Aero.
