# M332 qualification cycle

`BowArrowSetCycle` rebuilds the raised workbench fixture in two fresh
official server JVMs. Each run crafts bow `261` and arrows `262` from
their vanilla recipes, air-uses the bow, and requires Packet23 type `60`.
One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`b745e8656e459e93ffe617759990be48c4c454450256e53f8ef1c5bf1757d215`.

Run directly with:

```text
java tools/smoke/BowArrowSetCycle.java m332-bow-arrow-set
```

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
