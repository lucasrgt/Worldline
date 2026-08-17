# M6 Trace Viewer and First-Divergence Contract

Worldline v0.4.0 makes canonical `v2` state traces directly inspectable and
comparable without starting Minecraft. Parsing lives in `worldline-trace`;
rendering and comparison live in the independently compiled
`worldline-analysis` module.

## Accepted trace

M6 accepts the schema-bearing grammar emitted by `CanonicalStateTrace`:

```text
v2|seed=<long>|schema=<field,...>|<label>=<long,...>|...
```

The parser limits input to 4,194,304 characters, while the CLI also caps files
at 4 MiB. It validates format version,
canonical signed decimal spelling, non-empty unique field names, protocol-safe
labels, record widths, and every value. The CLI additionally requires strict
UTF-8 bytes. Parsed fields, records, and values are caller-immutable.

Legacy `v1` world-column traces are not promoted into the M6 analysis model;
they remain frozen regression evidence under their original protocol.

## Viewer

```text
java tools/replay/Replay.java trace show run.wltrace
```

The viewer prints format, seed, record count, canonical trace SHA-256, and a
tab-separated table containing record index, label, schema fields, and values.
Success emits `WORLDLINE_TRACE_SHOW=PASS` and exits 0.

## First divergence

```text
java tools/replay/Replay.java trace diff baseline.wltrace candidate.wltrace
```

Comparison follows serialized semantic order:

1. seed;
2. schema field order or length;
3. record label at each index;
4. field values from left to right;
5. first extra or missing record.

Equal traces emit `WORLDLINE_TRACE_DIFF=EQUAL` and exit 0. A difference is a
successful analysis result: it emits `WORLDLINE_TRACE_DIFF=DIVERGED`, kind,
record index/label, field index/name, ordered left/right values, and exits 3.
When the diverged field has a closed catalog alias, the CLI also prints
`role=<ROLE>`. The exact `TraceDiff` document is unchanged so fingerprints
stay stable. Usage errors exit 2; unreadable or invalid traces exit 1.

## Non-claims

M6 compares exact canonical integer state vectors. It does not infer causality,
align reordered records, tolerate schema evolution, decode double bit patterns,
render charts, stream unbounded traces, compare legacy `v1`, or automatically
capture traces from arbitrary mods. Causal explanations and richer interactive
visualization require later evidence and contracts.
