# M553 piston immovable set

M553 opens the official compound piston-immovable boundary. It clones the
M146 west-facing piston-`33` rejection fixture and the M367 family layout
into one SET: chest `54`, furnace `61`, and mob spawner `52`.

One headless session builds all three west-facing arms on the raised
stone column. Lever Packet15 powers each piston `33`. The payloads stay,
the pistons stay retracted `33:4`, and the destination cells stay air.
Those final cells remain after a clean save plus fresh login.

Frozen semantic SHA-256:
`6b35bf7c4b6f658370491bc20505538a93425b8309bc17d26f9d8b3d19ff06cf`.

This is distinct from M146 obsidian-only (`49:0`) and from M147's twelve-
block push limit. It does not claim note blocks, dispensers, sticky
retraction, quasi-connectivity, or a generic piston model.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
