# M430 qualification cycle

`RemainingPaintingMotivesCycle` rebuilds raised 4x2, 4x3, and 4x4 stone
walls in two fresh official server JVMs. Each run places painting item
`321` on the west face of each remaining size and correlates Packet25
across two peers. The frozen signal includes `sizes=4x2+4x3+4x4` and three
Packet25 observations, all direction `1`. One official EOF is retried
after a 5 second sleep.

Run directly with:

```text
java tools/smoke/RemainingPaintingMotivesCycle.java m430-remaining-painting-motives
```

The frozen semantic SHA-256 is
`1504c14913948dca32f92c0dacff830c42a51f7c402354b7a872fc92af410e09`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
