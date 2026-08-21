# M275 qualification cycle

`CactusDamageCycle` rebuilds the raised sand cactus fixture in two fresh
official server JVMs. Each run plants cactus `81` on sand `12`, moves the
headless `B173WireClient` into contact, and freezes Packet8 health `20 -> 19`
with Packet38 status 2 when observed. One official EOF is retried after a
5 second sleep.

The frozen semantic SHA-256 is
`c708ae878b6079760d5c246f952ca1789d98c31e395a568ad9c1a2d751ef6df8`.

Canonical evidence uses two official server JVMs and four client sessions.

```text
java tools/smoke/CactusDamageCycle.java m275-cactus-damage
```
