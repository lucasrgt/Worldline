# M373 qualification cycle

`MilkBucketSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run retargets default spawner `52` to `Cow`,
fills empty bucket `325` from a living type-`92` cow through Packet7
button 0, air-uses milk `335` back to empty `325`, and reloads that
empty bucket. The signal must include fill plus drink for `325/335`.
One official EOF is retried after a 5 second sleep. Headless
`B173WireClient` is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/MilkBucketSetCycle.java m373-milk-bucket-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`0def850e0165e277e1055538ab58e3a7772dcf0239f16acbc88f430b10e9a77c`.
