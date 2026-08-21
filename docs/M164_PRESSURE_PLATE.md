# M164 stone pressure plate

M164 opens the official stone pressure-plate boundary. Item `70` is placed on a
raised stone support as block `70:0`. `moveAndObserve` onto that cell lets the
official server power the plate to `70:1` through Packet53. Stepping off
depowers it to `70:0`, which is the persisted state after a clean save and
fresh login.

Frozen semantic SHA-256:
`ab14f3bebb0157e814af07dd4950065b472c5d5b99f25736c02b57fd08b1f754`.

This milestone does not claim wooden plates, detector rails, or entities other
than the actor.
