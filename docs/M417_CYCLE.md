# M417 qualification cycle

`RemainingTntPlaceCycle` rebuilds the raised two-TNT fixture in two fresh
official server JVMs. Each run places two TNT items `46`, primes one with
flint-and-steel `259`, requires Packet60 at strength `4`, and observes the
second TNT cell also primed then exploded. One official EOF is retried after
a 5 second sleep. Headless `B173WireClient` is the only client. There is no
GUI and no Aero path.

Run directly with:

```text
java tools/smoke/RemainingTntPlaceCycle.java m417-remaining-tnt-place
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`153e7f2258e4d355e0e2c070a630aebe6dfa4262d98a3e4aa3e99b8f99e0205d`.
