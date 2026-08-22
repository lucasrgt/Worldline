# M566 grass spread set

M566 qualifies official grass spread from block `2` onto adjacent dirt
`3` as one SET. Packet15 only builds the raised-stone pad: an 8-cell
grass ring, lit dirt samples, and one stone-covered dirt cell. Random
ticks then emit Packet53 `3->2` on a lit dirt sample. A dirt cell
covered by stone stays `3`. The frozen signal names `spread=3->2` and
`covered-stay=true`. Converted cells survive a clean save plus fresh
login.

This family is distinct from shipping M238 grass place and M223 dirt
place. It does not claim farmland, hoe conversion, mycelium, or a
generic spreading-block model. Headless `B173WireClient` protocol-14
only. No GUI. No Aero.

Exact wait length and which lit sample converts are not hashed.

Frozen semantic SHA-256:
`b80a81abefd273cf68a6495d0a825f6556e85176324d77bc1702832ffc448174`.
