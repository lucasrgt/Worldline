# M335 qualification cycle

`CakeSliceSetCycle` rebuilds the raised stone plus `BlockCake` fixture in
two fresh official server JVMs. Each run places cake item 354 as uneaten
`92:0`, eats three successive slices with empty-hand Packet15, and reloads
the bitten `92:3` block. The live signal must include `92:0->1->2`. One
official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/CakeSliceSetCycle.java m335-cake-slice-set
```

The frozen semantic SHA-256 is
`3ef77cdef925e0457ef17467a33321cc83aaffe183eb51cc6fc7768273ff2f68`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
