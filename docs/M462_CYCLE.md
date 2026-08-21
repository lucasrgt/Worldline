# M462 qualification cycle

`BowMobHitSetCycle` rebuilds the raised grass pen in two fresh official
server JVMs. Each run air-uses bow `261` so Packet23 type `60` hits
Packet24 pig `90`, then retargets the same spawner to `Zombie` at night
`14000` so type `54` also records Packet38 status `2`. One official EOF
is retried after a 5 second sleep.

The frozen signal must name bow `261`, type `60`, pig `90`, zombie `54`,
and `packet38-status2`. It must not collapse to M436 collect, M332
crafts, or skeleton-shot arrows.

Run directly with:

```text
java tools/smoke/BowMobHitSetCycle.java m462-bow-mob-hit-set
```

The frozen semantic SHA-256 is
`bbe6e87049578c8e26c8cca6f79ed7ac1f3c530df498b2d9da63a8f195578e22`.

Canonical evidence uses two official server JVMs and two client sessions
per JVM (fixture plus hostile reload). Headless protocol-14 only. No GUI.
No Aero.
