# M379 iron door set

M379 compounds official iron-door placement with the official redstone
open/close pair on one raised stone fixture. Iron door item 330 is placed
with Packet15 as lower `71:0` and upper `71:8`. A side lever on the same
support's east face stabilizes at unpowered `69:1`. Empty-hand Packet15
powers that lever to `69:9` and opens both door halves to `71:4` / `71:12`.
A second empty-hand Packet15 returns the lever to `69:1` and closes both
halves to `71:0` / `71:8`. The closed door and unpowered lever remain after
a clean save plus fresh login.

The frozen signal includes both block-71 cells and the powered/unpowered
metadata pair (`71:0->4->0`, `71:8->12->8`, `69:1->9->1`).

Frozen semantic SHA-256:
`9d887adb7cbebcca0c805d02f84507310ea3211b6e1abb774ec7e7ae8d3e4f0c`.

This milestone is distinct from M241 (place only), M118 (redstone open
only), and M306 (wooden door `64` plus trapdoor `96`). It does not claim
empty-hand iron-door toggle, wooden doors, stacking, hinge or facing
variants beyond this official placement, double doors, indirect-power
topology, or the Nether. Headless `B173WireClient` only. No GUI. No Aero.
