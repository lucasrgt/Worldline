# M291 spruce leaves

M291 opens the official spruce-leaves placement boundary. Packet15 of
spruce log item `17` damage `1` on the east face of the M175 raised
stone, then Packet15 of leaves item `18` damage `1` on the top face,
places leaf `18:9` beside log `17:1`. That exact spruce cell remains
after a clean save plus fresh login.

Official leaves decay without nearby wood, so the hashed path keeps
spruce log `17:1` adjacent and freezes the leaf identity that actually
persists (`18:9`, spruce species plus the decay-check bit). This
milestone is distinct from M209 oak leaves `18:8`. It does not claim
decay-without-log as the hashed success path, birch leaves, or shear
drops.
