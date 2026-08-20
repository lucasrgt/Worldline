# M309 qualification cycle

`RailPowerCycle` rebuilds the raised stone fixture in two fresh official
server JVMs. Each run places powered-rail item `27`, adjacent floor torch
`76`, detector rail item `28` two cells west, and minecart item `328` on that
detector. It freezes live `27:8` and occupied `28:8`, then reloads both cells
after save plus a fresh login. The result is distinct from unpowered `27:0`,
unpowered `28:0`, torch-only placement, and regular rail `66`. One official
EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`ff3995ce5426f88877abdf561aada4f7f2968dfa7fbdc44f768202ec4c14ff80`.

Run directly with:

```text
java tools/smoke/RailPowerCycle.java m309-rail-power
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
