# M172 wooden pressure plate

M172 opens the official wooden pressure-plate boundary. Item `72` is placed on a
raised stone support as block `72:0`. `moveAndObserve` onto that cell lets the
official server power the plate to `72:1` through Packet53. Stepping off
depowers it to `72:0`, which is the persisted state after a clean save and
fresh login.

Frozen semantic SHA-256:
`ffcac8ad53202102f7e7ff5179823d53d8ecd116c879faba5a3c1ccf9bcd94c1`.

This milestone does not claim stone plates, detector rails, or entities other
than the actor.
