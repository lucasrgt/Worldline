# M361 ladder climb set

M361 opens the official ladder-climb set. Packet15 of ladder item `65`
against a raised two-stone east face places two live `65:5` cells. The
headless actor Packet13-climbs that column at least two cells of height
(`climb=2000` milli-blocks) and contrasts the same ten-tick vertical
window in the air column before those ladders exist (`air=495`). The
frozen signal includes ladder `65` plus the climbed pose delta.

This is distinct from M174 ladder place-only, which freezes east facing
metadata and does not claim climbing physics. It does not claim waterlogged
ladders, sneak-clip, or a Worldline movement simulation.

Frozen semantic SHA-256:
`113dccdda9b6bd0140c7aea5b255db993bb9063c6d64ef9370f1fb9925c26340`.

Headless `B173WireClient` only. Protocol-14. No GUI. No Aero.
