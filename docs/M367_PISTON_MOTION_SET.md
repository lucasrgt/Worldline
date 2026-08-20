# M367 piston motion set

M367 opens the official compound piston-motion boundary. It clones the
M142 piston-`33` extension, M143 piston-`33` retraction, and M144 sticky
piston-`29` pull into one family cycle.

One headless session builds both west-facing arms on the raised stone
column. Lever Packet15 extends then retracts normal piston `33` with
stone retained, then extends and pulls with sticky piston `29`. The
frozen signal includes `extend`, `retract`, and `sticky-pull`. Those
final cells remain after a clean save plus fresh login.

Frozen semantic SHA-256:
`eeb597ce51f18b3841a00e606375efae5dfb531672564e34670469f420f304a8`.

This is distinct from M293/M294 place-only (`29:1` / `33:1` with no
motion) and from shipping M142-M144 1:1 single-arm qualifications. It
does not claim two-block chains, push limits, obsidian rejection,
quasi-connectivity, or a generic piston model.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
