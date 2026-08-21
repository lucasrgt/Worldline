# M351 qualification cycle

`PaintingOrientSetCycle` rebuilds two raised 2x2 stone walls in two fresh
official server JVMs. Each run places painting item `321` on the west
face and the east face and correlates Packet25 across two peers. The
frozen signal includes multiple facing values. One official EOF is
retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/PaintingOrientSetCycle.java m351-painting-orient-set
```

The frozen semantic SHA-256 is
`8f60b715dc6a3aeab49aaae89f1f147dd7822ab37806a8da79597e86acd2e9aa`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
