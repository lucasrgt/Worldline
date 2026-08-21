# M11 Aero Attribution Evidence Map

## Purpose

This cycle promotes deterministic render-work attribution above the M10
Pbuffer and qualifies the exact user-supplied Aero Model Lib source revision.

## Boundaries

The neutral `FrameAttribution` analysis knows only frame duration, host-pause
duration, and named work counters. The Aero adapter alone understands
`Aero_FrameSpikeLogger` fields. Neither the API nor kernel depends on Aero,
StationAPI, Gradle, LWJGL, or Minecraft classes.

The ignored checkout must have the pinned origin and commit and no tracked
changes. The cycle runs all 222 core tests with their intended animation-budget
properties, builds the StationAPI 3.0.0 JAR, compiles the StationAPI test mod
against that live composite build, validates its descriptor, and executes an
isolated class-load probe from the built JAR. It then boots the real Fabric
Loader/StationAPI client, observes both Aero test-mod entrypoints, and lets the
benchmark hook stop the process after eight seconds. A narrow Gradle init
script removes an upstream diagnostic `println` closure whose multiline unary
`+` fails before JavaExec; it does not patch library or game code.

## Attribution proof

M10 counts the calls issued before the Pbuffer boundary: one draw, one color
change, four vertices, and zero texture binds. M11 also maps exact-format Aero
frame lines into neutral counters. A five-times-slower frame with sixty-times
more accepted/flushed animation work is classified `LOGICAL_WORK`; the same
slowdown with stable work and a 36 ms GC pause is classified `RUNTIME_STALL`.
Both diagnoses are repeated in fresh JVMs and frozen by SHA-256.

## Non-claims

The bounded fixture proves attribution mechanics and build/load compatibility.
It does not yet reproduce the historical random spike in a saved world, prove
cross-machine frame-time determinism, or identify a production root cause.
The current startup also reports a caught showcase-block UV lookup before the
StationAPI atlas is ready. M11 records that non-fatal diagnostic explicitly.
