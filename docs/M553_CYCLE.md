# M553-PISTON-IMMOVABLE-SET Piston immovable set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

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

## Qualification cycle

`PistonImmovableSetCycle` rebuilds the cloned piston-`33` chest, furnace,
and mob-spawner arms in two fresh official server JVMs. Each run powers
all three west-facing pistons and reloads the unchanged payloads after
save plus fresh login. The frozen signal includes chest `54`, furnace
`61`, and spawner `52` with retracted piston `33:4->4`. One official EOF
is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`6b35bf7c4b6f658370491bc20505538a93425b8309bc17d26f9d8b3d19ff06cf`.

Run directly with:

```text
java tools/smoke/PistonImmovableSetCycle.java m553-piston-immovable-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=10,chest=54:0->54:0,furnace=61:4->61:4,spawner=52:0->52:0,piston33=4->4,chest-arm=4:65:4:33:4->4,payload=3:65:4:54:0->54:0,dest=2:65:4:0:0->0:0,furnace-arm=4:65:6:33:4->4,payload=3:65:6:61:4->61:4,dest=2:65:6:0:0->0:0,spawner-arm=4:65:8:33:4->4,payload=3:65:8:52:0->52:0,dest=2:65:8:0:0->0:0,retracted=true,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `6b35bf7c4b6f658370491bc20505538a93425b8309bc17d26f9d8b3d19ff06cf`.
