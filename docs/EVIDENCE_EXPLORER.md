# Evidence Explorer

The Evidence Explorer is a deterministic, read-only projection of Worldline's
tracked qualification evidence. Build it from the repository root with:

```text
java tools/harness/Gate.java --evidence-explorer
```

The command validates and reads these authoritative sources:

- `smokes/qualification.lock`
- `smokes/qualification-evidence/*.proof`
- `smokes/train-reconciliation.lock`
- each tracked `smoke.properties`
- `coordination/swarm/dispositions/*.properties`

It atomically writes canonical JSON and a dependency-free static dashboard to
`.worldline/reports/evidence-explorer/`. Rebuilding an unchanged tree produces
byte-identical output. The JSON exposes every pin's provenance, train class,
fingerprint, observation digest, proof-envelope digest, and last exact execution
head when the train records one. It also exposes supervised dispositions and
orphan proof envelopes.

The Explorer is not a verdict authority. It cannot promote evidence, alter a
qualification lock, or replace `Gate.java`. The canonical projection excludes
ephemeral local executor receipts, leases, and machine state; a future live
overlay may visualize those inputs without changing the canonical snapshot.
