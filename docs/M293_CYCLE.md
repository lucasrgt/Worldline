# M293 qualification cycle

`StickyPistonPlaceCycle` rebuilds the raised stone fixture in two fresh
official server JVMs. Each run places sticky piston item `29` and reloads
facing metadata `1`. Headless `B173WireClient` is the only client. There is
no GUI and no Aero path. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`bf6cf185cefc337d8be549efbcdce76d5c7cff54669d136c1002f30b7ff25c1e`.

Run directly with:

```text
java tools/smoke/StickyPistonPlaceCycle.java m293-sticky-piston-place
```

Canonical evidence uses two official server JVMs and four client sessions.
