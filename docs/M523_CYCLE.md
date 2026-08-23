# M523-SW-WORLD-TIME-ADVANCE Sw world time advance

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

This milestone freezes the behavior identified by its expected signal and semantic signature.

## Qualification cycle

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

Expected signal: `persisted=signed-long,restart=preserved,profile=overworld+nether,heartbeats=80,advance=bounded,no-heartbeat=smaller,save=clean,clients=1`.

Frozen semantic SHA-256: `583ff279e5fecfafedd95a704a77525872d14cd939775376b47a4a116d2b30f7`.
