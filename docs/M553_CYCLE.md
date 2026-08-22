# M553 qualification cycle

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
