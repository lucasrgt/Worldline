# M387 remaining light set

M387 opens the official compound remaining-light-emission set. Packet15
places glowstone `89:0` on a raised stone support, jack-o-lantern `91:1`
from actor look yaw `-90` on the west pad, and floor torch `50:5` on the
east pad. The frozen signal includes all three light cells (`89+91+50`).
Those exact cells remain after a clean save plus fresh login.

This is distinct from shipping M175 (torch only), M190 (jack place only),
M191 (glowstone place only), and M356 (jack craft). It does not claim
wall-torch facings, redstone torch, light-plane hashing, pumpkin `86`, or
glowstone-dust drops. Headless `B173WireClient` protocol-14 only. No GUI.
No Aero.

The frozen semantic SHA-256 is
`c8fb22dfee19b993ff3351bf0dfcb8de29c0975c84ee50c94848cd2d0e4c6d70`.
