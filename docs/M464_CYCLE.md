# M464 qualification cycle

`ExplosionPlayerHurtSetCycle` rebuilds a raised-stone pad in two fresh
official server JVMs. Each run retargets a spawner to `Creeper`, requires
Packet60 strength `3` plus a Packet8 health drop, restores with golden
apple `322`, then Packet15-ignites TNT `46` at a 3-block standoff and
requires Packet60 strength `4` plus a second Packet8 drop. The actor
survives both. One official EOF or Packet8 miss is retried after a 5
second sleep.

The frozen signal must name Packet60 strength `4`, Packet60 strength `3`,
Packet8, type `50`, and `survived=true`. It must not collapse to M137 TNT
crater cells, M391 wool/dirt crater, or M359 Nether-bed strength `5`.

Run directly with:

```text
java tools/smoke/ExplosionPlayerHurtSetCycle.java m464-explosion-player-hurt-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`57b40f0692f328b32fc02a568a0f47854d1d21fda0e2ed383262bf03c2b8c078`.
Headless protocol-14 only. No GUI. No Aero.
