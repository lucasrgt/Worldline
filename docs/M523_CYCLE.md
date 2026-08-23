# M523-SW qualification cycle

Run only while holding the exclusive official-runtime lock:

```text
java tools/smoke/WorldTimeAdvanceCycle.java m523-sw-world-time-advance
```

The cycle compiles the server adapter and smoke, verifies the official JAR, and
runs two fresh three-boot scenarios. Qualification requires identical semantic
signals and signatures. Use `-Dworldline.m523.diagnostic=true` only to discover
the candidate signature while `expected.signature=pending`; then freeze the
signature and rerun the same cycle.

## Frozen evidence

The expected signature is `583ff279e5fecfafedd95a704a77525872d14cd939775376b47a4a116d2b30f7`.
