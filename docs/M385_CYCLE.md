# M385 qualification cycle

`LeafDecaySetCycle` rebuilds isolated oak, spruce, and birch log-plus-leaf
fixtures in two fresh official server JVMs. Each run removes log support,
waits a bounded official random-tick window until leaves `18:8`, `18:9`,
and `18:10` become air, and reloads those air cells. One official EOF is
retried after a 5 second sleep. Headless `B173WireClient` protocol-14 is
the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/LeafDecaySetCycle.java m385-leaf-decay-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`3974fe1e9ab8e39e20e8122dce05d183745ba923b5b1dd4306f63c308e0f2e1c`.
