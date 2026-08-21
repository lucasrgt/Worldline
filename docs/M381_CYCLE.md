# M381 qualification cycle

`TntPrimeSetCycle` rebuilds the raised TNT fixture in two fresh
official server JVMs. Each run places TNT item `46`, primes it with
flint-and-steel `259`, requires Packet23 type `50` on the existing
object tracker, waits through the official fuse, and reloads the air
crater. One official EOF is retried after a 5 second sleep. Headless
`B173WireClient` is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/TntPrimeSetCycle.java m381-tnt-prime-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`6cf1cfe074d14a3c856cf768c9a8b9cdc9cfa573b8ee2e901445db31692bfad5`.
