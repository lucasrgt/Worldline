# M354 qualification cycle

`FarmlandHydrateSetCycle` rebuilds the raised dirt, still-water, and
isolated dry-dirt fixture in two fresh official server JVMs. Each run plants
the isolated dry plot, installs water, then hoes four water-adjacent plots to farmland `60`,
waits official random ticks, and reloads dry `60:0` plus hydrated `60:7`.
The frozen signal includes both `dry=60:0` and `hydrated=60:7`. Headless
`B173WireClient` is the only client. There is no GUI and no Aero path.
One official EOF or missed hydrate/hold is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/FarmlandHydrateSetCycle.java m354-farmland-hydrate-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`31e18ca11dc6928034468d2a503769a4559f5757e60dffc22e8bf85af35522d2`.
