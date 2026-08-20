# M369 qualification cycle

`CakeFullEatSetCycle` rebuilds the raised stone plus `BlockCake` fixture in
two fresh official server JVMs. Each run places cake item 354 as uneaten
`92:0`, eats remaining slices with empty-hand Packet15 through metadata
`3->4->5` and sixth-bite air, and reloads the air cell. The live signal
must include multiple `92` metadata values past M335. One official EOF is
retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/CakeFullEatSetCycle.java m369-cake-full-eat-set
```

The frozen semantic SHA-256 is
`1e7b764b96a4af45a053eec0e064137715747cb2554f80daaece626bee17a371`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
