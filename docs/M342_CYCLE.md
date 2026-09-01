# M342 qualification cycle

`GravityBlockSetCycle` rebuilds the supported sand and gravel fixture
in two fresh official server JVMs. Each run removes both stone
supports, requires Packet23 type `70` for sand `12` and type `71` for
gravel `13`, waits for one-cell settlement of both blocks, and reloads
both settled cells. Headless `B173WireClient` is the only client.
There is no GUI and no Aero path. One official EOF is retried after a
5 second sleep.

Run directly with:

```text
java tools/harness/Gate.java --milestone m342-gravity-block-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`959924fb1c3833226b9a7c0ffeebe212f9be0621ba06ca2f083e08492c72d066`.
