# M352 tool durability set

M352 opens the official compound pick-durability boundary. One headless
session Packet14-breaks cobble `4:0` and stone `1:0` with at least two
pick materials from wooden `270`, iron `257`, and gold `285`. The frozen
signal includes those tool IDs plus remaining durability damage on each
held stack after a clean save plus fresh login.

This milestone is distinct from M300 ore pick breaks, which only claim
Packet21 cobble, coal, and diamond drops and explicitly do not claim
pickaxe durability. M352 does not claim ore harvest, fortune, bare-hand
rejection, or tool break-on-zero.

Frozen semantic SHA-256:
`46cbf98b50d0745eafee30276fb3d3adafbbd1381f71bf7106012dbe80b75a30`.

Headless `B173WireClient` only. No GUI. No Aero.
