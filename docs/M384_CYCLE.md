# M384 qualification cycle

`CactusSugarSetCycle` rebuilds the raised sand, grass, and still-water fixture
in two fresh official server JVMs. Each run plants cactus `81` on sand and
sugar cane `83` on grass beside water, waits a bounded random-tick window until
both reach height `>= 2`, and reloads the same cells. The signal must include
`plants=81+83`. One official EOF is retried after a 5 second sleep. Headless
`B173WireClient` protocol-14 is the only client. There is no GUI and no Aero
path.

The frozen semantic SHA-256 is
`ebe81626228e8dc034975562ddc312713b9877d4020a97cec9b6e38884191824`.

Run directly with:

```text
java tools/smoke/CactusSugarSetCycle.java m384-cactus-sugar-set
```

Canonical evidence uses two official server JVMs and four client sessions.
