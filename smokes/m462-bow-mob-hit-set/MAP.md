# M462 behavior map

A raised 7x7 grass pad with a fence `85` pen places a default pig spawner
`52`. Seeded bow `261` plus arrows `262` air-use through Packet15 so
Packet23 type `60` hits Packet24 type `90` and records Packet38 status `2`.
The same spawner is then retargeted to `Zombie` at night `14000` so type
`54` receives the same player-arrow hurt.

This map does not re-qualify M436 land-then-collect, M157/M332 shoot-only
type-60 identity, or skeleton-shot arrows M445. Headless `B173WireClient`
only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+fence85-pen+spawner52+bow261+arrow262|cause=packet15-air-bow261+nbt-entityid-zombie+time-14000|wire=packet24-type90+packet24-type54+packet23-type60+packet38-status2|oracle=player-arrow-hit-pig-and-zombie-not-m436-collect-or-m332-craft|column=17,platform=7x7-48grass+fence85,spawner=4:72:4:52:0,bow=261,arrow=262,mobs=type90+type54,night=14000,wire=packet23-type60+packet38-status2,thrower=actor,hits=pig90+zombie54,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`bbe6e87049578c8e26c8cca6f79ed7ac1f3c530df498b2d9da63a8f195578e22`.
